package com.example.demo.service;

import com.example.demo.dto.response.PageResponse;
import com.example.demo.event.OrderEvent;
import com.example.demo.dto.request.CreateOrderRequest;
import com.example.demo.dto.response.CouponAppliedResult;
import com.example.demo.dto.response.OrderResponse;
import com.example.demo.entity.Address;
import com.example.demo.entity.Cart;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    OrderMapper orderMapper;
    OrderRepository orderRepository;
    UserRepository userRepository;
    CartRepository cartRepository;
    AddressRepository addressRepository;
    CouponService couponService;
    RedisTemplate<String , Object> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        //Áp dụng Distributed Lock bằng Redis để chặn click đúp
        String lockKey = "lock:checkout:" + userId;
        //setIfAbsent nếu key chưa tồn tại sẽ set key thành LOCKED
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 3, TimeUnit.SECONDS);
        
        if (Boolean.FALSE.equals(acquired)) {
            throw new AppException(ErrorCode.TOO_MANY_REQUEST);
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_CART);
        }
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        // 1. Trừ kho và tính subtotal từ CartItems
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (var item : cart.getCartItems()) {
            var product = item.getProduct();
            if (product.getQuantity() < item.getQuantity()) {
                throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
            }
            product.setQuantity(product.getQuantity() - item.getQuantity());

            var orderItem = orderMapper.toOrderItem(item);
            orderItems.add(orderItem);
            subtotal = subtotal.add(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }

        //Kiểm tra và lấy luôn kết quả tính tiền + đối tượng Coupon
        CouponAppliedResult couponResult = couponService.validateAndApply(request.getCouponCode(), subtotal);

        BigDecimal discountAmount = couponResult.discountAmount();
        BigDecimal finalAmount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);

        //Khởi tạo Order với đầy đủ dữ liệu hoàn chỉnh ngay từ đầu
        Order order = Order.builder()
                .user(cart.getUser())
                .orderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status(OrderStatus.PENDING)
                .note(request.getNote())
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .recipientName(address.getRecipientName())
                .recipientPhone(address.getPhoneNumber())
                .shippingAddress(String.format("%s, %s, %s, %s",
                        address.getAddressDetail(), address.getWard(), address.getDistrict(), address.getProvince()))
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .orderItems(new ArrayList<>())
                .build();

        orderItems.forEach(order::addOrderItem);

        //Chỉ thực hiện lưu DB đúng 1 lần
        Order savedOrder = orderRepository.save(order);

        //Lưu vết vào bảng order_coupons & trừ used_count (nếu có dùng mã)
        if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            couponService.recordOrderCouponUsage(savedOrder, couponResult.coupon(), discountAmount);
        }

        //Xóa giỏ hàng
        cartRepository.delete(cart);

        redisTemplate.delete("cart:" + userId);  // BẮN SỰ KIỆN QUA KAFKA(Không gọi Mail hay Notification)
        OrderEvent event = new OrderEvent(
                savedOrder.getId().toString(),
                userId,
                "CREATED",
                finalAmount.doubleValue()
        );

        // Đảm bảo chỉ bắn Kafka SAU KHI Transaction MySQL đã COMMIT thành công
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    kafkaTemplate.send("order_events_topic", event.getOrderId(), event);
                }
            });
        } else {
            kafkaTemplate.send("order_events_topic", event.getOrderId(), event);
        }

        return orderMapper.toOrderResponse(savedOrder);
    }

    @EntityGraph
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);

        return PageResponse.<OrderResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPages(orderPage.getTotalPages())
                .totalElements(orderPage.getTotalElements())
                .content(orderPage.map(orderMapper::toOrderResponse).getContent())
                .build();
    }
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(String userId, Long orderId){
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(()-> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String userId, Long orderId){
        Order order = orderRepository.findByIdAndUserIdForCancel(orderId, userId)
                .orElseThrow(()-> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if(order.getStatus()!= OrderStatus.PENDING)
            throw new AppException(ErrorCode.ORDER_CANNOT_BE_CANCELLED);

        order.setStatus(OrderStatus.CANCELLED);

        for(var item : order.getOrderItems()){
            if(item.getProduct()!= null){
                var product = item.getProduct();
                product.setQuantity(product.getQuantity()+item.getQuantity());
            }
        }
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus){
        Order order = orderRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setStatus(newStatus);
        //Kiểm tra xem newStatus có phải DELIVERED hay không để thêm vào danh sách bán chạy
        if (order.getStatus() != OrderStatus.DELIVERED && newStatus == OrderStatus.DELIVERED){
            //Lấy năm tháng hiện tại
            String currentMonth = YearMonth.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString();
            String leaderboardKey = "trending:products:" + currentMonth;
            for(OrderItem item : order.getOrderItems()){
                String productIdStr = String.valueOf(item.getProduct().getId());
                double scoreToAdd = item.getQuantity();
                redisTemplate.opsForZSet().incrementScore(leaderboardKey,productIdStr,scoreToAdd);

                redisTemplate.expire(leaderboardKey, 60, TimeUnit.DAYS);
            }
        }
        order.setStatus(newStatus);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }
}
