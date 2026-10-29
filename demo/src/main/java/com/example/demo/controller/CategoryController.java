package com.example.demo.controller;

import com.example.demo.dto.request.CreateCategoryRequest;
import com.example.demo.dto.request.UpdateCategoryRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.CategoryResponse;
import com.example.demo.service.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    CategoryService categoryService;

    @PostMapping
    public ApiResponse<CategoryResponse>createCategory(@RequestBody CreateCategoryRequest request){
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.createCategory(request))
                .build();
    }

    @GetMapping("/{slug}")
    public ApiResponse<CategoryResponse>getCategoryBySlug(@PathVariable String slug){
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getCategoryBySlug(slug))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse>updateCategory(@PathVariable Long id,@RequestBody UpdateCategoryRequest request){
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.updateCategory(id,request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>>getAllCategory() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAllCategory())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void>deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ApiResponse.<Void>builder().build();
    }
}
