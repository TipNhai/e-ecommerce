package com.example.demo.service;

import com.example.demo.dto.request.CreateBrandRequest;
import com.example.demo.dto.request.UpdateBrandRequest;
import com.example.demo.dto.response.BrandResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.BrandMapper;
import com.example.demo.repository.BrandRepository;
import com.example.demo.utils.SlugUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrandService {
    BrandRepository brandRepository;
    BrandMapper brandMapper;

    public List<BrandResponse> getAllBrand(){
        return brandRepository.findAll().stream().map(brandMapper::toResponse).toList();
    }

    public BrandResponse getBrandBySlug(String slug){
        return brandMapper.toResponse(brandRepository.findBySlug(slug)
                .orElseThrow(()->new AppException(ErrorCode.BRAND_NOT_FOUND)));
    }

    public BrandResponse createBrand(CreateBrandRequest request){
        var brand = brandMapper.toEntity(request);
        var slug = SlugUtils.makeSlug(brand.getName());
        if(brandRepository.existsBySlug(slug))
            throw new AppException(ErrorCode.BRAND_EXISTED);
        brand.setSlug(slug);
        brandRepository.save(brand);
        return brandMapper.toResponse(brand);
    }
    public BrandResponse updateBrand(Long id, UpdateBrandRequest request){
        var brand = brandRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.BRAND_NOT_FOUND));
        brandMapper.updateToEntity(request, brand);
        var slug = SlugUtils.makeSlug(brand.getName());
        if(brandRepository.existsBySlug(slug))
            throw new AppException(ErrorCode.BRAND_EXISTED);
        brand.setSlug(slug);
        brandRepository.save(brand);
        return brandMapper.toResponse(brand);

    }
    public void deleteBrand(Long id){
        var brand = brandRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.BRAND_NOT_FOUND));
        brandRepository.delete(brand);
    }
}
