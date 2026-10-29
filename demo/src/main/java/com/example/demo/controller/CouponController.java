package com.example.demo.controller;

import com.example.demo.dto.request.ApplyCouponRequest;
import com.example.demo.dto.request.CreateCouponRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.CouponCalculationResponse;
import com.example.demo.dto.response.CouponResponse;
import com.example.demo.service.CouponService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CouponController {

    CouponService couponService;

    // Admin tạo coupon
    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CreateCouponRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<CouponResponse>builder()
                        .message("Tạo mã khuyến mãi thành công")
                        .result(couponService.createCoupon(request))
                        .build()
        );
    }

    // User kiểm tra và áp thử mã giảm giá trước khi đặt hàng
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<CouponCalculationResponse>> checkCoupon(
            @Valid @RequestBody ApplyCouponRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<CouponCalculationResponse>builder()
                        .result(couponService.checkAndCalculateCoupon(request))
                        .build()
        );
    }

    // Xem danh sách mã khuyến mãi đang có hiệu lực
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAvailableCoupons() {
        return ResponseEntity.ok(
                ApiResponse.<List<CouponResponse>>builder()
                        .result(couponService.getAvailableCoupons())
                        .build()
        );
    }
}
