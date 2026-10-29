package com.example.demo.controller;

import com.example.demo.dto.request.AddToCartRequest;
import com.example.demo.dto.request.UpdateCartItemRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.CartResponse;
import com.example.demo.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {
    CartService cartService;

    @GetMapping("/{userId}")
    public ApiResponse<CartResponse> getMyCart(@PathVariable String userId){
        return ApiResponse.<CartResponse>builder()
                .result(cartService.getMyCart(userId))
                .build();
    }

    @PostMapping("/{userId}")
    public ApiResponse<CartResponse> addToCart(@PathVariable String userId, @RequestBody AddToCartRequest request){
        return ApiResponse.<CartResponse>builder()
                .result(cartService.addToCart(userId, request))
                .build();
    }

    @PutMapping("/{userId}/items/{cartItemId}")
    public ApiResponse<CartResponse> updateCartItemQuantity(@PathVariable String userId, @PathVariable Long cartItemId,
                                                            @RequestBody UpdateCartItemRequest request){
        return ApiResponse.<CartResponse>builder()
                .result(cartService.updateCartItemQuantity(userId, cartItemId, request))
                .build();
    }
    @DeleteMapping("/{userId}/items/{cartItemId}")
    public ApiResponse<CartResponse> removeCartItem(@PathVariable String userId, @PathVariable Long cartItemId){
        return ApiResponse.<CartResponse>builder()
                .result(cartService.removeCartItem(userId, cartItemId))
                .build();

    }

    @DeleteMapping("/{userId}/clear")
    public ApiResponse<String> clearCart(@PathVariable String userId){
        cartService.clearCart(userId);
        return ApiResponse.<String>builder()
                .result("Clear successfully")
                .build();
    }
}
