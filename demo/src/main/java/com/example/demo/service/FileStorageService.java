package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.response.FileResponse;
import com.example.demo.entity.FileMetadata;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.FileMetadataRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileStorageService {

    Cloudinary cloudinary;
    FileMetadataRepository fileMetadataRepository;

    static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/jpg"
    );
    static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB

    @Transactional
    public FileResponse storeFile(MultipartFile file) {
        validateFile(file);

        try {
            // Đẩy trực tiếp file lên Cloudinary vào thư mục "ecommerce"
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "ecommerce",
                            "resource_type", "auto"
                    )
            );

            String publicId = (String) uploadResult.get("public_id");
            String secureUrl = (String) uploadResult.get("secure_url");
            Long bytes = ((Number) uploadResult.get("bytes")).longValue();

            // Lưu metadata vào DB
            FileMetadata metadata = FileMetadata.builder()
                    .publicId(publicId)
                    .originalFilename(Objects.requireNonNull(file.getOriginalFilename()))
                    .contentType(file.getContentType())
                    .size(bytes)
                    .fileUrl(secureUrl)
                    .build();

            FileMetadata saved = fileMetadataRepository.save(metadata);

            return FileResponse.builder()
                    .id(saved.getId())
                    .fileCode(saved.getPublicId())
                    .originalFilename(saved.getOriginalFilename())
                    .fileUrl(saved.getFileUrl())
                    .contentType(saved.getContentType())
                    .size(saved.getSize())
                    .createdAt(saved.getCreatedAt())
                    .build();

        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Transactional
    public List<FileResponse> storeMultipleFiles(List<MultipartFile> files) {
        return files.stream().map(this::storeFile).toList();
    }

    // Hàm tiện ích: Xóa file trên Cloudinary khi cần
    @Transactional
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            fileMetadataRepository.findByPublicId(publicId).ifPresent(fileMetadataRepository::delete);
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_EMPTY);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
    }
}