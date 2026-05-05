package org.example.multileanproject.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioUploadService implements UploadService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String endpoint;

    private String upload(MultipartFile file, String folder) {
        try {
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String fileName = folder + UUID.randomUUID() + "_" + originalName;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return endpoint + "/" + bucket + "/" + fileName;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadImage(MultipartFile file) {
        return upload(file, "images/");
    }

    @Override
    public String uploadVideo(MultipartFile file) {
        return upload(file, "videos/");
    }

    @Override
    public String uploadFile(MultipartFile file) {
        return upload(file, "documents/");
    }

    public String getPresignedUrl(String objectKey, int minutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(minutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo URL truy cập: " + e.getMessage(), e);
        }
    }

    public String extractObjectKey(String fullUrl) {
        String prefix = endpoint + "/" + bucket + "/";
        if (fullUrl != null && fullUrl.startsWith(prefix)) {
            return fullUrl.substring(prefix.length());
        }
        return fullUrl;
    }
}