package com.example.demo.mapper;

import com.example.demo.dto.request.CreateCouponRequest;
import com.example.demo.dto.response.CouponResponse;
import com.example.demo.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CouponMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usedCount", constant = "0")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Coupon toEntity(CreateCouponRequest request);

    CouponResponse toResponse(Coupon coupon);
}