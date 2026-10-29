package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.User;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.service.DlqRetryService;
import com.example.demo.service.MailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDLQController {

    DlqRetryService dlqRetryService;
    MailService mailService;

    @PostMapping("/retry-failed-emails")
    public ApiResponse<String> retryFailedEmails() {
        String result = dlqRetryService.retryFailedEmails();
        return ApiResponse.<String>builder()
                .result(result)
                .build();
    }

    @PostMapping("/retry-user-dlq")
    public ApiResponse<String> retryUserDlq() {
        String result = dlqRetryService.retryFailedUserRegistrationEmails();
        return ApiResponse.<String>builder()
                .result(result)
                .build();
    }

    @PostMapping("/retry-all-dlq")
    public ApiResponse<String> retryAllDlq() {
        String result = dlqRetryService.retryAllFailedMessages();
        return ApiResponse.<String>builder()
                .result(result)
                .build();
    }

    @PostMapping("/test-welcome-email")
    public ApiResponse<String> testWelcomeEmail(@RequestParam String toEmail, 
                                                @RequestParam(defaultValue = "Típ Nguyễn") String fullName) {
        mailService.sendWelcomeEmail(toEmail, fullName);
        return ApiResponse.<String>builder()
                .result("Đã gửi email Chào mừng mẫu tới: " + toEmail)
                .build();
    }

    @PostMapping("/test-order-email")
    public ApiResponse<String> testOrderEmail(@RequestParam String toEmail) {
        Order mockOrder = Order.builder()
                .id(999L)
                .orderCode("ORD-889966")
                .recipientName("Típ Nguyễn")
                .recipientPhone("0987 654 321")
                .shippingAddress("Tòa nhà Landmark 81, Phường 22, Quận Bình Thạnh, TP. Hồ Chí Minh")
                .paymentMethod(PaymentMethod.COD)
                .subtotal(new BigDecimal("1250000"))
                .discountAmount(new BigDecimal("50000"))
                .finalAmount(new BigDecimal("1200000"))
                .orderItems(List.of(
                        OrderItem.builder()
                                .productName("Bàn phím cơ không dây Bluetooth RGB")
                                .quantity(1)
                                .price(new BigDecimal("850000"))
                                .build(),
                        OrderItem.builder()
                                .productName("Chuột công thái học Wireless Silent")
                                .quantity(1)
                                .price(new BigDecimal("400000"))
                                .build()
                ))
                .build();

        User mockUser = User.builder()
                .fullName("Típ Nguyễn")
                .email(toEmail)
                .build();

        mailService.sendOrderConfirmationEmail(toEmail, mockOrder, mockUser);
        return ApiResponse.<String>builder()
                .result("Đã gửi email Hóa đơn đặt hàng mẫu tới: " + toEmail)
                .build();
    }

    @PostMapping("/test-otp-email")
    public ApiResponse<String> testOtpEmail(@RequestParam String toEmail,
                                            @RequestParam(defaultValue = "889966") String otpCode) {
        mailService.sendOtpEmail(toEmail, "Típ Nguyễn", otpCode, 5);
        return ApiResponse.<String>builder()
                .result("Đã gửi email Mã xác thực OTP mẫu tới: " + toEmail)
                .build();
    }
}
