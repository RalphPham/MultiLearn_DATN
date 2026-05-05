//package org.example.multileanproject.service;
//
//import com.cloudinary.Cloudinary;
//import com.cloudinary.utils.ObjectUtils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//@ConditionalOnProperty(name = "upload.provider", havingValue = "cloudinary")
//public class CloudinaryUploadService implements UploadService {
//
//    private final Cloudinary cloudinary;
//
//    private String uploadToCloudinary(MultipartFile file, String folder, String resourceType) {
//        try {
//            Map params = ObjectUtils.asMap(
//                    "folder", "multilearn/" + folder,
//                    "resource_type", resourceType
//            );
//            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
//            return uploadResult.get("secure_url").toString();
//        } catch (Exception e) {
//            throw new RuntimeException("Cloudinary upload failed: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public String uploadImage(MultipartFile file) {
//        return uploadToCloudinary(file, "images", "image");
//    }
//
//    @Override
//    public String uploadVideo(MultipartFile file) {
//        return uploadToCloudinary(file, "videos", "video");
//    }
//
//    // 🔥 [MỚI THÊM] Triển khai phương thức thiếu cho tài liệu PDF/File
//    @Override
//    public String uploadFile(MultipartFile file) {
//        // resource_type là "raw" dành cho các tệp như PDF, Zip, Docx
//        return uploadToCloudinary(file, "documents", "raw");
//    }
//}