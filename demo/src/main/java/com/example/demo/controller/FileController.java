package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.response.FileResponse;
import com.example.demo.service.FileStorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {

    FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                ApiResponse.<FileResponse>builder()
                        .message("Upload file lên Cloudinary thành công")
                        .result(fileStorageService.storeFile(file))
                        .build()
        );
    }

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<FileResponse>>> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files
    ) {
        return ResponseEntity.ok(
                ApiResponse.<List<FileResponse>>builder()
                        .message("Upload nhiều file thành công")
                        .result(fileStorageService.storeMultipleFiles(files))
                        .build()
        );
    }
}