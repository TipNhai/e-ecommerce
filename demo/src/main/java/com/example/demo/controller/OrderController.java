package com.example.demo.controller;

import com.example.demo.dto.request.CreateOrderRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.OrderResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    OrderService orderService;
    @PostMapping("/{userId}")
    public ApiResponse<OrderResponse> createOrder(@PathVariable String userId,@RequestBody CreateOrderRequest request){
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createOrder(userId, request))
                .build();
    }

    @GetMapping("/{userId}/{orderId}")
    public ApiResponse<OrderResponse> getOrderDetail(@PathVariable String userId, @PathVariable Long orderId){
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrderDetail(userId, orderId))
                .build();
    }

    @PutMapping("/{userId}/{orderId}")
    public ApiResponse<OrderResponse> cancelOrder(@PathVariable String userId, @PathVariable Long orderId){
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.cancelOrder(userId, orderId))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<OrderResponse> updateOrderStatus(@PathVariable Long id, @RequestParam String status){
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.updateOrderStatus(id, OrderStatus.valueOf(status)))
                .build();
    }
}
