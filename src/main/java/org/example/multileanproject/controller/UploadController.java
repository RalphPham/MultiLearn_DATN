package org.example.multileanproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
@Tag(name = "Upload", description = "API quản lý file upload")
public class UploadController {

    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    // Đường dẫn thư mục lưu ảnh
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public UploadController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Không thể tạo thư mục upload.", ex);
        }
    }

    // API Upload Avatar (Đã thêm bảo mật chặn file rác)
    @PostMapping("/avatar")
    @Operation(summary = "Upload Avatar", description = "Upload ảnh đại diện cho user (Chỉ nhận file ảnh)")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 1. CHECK RỖNG
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File không được để trống");
            }

            // 2. CHECK DUNG LƯỢNG (Chặn file > 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("File quá lớn! Vui lòng chọn ảnh dưới 5MB.");
            }

            // 3. CHECK ĐUÔI FILE (EXTENSION)
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null) return ResponseEntity.badRequest().body("Tên file bị lỗi");

            String lowerCaseName = originalFileName.toLowerCase();
            if (!lowerCaseName.endsWith(".jpg") &&
                    !lowerCaseName.endsWith(".jpeg") &&
                    !lowerCaseName.endsWith(".png") &&
                    !lowerCaseName.endsWith(".webp")) {
                return ResponseEntity.badRequest().body("Chỉ chấp nhận file ảnh (.jpg, .png, .jpeg, .webp)");
            }

            // 4. CHECK MIME TYPE (Chặn đổi đuôi .exe thành .jpg)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("File không phải là định dạng ảnh hợp lệ");
            }

            // --- LƯU FILE ---

            // Tạo tên file ngẫu nhiên (UUID) để tránh trùng lặp
            String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

            // Đường dẫn đích
            Path targetLocation = this.fileStorageLocation.resolve(fileName);

            // Lưu file
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Trả về URL
            String fileUrl = backendUrl + "/uploads/" + fileName;

            return ResponseEntity.ok(fileUrl);

        } catch (IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi khi upload file: " + ex.getMessage());
        }
    }
}