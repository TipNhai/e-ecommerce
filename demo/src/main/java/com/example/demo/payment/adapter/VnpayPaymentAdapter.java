package com.example.demo.payment.adapter;

import com.example.demo.dto.request.InitiatePaymentRequest;
import com.example.demo.dto.response.PaymentResponse;
import com.example.demo.entity.Payment;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.PaymentMapper;
import com.example.demo.payment.config.VnpayConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Component
@RequiredArgsConstructor
public class VnpayPaymentAdapter implements PaymentGatewayAdapter{
    private final VnpayConfig vnpayConfig;

    private final PaymentMapper paymentMapper;

    public static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private static final DateTimeFormatter VNP_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public PaymentResponse initiatePayment(Payment payment, InitiatePaymentRequest request, HttpServletRequest httpServletRequest) {
        // VNPAY quy định số tiền = amount * 100 (bỏ dấu thập phân)
        long amount = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnpayConfig.getVersion());
        vnpParams.put("vnp_Command", vnpayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnpayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");

        if (request.getBankCode() != null && !request.getBankCode().isBlank()) {
            vnpParams.put("vnp_BankCode", request.getBankCode());
        }
        vnpParams.put("vnp_TxnRef", payment.getReferenceCode());
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang: " + payment.getOrder().getOrderCode());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", getClientIp(httpServletRequest));



// Lấy thời gian hiện tại chuẩn giờ Việt Nam (GMT+7)
        ZonedDateTime createDate = ZonedDateTime.now(VIETNAM_ZONE);
        String vnpCreateDate = createDate.format(VNP_DATE_FORMATTER);
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

// Thời gian hết hạn sau 15 phút
        ZonedDateTime expireDate = createDate.plusMinutes(15);
        String vnpExpireDate = expireDate.format(VNP_DATE_FORMATTER);
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);
        // 1. Sắp xếp key theo thứ tự Alphabet
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        // 2. Tạo chuỗi hash data và chuỗi query string
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build hash data
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // 3. Ký chữ ký số HMAC-SHA512
        String queryUrl = query.toString();
        String vnpSecureHash = VnpayConfig.hmacSHA512(vnpayConfig.getSecretKey(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

        String paymentUrl = vnpayConfig.getUrl() + "?" + queryUrl;

        PaymentResponse response = paymentMapper.toResponse(payment);
        response.setPaymentUrl(paymentUrl);
        return response;
    }

    @Override
    public void processCallback(Payment payment, Map<String, String> params) {
        Map<String, String> fields = new HashMap<>(params);

        // Lấy chữ ký gửi về từ VNPAY
        String vnpSecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        // 1. Sắp xếp các tham số trả về
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        // 2. Tạo chữ ký kiểm tra từ dữ liệu nhận được
        String signValue = VnpayConfig.hmacSHA512(vnpayConfig.getSecretKey(), hashData.toString());

        // 3. So khớp chữ ký

        if (vnpSecureHash == null ||!signValue.equalsIgnoreCase(vnpSecureHash)) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setResponseMessage("Chữ ký không hợp lệ (Invalid Checksum)");
            throw new AppException(ErrorCode.PAYMENT_INVALID_CHECKSUM);
        }

        // 4. Kiểm tra mã phản hồi
        String responseCode = fields.get("vnp_ResponseCode");
        String transactionNo = fields.get("vnp_TransactionNo");

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(transactionNo);
            // Khi set thời gian thanh toán:
            payment.setPaidAt(LocalDateTime.now(VIETNAM_ZONE));
            payment.setResponseMessage("Giao dịch VNPAY thành công");
            payment.getOrder().setStatus(OrderStatus.PROCESSING);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setResponseMessage("Giao dịch VNPAY thất bại. Mã lỗi: " + responseCode);
        }
    }

    //Helper
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(","))
            return ip.split(",")[0].trim() ;
        return ip != null ? ip : "127.0.0.1";
    }
}
