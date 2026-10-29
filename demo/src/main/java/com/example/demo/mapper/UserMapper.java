package com.example.demo.mapper;

import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.request.UpdateProfileRequest;
import com.example.demo.dto.response.UserProfileResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User registerToEntity(RegisterRequest request);
    @Mapping(target = "roles", ignore = true)
    UserResponse toResponse(User user);

    UserProfileResponse toProfileResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
        // không map những field bị null và nên dùng cho @MappingTarget
    void updateToEntity(UpdateProfileRequest request, @MappingTarget User user);
}
