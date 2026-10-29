package com.example.demo.payment.adapter;

import com.example.demo.dto.request.InitiatePaymentRequest;
import com.example.demo.dto.response.PaymentResponse;
import com.example.demo.entity.Payment;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.mapper.PaymentMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CodPaymentAdapter implements PaymentGatewayAdapter {
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.COD;
    }

    @Override
    public PaymentResponse initiatePayment(Payment payment, InitiatePaymentRequest request, HttpServletRequest httpServletRequest) {
        payment.setStatus(PaymentStatus.PENDING);
        PaymentResponse response = paymentMapper.toResponse(payment);
        response.setPaymentInstructions("Thanh toán khi nhận hàng (COD).");
        return response;
    }

    @Override
    public void processCallback(Payment payment, Map<String, String> params) {
        // COD được shipper xác nhận qua API giao vận nội bộ
        payment.setStatus(PaymentStatus.SUCCESS);
    }
}
