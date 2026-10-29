package com.example.demo.dto.response;

import com.example.demo.entity.Coupon;

import java.math.BigDecimal;

//Class để trả về object sử dụng trong createOrder tránh để coupon gọi xuống db 2 lần
public record CouponAppliedResult(
        Coupon coupon,
        BigDecimal discountAmount
) {}