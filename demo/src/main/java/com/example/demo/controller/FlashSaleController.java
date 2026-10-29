package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flashsale")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @PostMapping("/reset")
    public ApiResponse<String> resetStock(@RequestParam int newStock) {
        flashSaleService.resetStock(newStock);
        return ApiResponse.<String>builder()
                .result("Stock reset to " + newStock)
                .build();
    }

    @PostMapping("/buy-without-lock")
    public ApiResponse<String> buyWithoutLock(@RequestParam String productId, @RequestParam String userId) {
        flashSaleService.buyWithoutLock(productId, userId);
        return ApiResponse.<String>builder()
                .result("Purchase request submitted without lock")
                .build();
    }

    @PostMapping("/buy-with-lock")
    public ApiResponse<String> buyWithLock(@RequestParam String productId, @RequestParam String userId) {
        flashSaleService.buyWithLock(productId, userId);
        return ApiResponse.<String>builder()
                .result("Purchase request submitted with lock")
                .build();
    }

    @PostMapping("/test")
    public ApiResponse<String> runConcurrentTest(
            @RequestParam String productId, 
            @RequestParam boolean useLock, 
            @RequestParam int totalRequest) throws InterruptedException {
        flashSaleService.runConcurrentTest(productId, useLock, totalRequest);
        return ApiResponse.<String>builder()
                .result("Concurrent test completed for " + totalRequest + " requests. Check logs for details.")
                .build();
    }
}
