package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username:your_email@gmail.com}")
    private String fromEmail;

    /**
     * Gửi mail dạng text đơn giản (backward compatibility)
     */
    public void sendSimpleEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Đã gửi Email (Text) thành công tới: {}", toEmail);
        } catch (Exception e) {
            log.error("Gửi Email (Text) thất bại tới {}. Lỗi: {}", toEmail, e.getMessage());
            throw new RuntimeException("Lỗi gửi mail", e);
        }
    }

    /**
     * Gửi Email dạng HTML trực tiếp
     */
    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Đã gửi Email (HTML) thành công tới: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Gửi Email (HTML) thất bại tới {}. Lỗi: {}", toEmail, e.getMessage());
            throw new RuntimeException("Lỗi gửi mail HTML", e);
        }
    }

    /**
     * Hàm dùng chung render template Thymeleaf và gửi HTML email
     */
    public void sendTemplateEmail(String toEmail, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            if (variables != null) {
                context.setVariables(variables);
            }
            String htmlContent = templateEngine.process("email/" + templateName, context);
            sendHtmlEmail(toEmail, subject, htmlContent);
        } catch (Exception e) {
            log.error("Render & gửi email template [{}] thất bại tới {}. Lỗi: {}", templateName, toEmail, e.getMessage(), e);
            throw new RuntimeException("Lỗi render email template: " + templateName, e);
        }
    }

    /**
     * Email 1: Chào mừng thành viên mới (Phong cách TikTok / Notion)
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("fullName", fullName != null ? fullName : "Quý khách");
        variables.put("shopUrl", "http://localhost:3000");

        String subject = "🎉 Chào mừng bạn gia nhập ShopSmart Ecosystem!";
        sendTemplateEmail(toEmail, subject, "welcome-template", variables);
    }

    /**
     * Email 2: Xác nhận đơn hàng & Hóa đơn chi tiết (Phong cách Shopee / Amazon)
     */
    public void sendOrderConfirmationEmail(String toEmail, Order order, User user) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderCode", order.getOrderCode());
        variables.put("orderId", order.getId());
        variables.put("customerName", user != null ? user.getFullName() : order.getRecipientName());
        variables.put("recipientName", order.getRecipientName() != null ? order.getRecipientName() : (user != null ? user.getFullName() : "Quý khách"));
        variables.put("recipientPhone", order.getRecipientPhone());
        variables.put("shippingAddress", order.getShippingAddress());
        variables.put("paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD (Thanh toán khi nhận)");
        variables.put("subtotal", order.getSubtotal() != null ? order.getSubtotal() : order.getFinalAmount());
        variables.put("discountAmount", order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO);
        variables.put("finalAmount", order.getFinalAmount());
        variables.put("items", order.getOrderItems());
        variables.put("trackingUrl", "http://localhost:3000/orders/" + order.getId());

        String subject = "✅ Xác nhận đơn hàng #" + order.getOrderCode() + " thành công - ShopSmart";
        sendTemplateEmail(toEmail, subject, "order-confirmation", variables);
    }

    /**
     * Email 3: Xác thực OTP bảo mật / Quên mật khẩu (Phong cách Google / Apple)
     */
    @Async
    public void sendOtpEmail(String toEmail, String recipientName, String otpCode, int expiryMinutes) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("recipientName", recipientName != null ? recipientName : "Quý khách");
        variables.put("otpCode", otpCode);
        variables.put("expiryMinutes", expiryMinutes > 0 ? expiryMinutes : 5);

        String subject = "🔐 [ShopSmart] Mã xác thực bảo mật: " + otpCode;
        sendTemplateEmail(toEmail, subject, "otp-verification", variables);
    }
}
