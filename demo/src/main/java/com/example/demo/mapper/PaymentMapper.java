package com.example.demo.mapper;

import com.example.demo.dto.response.PaymentResponse;
import com.example.demo.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "orderCode", source = "order.orderCode")
    @Mapping(target = "paymentUrl", ignore = true)
    @Mapping(target = "paymentInstructions", ignore = true)
    PaymentResponse toResponse(Payment payment);
}
