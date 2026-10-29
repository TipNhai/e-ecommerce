package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "files")
public class FileMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String publicId; // Định danh trên Cloudinary (dùng để xóa ảnh)

    @Column(nullable = false)
    String originalFilename;

    @Column(nullable = false)
    String contentType;

    @Column(nullable = false)
    Long size;

    @Column(nullable = false, length = 500)
    String fileUrl; // Đường dẫn https://res.cloudinary.com/...

    @CreationTimestamp
    LocalDateTime createdAt;
}