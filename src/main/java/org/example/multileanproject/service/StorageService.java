package org.example.multileanproject.service;

import org.springframework.web.multipart.MultipartFile;
public interface StorageService {
    String store(MultipartFile file);
    void delete(String filename);
    boolean exists(String filename);
}
