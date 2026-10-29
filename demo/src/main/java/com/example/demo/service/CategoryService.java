package com.example.demo.service;

import com.example.demo.dto.request.CreateCategoryRequest;
import com.example.demo.dto.request.UpdateCategoryRequest;
import com.example.demo.dto.response.CategoryResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.utils.SlugUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CategoryService {
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    public List<CategoryResponse>getAllCategory(){
        return categoryRepository.findAll().stream().map(categoryMapper::toResponse).toList();
    }

    public CategoryResponse getCategoryBySlug(String slug){
        return categoryMapper.toResponse(categoryRepository.findBySlug(slug)
                .orElseThrow(()->new AppException(ErrorCode.CATEGORY_NOT_FOUND)));
    }

    public CategoryResponse createCategory(CreateCategoryRequest request){
        var category = categoryMapper.toEntity(request);
        var slug = SlugUtils.makeSlug(category.getName());
        if(categoryRepository.existsBySlug(slug))
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        category.setSlug(slug);
        categoryRepository.save(category);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        var category = categoryRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryMapper.updateToEntity(request, category);
        var slug = SlugUtils.makeSlug(category.getName());
        if(categoryRepository.existsBySlug(slug))
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        category.setSlug(slug);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }
    public void deleteCategory(Long id){
        var category = categoryRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
    }

    void updateCategoryStatus(Long id, String status){
        var category = categoryRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        category.setStatus(status);
        categoryRepository.save(category);
    }


}
