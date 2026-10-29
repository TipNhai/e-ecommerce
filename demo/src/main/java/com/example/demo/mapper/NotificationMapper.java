package com.example.demo.mapper;

import com.example.demo.dto.response.NotificationResponse;
import com.example.demo.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);
}
