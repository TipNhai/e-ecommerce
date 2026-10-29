package com.example.demo.controller;

import com.example.demo.dto.request.InitiatePaymentRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.PaymentResponse;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.demo.entity.User;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    PaymentService paymentService;

    /**
     * 1. KHỞI TẠO THANH TOÁN (Yêu cầu đăng nhập)
     * Client gọi API này để lấy URL redirect (VNPAY/MoMo) hoặc thông tin VietQR/COD.
     */
    @PostMapping("/initiate")
    public ApiResponse<PaymentResponse>initiatePayment( 
            @RequestBody InitiatePaymentRequest request, 
            HttpServletRequest httpServletRequest,
            @AuthenticationPrincipal User user){
        
        String userId = (user != null) ? user.getId() : null; 
        
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.initiatePayment(request, userId, httpServletRequest))
                .build();
    }

    /**
     * 2. XỬ LÝ CALLBACK / RETURN URL TỪ CÁC CỔNG (Public Endpoint)
     * Được gọi khi người dùng thanh toán xong và trình duyệt redirect về hệ thống.
     * Cần cấu hình permitAll() trong Spring Security cho endpoint này.
     */
    @GetMapping("/callback/{method}")
    public ApiResponse<PaymentResponse>handleCallback(
            @PathVariable PaymentMethod method,
            @RequestParam(name = "vnp_TxnRef", required = false) String vnpTxnRef,
            @RequestParam(name = "orderId", required = false) String momoOrderId,
            @RequestParam Map<String, String> queryParams
    ){
        // Tự động nhận diện mã tham chiếu theo từng cổng (VNPAY dùng vnp_TxnRef, MoMo dùng orderId)
        String referenceCode = (vnpTxnRef != null && !vnpTxnRef.isBlank()) ? vnpTxnRef : momoOrderId;
        return ApiResponse.<PaymentResponse>builder()
                .message("Xử lý kết quả thanh toán thành công")
                .result(paymentService.processCallback(method,referenceCode,queryParams))
                .build();
    }

    /**
     * 3. XỬ LÝ IPN (Instant Payment Notification / Webhook - Public Endpoint)
     * Dành riêng cho máy chủ MoMo/VNPAY bắn tín hiệu ngầm dạng Server-to-Server qua POST.
     */
    @PostMapping("/ipn/{method}")
    public ApiResponse<PaymentResponse>handleIpn(
            @PathVariable PaymentMethod method,
            @RequestBody Map<String, String> payload
    ){
        String referenceCode = payload.get("orderId"); // Thường là MoMo IPN
        return ApiResponse.<PaymentResponse>builder()
                .message("Xác thực IPN thành công")
                .result(paymentService.processCallback(method,referenceCode,payload))
                .build();
    }

    /**
     * 4. TRA CỨU THÔNG TIN THANH TOÁN CỦA ĐƠN HÀNG (Yêu cầu đăng nhập)
     */
    @GetMapping("/order/{orderId}")
    public ApiResponse<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId
    ) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);

        return ApiResponse.<PaymentResponse>builder()
                        .result(response)
                        .build();
    }

    @GetMapping("/order/{referenceCode}")
    public ApiResponse<PaymentResponse> getPaymentByReferenceCode(
            @PathVariable Long referenceCode
    ) {
        PaymentResponse response = paymentService.getPaymentByOrderId(referenceCode);

        return ApiResponse.<PaymentResponse>builder()
                .result(response)
                .build();
    }
}
