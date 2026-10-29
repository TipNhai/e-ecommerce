package com.example.demo.controller;

import com.example.demo.dto.request.CreateProductRequest;
import com.example.demo.dto.request.ProductFilterParam;
import com.example.demo.dto.request.UpdateProductRequest;
import com.example.demo.dto.response.*;
import com.example.demo.entity.User;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;

    @GetMapping
    public ApiResponse<PageResponse<ProductSummaryResponse>> getAllProducts(@Valid ProductFilterParam param , @AuthenticationPrincipal User user){
        String userId = (user != null) ? user.getId() : null;
        return ApiResponse.<PageResponse<ProductSummaryResponse>>builder()
                .result(productService.getAllProducts(param , userId))
                .build();
    }

    @GetMapping("/recent-searches")
    public ApiResponse<List<Object>> getRecentSearches(@AuthenticationPrincipal User user) {
        // Tránh lỗi null nếu chưa đăng nhập
        if (user == null) {
            return ApiResponse.<List<Object>>builder().result(Collections.emptyList()).build();
        }
        return ApiResponse.<List<Object>>builder()
                .result(productService.getRecentSearches(user.getId()))
                .build();
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductSummaryResponse> getProduct(@PathVariable Long productId){
        return ApiResponse.<ProductSummaryResponse>builder()
                .result(productService.getProduct(productId))
                .build();
    }

    @PostMapping
    public ApiResponse<ProductDetailResponse> createProduct(@RequestBody CreateProductRequest request){
        return ApiResponse.<ProductDetailResponse>builder()
                .result(productService.createProduct(request))
                .build();
    }
    @PutMapping("/{productId}")
    public ApiResponse<ProductDetailResponse> updateProduct(@PathVariable Long productId, @RequestBody UpdateProductRequest request,@AuthenticationPrincipal User user){
        String userId = (user != null) ? user.getId() : null;
        return ApiResponse.<ProductDetailResponse>builder()
                .result(productService.updateProduct(productId, request,userId))
                .build();
    }
    @DeleteMapping("/{productId}")
    public ApiResponse<String> deleteProduct(@PathVariable Long productId){
        productService.deleteProduct(productId);
        return ApiResponse.<String>builder()
                .result("Delete successfully")
                .build();
    }

    @PutMapping("/{productId}/quantity")
    public ApiResponse<String> updateQuantity(@PathVariable Long productId, @RequestParam int quantityDelta){
        productService.updateQuantity(productId, quantityDelta);
        return ApiResponse.<String>builder()
                .result("Update quantity successfully")
                .build();
    }

    @PostMapping("/{productId}/images")
    public ApiResponse<List<ProductImageResponse>> addProductImages(@PathVariable Long productId, @RequestBody List<String> imageUrls)
    {
        return ApiResponse.<List<ProductImageResponse>>builder()
                .result(productService.addProductImages(productId, imageUrls))
                .build();
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    public ApiResponse<String> deleteProductImage(@PathVariable Long productId, @PathVariable Long imageId){
        productService.deleteProductImage(productId, imageId);
        return ApiResponse.<String>builder()
                .result("Delete successfully")
                .build();
    }

    @PutMapping("/{productId}/thumbnail")
    public ApiResponse<String> setProductThumbnail(@PathVariable Long productId, @RequestParam Long imageId){
        productService.setProductThumbnail(productId, imageId);
        return ApiResponse.<String>builder()
                .result("Set thumbnail successfully")
                .build();
    }

}
