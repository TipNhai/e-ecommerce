package com.example.demo.service;

import com.example.demo.dto.request.InitiatePaymentRequest;
import com.example.demo.dto.response.PaymentResponse;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.PaymentMapper;
import com.example.demo.payment.adapter.PaymentGatewayAdapter;
import com.example.demo.payment.factory.PaymentGatewayFactory;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class PaymentService {
    PaymentMapper paymentMapper;
    PaymentGatewayFactory paymentGatewayFactory;
    OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 1. KHỞI TẠO THANH TOÁN (INITIATE PAYMENT)
     * - Kiểm tra tính hợp lệ của đơn hàng
     * - Tìm kiếm hoặc tạo mới bản ghi Payment (trạng thái PENDING)
     * - Ủy quyền sinh URL/Payload thanh toán cho từng Adapter qua Factory
     */
    @Transactional
    public PaymentResponse initiatePayment( InitiatePaymentRequest request, String userId, HttpServletRequest httpServletRequest){
        Order order = orderRepository.findByIdAndUserId(request.getOrderId(),userId)
                .orElseThrow(()->new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED)
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);

        //tao moi hoac thay doi phuong thuc thanh toan
        Payment payment = paymentRepository.getByOrderId(request.getOrderId()).orElseGet(()->
                Payment.builder()
                        .order(order)
                        .build());

        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(order.getFinalAmount());
        payment.setReferenceCode(order.getOrderCode() + "_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());

        // 3. BẮT BUỘC SAVE để đưa đối tượng Transient vào DB (nếu là tạo mới)
        Payment savedPayment = paymentRepository.save(payment);

        PaymentGatewayAdapter paymentGatewayAdapter = paymentGatewayFactory.getAdapter(request.getMethod());

        return paymentGatewayAdapter.initiatePayment(savedPayment,request,httpServletRequest);
    }

    /**
     * 2. XỬ LÝ CALLBACK / IPN TỪ CÁC CỔNG THANH TOÁN
     * - Tra cứu bản ghi Payment theo ReferenceCode
     * - Adapter kiểm tra chữ ký số (Checksum) và cập nhật trạng thái
     * - Cập nhật trạng thái đơn hàng sang PROCESSING nếu giao dịch thành công
     */
    @Transactional
    public PaymentResponse processCallback(PaymentMethod paymentMethod, String referenceCode, Map<String , String>queryParams){
        Payment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(()-> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        //Kiểm tra xem payment thành công chưa để tránh xử lý trùng lặp IPN
        if(payment.getStatus() == PaymentStatus.SUCCESS)
            return paymentMapper.toResponse(payment);
        PaymentGatewayAdapter paymentGatewayAdapter = paymentGatewayFactory.getAdapter(paymentMethod);

        paymentGatewayAdapter.processCallback(payment,queryParams);
        Payment paymentUpdated = paymentRepository.save(payment);
        return  paymentMapper.toResponse(paymentUpdated);
    }

    /**
     * 3. TRA CỨU TRẠNG THÁI THANH TOÁN THEO ORDER ID
     */
    @Transactional
    public PaymentResponse getPaymentByOrderId(Long orderId){
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(()-> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return paymentMapper.toResponse(payment);
    }

    /**
     * 4. TRA CỨU TRẠNG THÁI THANH TOÁN THEO REFERENCE CODE
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReferenceCode(String referenceCode) {
        Payment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        return paymentMapper.toResponse(payment);
    }
}
