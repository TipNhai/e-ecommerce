package com.example.demo.mapper;

import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.entity.Review;
import com.example.demo.entity.ReviewImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "userId" , source = "user.id")
    @Mapping(target = "username" , source = "user.username")
    @Mapping(target = "userAvatar" , source = "user.avatar")
    @Mapping(target = "productId" , source = "product.id")
    @Mapping(target = "images", source = "images", qualifiedByName = "mapImagesToUrls")
    ReviewResponse toResponse(Review review);

    @Named("mapImagesToUrls")
    default List<String> mapImagesToUrls(List<ReviewImage>images){
        if(images.isEmpty()|| images == null){
            return Collections.emptyList();
        }
        return images.stream().map(ReviewImage::getImageUrl).toList();
    }
}
