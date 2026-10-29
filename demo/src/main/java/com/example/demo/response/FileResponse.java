package com.example.demo.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileResponse {
    Long id;
    String fileCode;
    String originalFilename;
    String fileUrl;
    String contentType;
    Long size;
    LocalDateTime createdAt;
}