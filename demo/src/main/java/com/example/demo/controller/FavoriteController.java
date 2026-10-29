package com.example.demo.controller;

import com.example.demo.dto.request.AddFavoriteRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.FavoriteResponse;
import com.example.demo.service.FavoriteService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FavoriteController {
    FavoriteService favoriteService;

    @GetMapping("/{userId}")
    public ApiResponse<FavoriteResponse> getMyFavorite(@PathVariable String userId){
        return ApiResponse.<FavoriteResponse>builder()
                .result(favoriteService.getMyFavorite(userId))
                .build();
    }

    @PostMapping("/{userId}")
    public ApiResponse<FavoriteResponse> addProductToFavorite(@PathVariable String userId, @RequestBody AddFavoriteRequest request){
        return ApiResponse.<FavoriteResponse>builder()
                .result(favoriteService.addProductToFavorite(userId, request))
                .build();
    }

    @DeleteMapping("/{userId}/products/{productId}")
    public ApiResponse<FavoriteResponse> removeProductFromFavorite(@PathVariable String userId, @PathVariable Long productId){
        return ApiResponse.<FavoriteResponse>builder()
                .result(favoriteService.removeProductFromFavorite(userId, productId))
                .build();
    }

    @GetMapping("/{userId}/products/{productId}")
    public ApiResponse<Boolean> isProductInFavorite(@PathVariable String userId, @PathVariable Long productId){
        return ApiResponse.<Boolean>builder()
                .result(favoriteService.isProductInFavorite(userId, productId))
                .build();
    }
}
