package com.example.demo.service;

import com.example.demo.dto.request.AddToCartRequest;
import com.example.demo.dto.request.UpdateCartItemRequest;
import com.example.demo.dto.response.CartResponse;
import com.example.demo.entity.Cart;
import com.example.demo.entity.CartItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.CartMapper;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService {
    CartRepository cartRepository;
    CartMapper cartMapper;
    UserRepository userRepository;
    ProductRepository productRepository;
    RedisTemplate<String , Object>redisTemplate;
    // Lấy giỏ hàng của User (nếu chưa có thì tự động khởi tạo Cart trống)
    @Transactional
    public CartResponse getMyCart(String userId){
        String cartKey = "cart:" + userId;
        Map<Object,Object> cartItems = redisTemplate.opsForHash().entries(cartKey);
        if(cartItems!=null && !cartItems.isEmpty()){
            // Đã có trong Redis!
            // dùng vòng lặp để convert cái Map này thành CartResponse rồi return luôn.
            // (Không cần chọc xuống DB nữa).
            return mapToCartResponse(userId,cartItems);
        }
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCartForUser(userId));
        Map<String, String> dataToCache = new HashMap<>();
        for(CartItem item : cart.getCartItems()){
            dataToCache.put(String.valueOf(item.getProduct().getId()),String.valueOf(item.getQuantity()));
        }
        if (!dataToCache.isEmpty()) {
            redisTemplate.opsForHash().putAll(cartKey, dataToCache);
        }
        return cartMapper.toCartResponse(cart);
    }

    // Thêm sản phẩm vào giỏ (Nếu sản phẩm đã có trong CartItem -> cộng dồn quantity)
    @Transactional
    public CartResponse addToCart(String userId, AddToCartRequest request){
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCartForUser(userId));
        // Đảm bảo danh sách không bị null
        if (cart.getCartItems() == null) {
            cart.setCartItems(new ArrayList<>());
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        String cartKey = "cart:" + userId;
        //Xử lý nếu sản phẩm đã có trong giỏ hàng
        Optional<CartItem>existingItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if(existingItemOpt.isPresent()){
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartRepository.save(cart);
        }else {
            CartItem cartItem = CartItem.builder()
                    .product(product)
                    .quantity(request.getQuantity())
                    .cart(cart)
                    .build();
        cart.getCartItems().add(cartItem);
        }
        redisTemplate.opsForHash().increment(cartKey,String.valueOf(request.getProductId()), request.getQuantity());
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toCartResponse(savedCart);
    }

    // Cập nhật số lượng của 1 CartItem theo cartItemId
    @Transactional
    public CartResponse updateCartItemQuantity(String userId, Long cartItemId, UpdateCartItemRequest request){
        //Vì là update nên nếu không tồn tại phải ném ra exception luôn
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        if(cart.getCartItems() == null || cart.getCartItems().isEmpty())
            throw new AppException(ErrorCode.EMPTY_CART);
        //dùng stream filter tận dụng ram , không cần xuống db
        CartItem cartItem = cart.getCartItems().stream()
                .filter(item->item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
        cartItem.setQuantity(request.getQuantity());
        //sửa cart trong redis
        Cart savedCart = cartRepository.save(cart);
        String cartKey = "cart:" + userId;
        redisTemplate.opsForHash().put(cartKey, String.valueOf(cartItem.getProduct().getId()), String.valueOf(request.getQuantity()));
        return cartMapper.toCartResponse(savedCart);
    }

    // Xóa 1 CartItem khỏi giỏ hàng
    @Transactional
    public CartResponse removeCartItem(String userId, Long cartItemId){
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        if(cart.getCartItems() == null || cart.getCartItems().isEmpty())
            throw new AppException(ErrorCode.EMPTY_CART);
        CartItem itemToRemove = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        String productIdStr = String.valueOf(itemToRemove.getProduct().getId());
        // 1. Xóa trong DB...
        cart.getCartItems().remove(itemToRemove);
        // 2. Xóa khỏi Redis Hash
        String cartKey = "cart:" + userId;
        redisTemplate.opsForHash().delete(cartKey, productIdStr);
        return cartMapper.toCartResponse(cart);
    }

    // Xóa sạch giỏ hàng (Xóa toàn bộ CartItem nhờ orphanRemoval = true)
    @Transactional
    public void clearCart(String userId){
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(()-> new AppException(ErrorCode.CART_NOT_FOUND));
        //Kiểm tra để tránh lỗi nullException
        if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
            // orphanRemoval = true sẽ tự động DELETE tất cả cart_items trong DB
            String cartKey = "cart:" + userId;
            //Xoá tất cả các
            redisTemplate.delete(cartKey);
            cart.getCartItems().clear();
        }
    }


    //
    private Cart createNewCartForUser(String userId) {
        // 1. Tìm User từ userId (Lấy từ Token)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Khởi tạo đối tượng Cart mới
        Cart newCart = new Cart();
        newCart.setUser(user);
        // Đảm bảo danh sách items trống, không bị null
        newCart.setCartItems(new ArrayList<>());

        // 3. Lưu xuống Database và trả về
        return cartRepository.save(newCart);
    }


    private CartResponse mapToCartResponse(String userId, Map<Object, Object> cartItemsFromRedis) {
        // 1. Tạo một User ảo để map vào Cart
        User mockUser = new User();
        mockUser.setId(userId);

        List<CartItem> cartItems = new ArrayList<>();
        // 2. Lặp qua Redis Hash để dựng lại danh sách CartItem
        for (Map.Entry<Object, Object> entry : cartItemsFromRedis.entrySet()) {
            String productIdStr = entry.getKey().toString();
            Integer quantity = Integer.parseInt(entry.getValue().toString());

            // Vẫn phải lấy Product thật từ DB để có giá tiền mới nhất tính toán
            Product product = productRepository.findById(Long.valueOf(productIdStr))
                    .orElse(null);

            if (product != null) {
                CartItem item = CartItem.builder()
                        .product(product)
                        .quantity(quantity)
                        .build();
                cartItems.add(item);
            }
        }

        // 3. Đóng gói thành Entity Cart ảo
        Cart mockCart = Cart.builder()
                .user(mockUser)
                .cartItems(cartItems)
                .build();

        // 4. Nhường lại toàn bộ phần tính toán và convert cho Mapper của bạn!
        return cartMapper.toCartResponse(mockCart);
    }
}
