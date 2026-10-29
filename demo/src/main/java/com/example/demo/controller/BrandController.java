package com.example.demo.controller;

import com.example.demo.dto.request.CreateBrandRequest;
import com.example.demo.dto.request.UpdateBrandRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.BrandResponse;
import com.example.demo.service.BrandService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrandController {
    BrandService brandService;

    @PostMapping
    public ApiResponse<BrandResponse>createBrand(@RequestBody CreateBrandRequest request){
        return ApiResponse.<BrandResponse>builder()
                .result(brandService.createBrand(request))
                .build();
    }

    @GetMapping("/{slug}")
    public ApiResponse<BrandResponse>getBrandBySlug(@PathVariable String slug){
        return ApiResponse.<BrandResponse>builder()
                .result(brandService.getBrandBySlug(slug))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<BrandResponse>updateBrand(@PathVariable Long id, @RequestBody UpdateBrandRequest request){
        return ApiResponse.<BrandResponse>builder()
                .result(brandService.updateBrand(id,request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<BrandResponse>>getAllBrands(){
        return ApiResponse.<List<BrandResponse>>builder()
                .result(brandService.getAllBrand())
                .build();
    }
    @DeleteMapping("/{id}")
    public ApiResponse<Void>deleteBrand(@PathVariable Long id){
        brandService.deleteBrand(id);
        return ApiResponse.<Void>builder().build();
    }
}
