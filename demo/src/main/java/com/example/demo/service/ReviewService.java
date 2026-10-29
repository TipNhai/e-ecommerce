package com.example.demo.service;

import com.example.demo.dto.request.CreateReviewRequest;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Review;
import com.example.demo.entity.ReviewImage;
import com.example.demo.enums.OrderStatus;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.ReviewMapper;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ReviewRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ReviewService {
    ReviewRepository reviewRepository;
    ReviewMapper reviewMapper;
    OrderRepository orderRepository;

    @Transactional
    public ReviewResponse createReview(String userId, CreateReviewRequest request){
        //Kiểm tra order và user
        Order order = orderRepository.findByIdAndUserId(request.getOrderId(), userId)
                .orElseThrow(()->new AppException(ErrorCode.ORDER_NOT_FOUND));
        //Nếu order chưa được giao
        if(order.getStatus() != OrderStatus.DELIVERED)
            throw new AppException(ErrorCode.ORDER_NOT_DELIVERED);
        //Kiểm tra xem sản phẩm có ở trong order hay không
        OrderItem matchedItem = order.getOrderItems().stream().
                filter(item->item.getProduct() != null &&item.getId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(()->new AppException(ErrorCode.PRODUCT_NOT_IN_ORDER));
        // 4. Kiểm tra xem người dùng đã đánh giá sản phẩm này TRONG ĐƠN HÀNG NÀY chưa
        if (reviewRepository.existsByOrderIdAndProductId(request.getOrderId(), request.getProductId())) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .order(order)
                .user(order.getUser())
                .product(matchedItem.getProduct())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        // 6. Gắn ảnh đánh giá (nếu có)
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            request.getImageUrls().forEach(url -> {
                ReviewImage image = ReviewImage.builder()
                        .imageUrl(url)
                        .build();
                review.addImage(image);
            });
        }
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponse(savedReview);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(Long productId , int page , int size , String sortDir ) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by("rating").descending() :
                Sort.by("rating").ascending();

        Pageable pageable = PageRequest.of(page,size,sort);
        Page<Review>reviewPage = reviewRepository.findAllByProductId(productId,pageable);
        Page<ReviewResponse>reviewResponsePage = reviewPage.map(reviewMapper::toResponse);
        return PageResponse.from(reviewResponsePage);
    }
}
