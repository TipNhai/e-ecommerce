package com.example.demo.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateReviewRequest {

    @NotNull(message = "Product ID is required")
    Long productId;

    @NotNull(message = "Product ID is required")
    Long orderId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    Integer rating;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    String comment;

    // Danh sách URL ảnh sau khi upload lên S3/Cloudinary (tối đa 5 ảnh)
    @Size(max = 5, message = "You can upload up to 5 images per review")
    List<String> imageUrls;
}
