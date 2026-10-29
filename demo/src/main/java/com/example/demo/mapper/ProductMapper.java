package com.example.demo.mapper;

import com.example.demo.dto.request.CreateProductRequest;
import com.example.demo.dto.request.UpdateProductRequest;
import com.example.demo.dto.response.ProductDetailResponse;
import com.example.demo.dto.response.ProductSummaryResponse;
import com.example.demo.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    //Mapstruct sẽ tự động map theo tên field không cần (brand.getId())
    @Mapping(source = "brand.id" ,target = "brandId")
    @Mapping(source = "brand.name" , target = "brandName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductSummaryResponse toSummaryResponse(Product entity);

    ProductDetailResponse toDetailResponse(Product entity);

    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateToEntity(UpdateProductRequest request, @MappingTarget Product product);

    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product createToEntity(CreateProductRequest request);
}
