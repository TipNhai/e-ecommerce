package com.example.demo.controller;

import com.example.demo.dto.request.CreateReviewRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.entity.User;
import com.example.demo.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {
    ReviewService reviewService;

    // Tạo đánh giá (Yêu cầu đăng nhập)
    @PostMapping
    ApiResponse<ReviewResponse>createReview(@Valid @RequestBody CreateReviewRequest request , @AuthenticationPrincipal User user){
        String userId = (user != null) ? user.getId() : null;
        return ApiResponse.<ReviewResponse>builder()
                .message("tạo đánh giá thành công")
                .result(reviewService.createReview(userId,request))
                .build();
    }

    // Lấy danh sách đánh giá của sản phẩm (Public endpoint)
//    @GetMapping("/product/{productId}")
//    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getProductReviews(
//            @PathVariable Long productId,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size
//    ) {
//        PageResponse<ReviewResponse> response = reviewService.getProductReviews(productId, page, size);
//
//        return ResponseEntity.ok(
//                ApiResponse.<PageResponse<ReviewResponse>>builder()
//                        .result(response)
//                        .build()
//        );
//    }
}
