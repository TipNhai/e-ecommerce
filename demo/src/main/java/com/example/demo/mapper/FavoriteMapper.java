package com.example.demo.mapper;

import com.example.demo.dto.response.FavoriteResponse;
import com.example.demo.entity.Favorite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FavoriteMapper {
    @Mapping(target = "userId" , source = "user.id")
    @Mapping(target = "products", source = "products")
    FavoriteResponse toResponse(Favorite entity);
}
