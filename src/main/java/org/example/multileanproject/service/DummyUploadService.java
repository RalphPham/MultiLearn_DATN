//package org.example.multileanproject.service;
//
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//@Service
//// 🔥 THÊM DÒNG NÀY: Chỉ kích hoạt khi provider là 'dummy'
//// Nếu thiếu cấu hình 'upload.provider', nó cũng sẽ không chạy (matchIfMissing = false)
//@ConditionalOnProperty(name = "upload.provider", havingValue = "dummy")
//public class DummyUploadService implements UploadService {
//    @Override
//    public String uploadImage(MultipartFile file) {
//        return "UPLOAD IMAGE DISABLED (DUMMY MODE)";
//    }
//
//    @Override
//    public String uploadVideo(MultipartFile file) {
//        return "UPLOAD VIDEO DISABLED (DUMMY MODE)";
//    }
//
//    @Override
//    public String uploadFile(MultipartFile file) {
//        return "UPLOAD FILE DISABLED (DUMMY MODE)";
//    }
//}