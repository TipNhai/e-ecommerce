package com.example.demo.payment.adapter;

import com.example.demo.dto.request.InitiatePaymentRequest;
import com.example.demo.dto.response.PaymentResponse;
import com.example.demo.entity.Payment;
import com.example.demo.enums.PaymentMethod;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentGatewayAdapter {
    //Xác định Adapter phục vụ cho PaymentMethod nào
    PaymentMethod getPaymentMethod();

    //Khởi tạo thanh toán URL/QR ,...
    PaymentResponse initiatePayment(Payment payment, InitiatePaymentRequest request, HttpServletRequest httpServletRequest);

    //Xử lý callback/IPN trả về từ bên thứ 3
    void processCallback(Payment payment, Map<String,String>params);
}
