package com.example.demo.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {

    Long id;
    String userId;
    String username; // hoặc fullName
    String userAvatar;
    Long productId;
    Integer rating;
    String comment;
    List<String> images;
    LocalDateTime createdAt;
}