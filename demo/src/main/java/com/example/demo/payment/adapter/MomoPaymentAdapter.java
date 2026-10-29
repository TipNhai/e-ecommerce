package com.example.demo.payment.adapter;

import com.example.demo.dto.request.InitiatePaymentRequest;
import com.example.demo.dto.request.momo.MomoCreatePaymentRequest;
import com.example.demo.dto.response.PaymentResponse;
import com.example.demo.dto.response.momo.MomoCreatePaymentResponse;
import com.example.demo.entity.Payment;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.PaymentMapper;
import com.example.demo.payment.config.MomoConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MomoPaymentAdapter implements PaymentGatewayAdapter {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final MomoConfig momoConfig;
    private final PaymentMapper paymentMapper;
    private final RestClient restClient = RestClient.create();

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.MOMO;
    }

    @Override
    public PaymentResponse initiatePayment(Payment payment, InitiatePaymentRequest request, HttpServletRequest httpRequest) {
        String requestId = UUID.randomUUID().toString();
        String orderId = payment.getReferenceCode();
        long amount = payment.getAmount().longValue();
        String orderInfo = "Thanh toan don hang " + payment.getOrder().getOrderCode();
        String extraData = ""; // Có thể để rỗng dạng base64 nếu không truyền thêm gì

        // 1. Tạo chuỗi raw signature theo đúng thứ tự tài liệu MoMo v2 quy định:
        // accessKey=$accessKey&amount=$amount&extraData=$extraData&ipnUrl=$ipnUrl&orderId=$orderId&orderInfo=$orderInfo&partnerCode=$partnerCode&redirectUrl=$redirectUrl&requestId=$requestId&requestType=$requestType
        String rawSignature = String.format(
                "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                momoConfig.getAccessKey(),
                amount,
                extraData,
                momoConfig.getIpnUrl(),
                orderId,
                orderInfo,
                momoConfig.getPartnerCode(),
                momoConfig.getReturnUrl(),
                requestId,
                momoConfig.getRequestType()
        );

        // 2. Ký chữ ký số HMAC-SHA256
        String signature = MomoConfig.hmacSHA256(momoConfig.getSecretKey(), rawSignature);

        MomoCreatePaymentRequest momoRequest = MomoCreatePaymentRequest.builder()
                .partnerCode(momoConfig.getPartnerCode())
                .requestId(requestId)
                .amount(amount)
                .orderId(orderId)
                .orderInfo(orderInfo)
                .redirectUrl(momoConfig.getReturnUrl())
                .ipnUrl(momoConfig.getIpnUrl())
                .requestType(momoConfig.getRequestType())
                .extraData(extraData)
                .lang("vi")
                .signature(signature)
                .build();

        try {
            // 3. Gọi HTTP POST thật sang MoMo Gateway
            MomoCreatePaymentResponse momoResponse = restClient.post()
                    .uri(momoConfig.getEndpoint())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(momoRequest)
                    .retrieve()
                    .body(MomoCreatePaymentResponse.class);

            if (momoResponse == null || momoResponse.getResultCode() == null || momoResponse.getResultCode() != 0) {
                log.error("MoMo payment initiation failed: {}", momoResponse != null ? momoResponse.getMessage() : "null response");
                throw new AppException(ErrorCode.PAYMENT_GATEWAY_ERROR);
            }

            PaymentResponse response = paymentMapper.toResponse(payment);
            // MoMo trả về link checkout thật tại payUrl
            response.setPaymentUrl(momoResponse.getPayUrl());
            return response;

        } catch (Exception ex) {
            log.error("Error calling MoMo API: ", ex);
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }

    @Override
    public void processCallback(Payment payment, Map<String, String> params) {
        String accessKey = momoConfig.getAccessKey();
        String amount = params.get("amount");
        String extraData = params.getOrDefault("extraData", "");
        String message = params.get("message");
        String orderId = params.get("orderId");
        String orderInfo = params.get("orderInfo");
        String orderType = params.get("orderType");
        String partnerCode = params.get("partnerCode");
        String payType = params.get("payType");
        String requestId = params.get("requestId");
        String responseTime = params.get("responseTime");
        String resultCode = params.get("resultCode");
        String transId = params.get("transId");
        String receivedSignature = params.get("signature");

        // 1. Tạo chuỗi raw signature xác thực callback từ MoMo
        String rawSignature = String.format(
                "accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
                accessKey, amount, extraData, message, orderId, orderInfo, orderType, partnerCode, payType, requestId, responseTime, resultCode, transId
        );

        String computedSignature = MomoConfig.hmacSHA256(momoConfig.getSecretKey(), rawSignature);

        // 2. Xác thực chữ ký số chống giả mạo request
        if (receivedSignature == null || !computedSignature.equalsIgnoreCase(receivedSignature)) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setResponseMessage("Chữ ký MoMo không hợp lệ (Invalid Checksum)");
            throw new AppException(ErrorCode.PAYMENT_INVALID_CHECKSUM);
        }

        // 3. Kiểm tra kết quả thanh toán (resultCode == 0 là thành công)
        if ("0".equals(resultCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(transId);
            payment.setPaidAt(LocalDateTime.now(VN_ZONE));
            payment.setResponseMessage("Giao dịch MoMo thành công");
            payment.getOrder().setStatus(OrderStatus.PROCESSING);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setResponseMessage("Giao dịch MoMo thất bại: " + message);
        }
    }
}