package com.example.demo.service;

import com.example.demo.dto.request.ApplyCouponRequest;
import com.example.demo.dto.request.CreateCouponRequest;
import com.example.demo.dto.response.CouponAppliedResult;
import com.example.demo.dto.response.CouponCalculationResponse;
import com.example.demo.dto.response.CouponResponse;
import com.example.demo.entity.Coupon;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderCoupon;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.CouponMapper;
import com.example.demo.repository.CouponRepository;
import com.example.demo.repository.OrderCouponRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class CouponService {
    CouponRepository couponRepository;
    OrderCouponRepository orderCouponRepository;
    CouponMapper couponMapper;

    @Transactional
    public CouponResponse createCoupon(CreateCouponRequest request){
        if(couponRepository.existsByCodeIgnoreCase(request.getCode().trim()))
            throw new AppException(ErrorCode.COUPON_ALREADY_EXISTS);

        if(request.getEndDate().isBefore(request.getStartDate()))
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);

        Coupon coupon = couponMapper.toEntity(request);
        coupon.setCode(request.getCode().toUpperCase().trim());
        couponRepository.save(coupon);
        return couponMapper.toResponse(coupon);
    }

    // 2. Dành cho Client: Ước tính số tiền giảm khi nhập mã ở màn Checkout
    @Transactional(readOnly = true)
    public CouponCalculationResponse checkAndCalculateCoupon(ApplyCouponRequest request){
        Coupon coupon = couponRepository.findByCodeIgnoreCaseAndIsActiveTrue(request.getCode().trim())
                .orElseThrow(()-> new AppException(ErrorCode.COUPON_NOT_FOUND));

        if(!coupon.isValidForOrder(request.getOrderTotal()))
            throw new AppException(ErrorCode.COUPON_NOT_APPLICABLE);

        BigDecimal discountAmount = coupon.calculateDiscount(request.getOrderTotal());
        BigDecimal finalTotal = request.getOrderTotal().subtract(discountAmount).max(BigDecimal.ZERO);

        return CouponCalculationResponse.builder()
                .code(request.getCode())
                .originalTotal(request.getOrderTotal())
                .discountAmount(discountAmount)
                .finalTotal(finalTotal)
                .build();
    }
    // 3. Tích hợp trong luồng đặt hàng (OrderService gọi hàm này khi tạo đơn thành công)
    @Transactional
    public CouponAppliedResult validateAndApply(String code , BigDecimal subtotal){
        if (code == null || code.isBlank()) {
            return new CouponAppliedResult(null, BigDecimal.ZERO);
        }
        Coupon coupon = couponRepository.findByCodeIgnoreCaseAndIsActiveTrue(code)
                .orElseThrow(()->new AppException(ErrorCode.COUPON_NOT_FOUND));

        if(!coupon.isValidForOrder(subtotal))
            throw new AppException(ErrorCode.COUPON_NOT_APPLICABLE);

        BigDecimal discountAmount = coupon.calculateDiscount(subtotal);

        return new CouponAppliedResult(coupon, discountAmount);
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> getAvailableCoupons() {
        return couponRepository.findAll().stream()
                .filter(c -> c.getIsActive() && LocalDateTime.now().isBefore(c.getEndDate()))
                .map(couponMapper::toResponse)
                .toList();
    }

    @Transactional
    public void recordOrderCouponUsage(Order savedOrder, Coupon coupon, BigDecimal discountAmount) {
        if (coupon == null) return;

        // Tăng lượt dùng (Dirty checking tự update)
        coupon.setUsedCount(coupon.getUsedCount() + 1);

        OrderCoupon orderCoupon = OrderCoupon.builder()
                .order(savedOrder)
                .coupon(coupon)
                .discountAmount(discountAmount)
                .build();

        orderCouponRepository.save(orderCoupon);
    }

}
