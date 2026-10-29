package com.example.demo.mapper;

import com.example.demo.dto.response.ProductImageResponse;
import com.example.demo.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    @Mapping(source = "product.id", target = "productId")
    ProductImageResponse toProductImageResponse(ProductImage productImage);
}
