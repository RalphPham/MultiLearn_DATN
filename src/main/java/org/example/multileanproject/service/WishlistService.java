package org.example.multileanproject.service;

import org.example.multileanproject.dto.WishlistResponse; // Nhớ Import DTO
import java.util.List;

public interface WishlistService {
    // ✅ CHỈ GIỮ LẠI DÒNG NÀY (Trả về DTO)
    List<WishlistResponse> getWishlist(Long studentId);

    // ❌ XÓA DÒNG NÀY ĐI: List<Wishlist> getWishlist(Long studentId);

    void addToWishlist(Long studentId, Long courseId);
    void removeFromWishlist(Long studentId, Long courseId);
    void toggleWishlist(Long studentId, Long courseId);
}