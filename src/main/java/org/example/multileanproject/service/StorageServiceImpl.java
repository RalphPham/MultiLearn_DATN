package org.example.multileanproject.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

// Import Logger thủ công
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class StorageServiceImpl implements StorageService {

    // 1. Khai báo Logger thủ công (Thay thế @Slf4j)
    private static final Logger log = LoggerFactory.getLogger(StorageServiceImpl.class);

    // Thư mục lưu trữ file
    private final Path uploadDir = Paths.get("uploads");

    // Danh sách file extensions được phép upload
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "svg"
    );

    private static final List<String> ALLOWED_VIDEO_EXTENSIONS = Arrays.asList(
            "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm"
    );

    private static final List<String> ALLOWED_DOCUMENT_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx"
    );

    // Kích thước file tối đa (bytes)
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;      // 10MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024;     // 100MB
    private static final long MAX_DOCUMENT_SIZE = 20 * 1024 * 1024;   // 20MB

    public StorageServiceImpl() {
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("✅ Đã tạo thư mục uploads tại: {}", uploadDir.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("❌ Không thể tạo thư mục uploads", e);
            throw new RuntimeException("Không thể khởi tạo storage", e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        log.info("📤 Bắt đầu upload file: {}", file.getOriginalFilename());

        // ===== VALIDATE FILE =====

        // 1. Kiểm tra file không rỗng
        if (file.isEmpty()) {
            log.error("❌ File rỗng");
            throw new RuntimeException("File không được rỗng");
        }

        // 2. Lấy tên file gốc và extension
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);

        log.info("📄 File gốc: {}, Extension: {}", originalFilename, fileExtension);

        // 3. Validate file extension
        validateFileExtension(fileExtension);

        // 4. Validate file size
        validateFileSize(file.getSize(), fileExtension);

        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        // ===== LƯU FILE =====

        try {
            // Đường dẫn đầy đủ của file
            Path targetLocation = uploadDir.resolve(uniqueFilename);

            log.info("💾 Lưu file vào: {}", targetLocation.toAbsolutePath());

            // Copy file từ InputStream vào đích
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("✅ Upload thành công: {}", uniqueFilename);

            return "/uploads/" + uniqueFilename;

        } catch (IOException e) {
            log.error("❌ Lỗi khi lưu file: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lưu file: " + originalFilename, e);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            // Lấy tên file từ path (nếu có)
            // Ví dụ: /uploads/abc.jpg -> abc.jpg
            String cleanFilename = filename.replace("/uploads/", "");

            Path fileToDelete = uploadDir.resolve(cleanFilename);

            if (Files.exists(fileToDelete)) {
                Files.delete(fileToDelete);
                log.info("🗑️ Đã xóa file: {}", cleanFilename);
            } else {
                log.warn("⚠️ File không tồn tại: {}", cleanFilename);
            }

        } catch (IOException e) {
            log.error("❌ Lỗi khi xóa file: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể xóa file: " + filename, e);
        }
    }

    @Override
    public boolean exists(String filename) {
        String cleanFilename = filename.replace("/uploads/", "");
        Path filePath = uploadDir.resolve(cleanFilename);
        return Files.exists(filePath);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new RuntimeException("File không có extension");
        }

        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private void validateFileExtension(String extension) {
        boolean isImage = ALLOWED_IMAGE_EXTENSIONS.contains(extension);
        boolean isVideo = ALLOWED_VIDEO_EXTENSIONS.contains(extension);
        boolean isDocument = ALLOWED_DOCUMENT_EXTENSIONS.contains(extension);

        if (!isImage && !isVideo && !isDocument) {
            log.error("❌ File extension không hợp lệ: {}", extension);
            throw new RuntimeException(
                    "File extension không được hỗ trợ: " + extension + ". " +
                            "Chỉ chấp nhận: " +
                            "Ảnh (" + String.join(", ", ALLOWED_IMAGE_EXTENSIONS) + "), " +
                            "Video (" + String.join(", ", ALLOWED_VIDEO_EXTENSIONS) + "), " +
                            "Document (" + String.join(", ", ALLOWED_DOCUMENT_EXTENSIONS) + ")"
            );
        }
    }

    private void validateFileSize(long fileSize, String extension) {
        long maxSize;
        String fileType;

        if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            maxSize = MAX_IMAGE_SIZE;
            fileType = "ảnh";
        } else if (ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            maxSize = MAX_VIDEO_SIZE;
            fileType = "video";
        } else if (ALLOWED_DOCUMENT_EXTENSIONS.contains(extension)) {
            maxSize = MAX_DOCUMENT_SIZE;
            fileType = "tài liệu";
        } else {
            throw new RuntimeException("File extension không hợp lệ");
        }

        if (fileSize > maxSize) {
            String maxSizeMB = String.format("%.2f", maxSize / (1024.0 * 1024.0));
            String currentSizeMB = String.format("%.2f", fileSize / (1024.0 * 1024.0));

            log.error("❌ File quá lớn: {} MB (tối đa {} MB cho {})",
                    currentSizeMB, maxSizeMB, fileType);

            throw new RuntimeException(
                    String.format("File %s quá lớn (%s MB). Kích thước tối đa: %s MB",
                            fileType, currentSizeMB, maxSizeMB)
            );
        }
    }
}