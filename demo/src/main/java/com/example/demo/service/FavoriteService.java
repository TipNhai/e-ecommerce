package com.example.demo.service;

import com.example.demo.dto.request.AddFavoriteRequest;
import com.example.demo.dto.response.FavoriteResponse;
import com.example.demo.entity.Favorite;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.FavoriteMapper;
import com.example.demo.repository.FavoriteRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
@Service
public class FavoriteService {
    UserRepository userRepository;
    FavoriteRepository favoriteRepository;
    FavoriteMapper favoriteMapper;
    ProductRepository productRepository;
    RedisTemplate<String , Object>redisTemplate;

    // Lấy danh sách yêu thích của User
    @Transactional
    @Cacheable(value = "favorites", key = "#userId", sync = true)
    public FavoriteResponse getMyFavorite(String userId){
        Favorite favorite = favoriteRepository.findByUserId(userId)
                .orElseGet(() -> createFavorite(userId));
        if(favorite.getProducts() == null)
            favorite.setProducts(new ArrayList<>());
        return favoriteMapper.toResponse(favorite);
    }

    // Thêm sản phẩm vào danh sách Favorite
    @Transactional
    @CacheEvict(value = "favorites", key = "#userId")
    public FavoriteResponse addProductToFavorite(String userId, AddFavoriteRequest request){
        Favorite favorite = favoriteRepository.findByUserId(userId)
                .orElseGet(() -> createFavorite(userId));
        // Đảm bảo list không null tránh NPE
        if (favorite.getProducts() == null) {
            favorite.setProducts(new ArrayList<>());
        }
        String productLikesKey = "product:likes:" + request.getProductId();
        boolean isExisted = redisTemplate.opsForSet().isMember(productLikesKey , userId);
        if(!isExisted){
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        favorite.getProducts().add(product);
        // Thêm user vào danh sách yêu thích sản phẩm này
        redisTemplate.opsForSet().add(productLikesKey, userId);
        favoriteRepository.save(favorite);
        }else{
            throw new AppException(ErrorCode.PRODUCT_ALREADY_FAVORITED);
        }
        return favoriteMapper.toResponse(favorite);
    }

    // Xóa một sản phẩm khỏi danh sách Favorite
    @Transactional
    @CacheEvict(value = "favorites", key = "#userId")
    public FavoriteResponse removeProductFromFavorite(String userId, Long productId){
        Favorite favorite = favoriteRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.FAVORITE_NOT_FOUND));
        if (favorite.getProducts() == null || favorite.getProducts().isEmpty()) {
            throw new AppException(ErrorCode.EMPTY);
        }
        Product product = favorite.getProducts().stream()
                .filter(p->p.getId().equals(productId))
                .findFirst()
                .orElseThrow(()->new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        String productLikesKey ="product:likes:" + product.getId();
        redisTemplate.opsForSet().remove(productLikesKey,userId);
        favorite.getProducts().remove(product);
        return favoriteMapper.toResponse(favorite);
    }

    // Kiểm tra xem một sản phẩm đã nằm trong Favorite của User chưa
    @Transactional(readOnly = true)
    public boolean isProductInFavorite(String userId, Long productId){
        String productLikesKey = "product:likes:" + productId;
        // Hỏi trực tiếp Redis: "Trong cái Set của SP này có userId này không?"
        Boolean isMember = redisTemplate.opsForSet().isMember(productLikesKey, userId);
        return Boolean.TRUE.equals(isMember);
    }


    private Favorite createFavorite(String userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        Favorite favorite = new Favorite();
        favorite.setProducts(new ArrayList<>());
        favorite.setUser(user);
        return favoriteRepository.save(favorite);
    }
}
