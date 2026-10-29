package com.example.demo.payment.factory;

import com.example.demo.enums.PaymentMethod;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.payment.adapter.PaymentGatewayAdapter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentGatewayFactory {

    private final Map<PaymentMethod, PaymentGatewayAdapter> adapters;

    // Spring Boot tự động gom toàn bộ các class implement PaymentGatewayAdapter
    public PaymentGatewayFactory(List<PaymentGatewayAdapter> adapterList) {
        this.adapters = adapterList.stream()
                .collect(Collectors.toUnmodifiableMap(
                        PaymentGatewayAdapter::getPaymentMethod,
                        Function.identity()
                ));
    }

    public PaymentGatewayAdapter getAdapter(PaymentMethod method) {
        return Optional.ofNullable(adapters.get(method))
                .orElseThrow(() -> new AppException(ErrorCode.UNSUPPORTED_PAYMENT_METHOD));
    }
}