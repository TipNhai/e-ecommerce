package com.example.demo.dto.request.momo;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MomoCreatePaymentRequest {
    String partnerCode;
    String requestId;
    Long amount;
    String orderId;
    String orderInfo;
    String redirectUrl;
    String ipnUrl;
    String requestType;
    String extraData;
    String lang;
    String signature;
}