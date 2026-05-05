package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final StudentRepository studentRepository;

    // Lấy userId từ JWT — không tin client tự khai báo
    private Long getCurrentStudentId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"))
                .getId();
    }

    // Giữ path /user/{studentId} để frontend không cần đổi URL, nhưng bỏ qua giá trị truyền lên
    @GetMapping("/user/{studentId}")
    public ResponseEntity<?> getWishlist(@PathVariable Long studentId) {
        return ResponseEntity.ok(wishlistService.getWishlist(getCurrentStudentId()));
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestParam Long courseId) {
        wishlistService.addToWishlist(getCurrentStudentId(), courseId);
        return ResponseEntity.ok("Đã thêm vào danh sách yêu thích");
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> remove(@RequestParam Long courseId) {
        wishlistService.removeFromWishlist(getCurrentStudentId(), courseId);
        return ResponseEntity.ok("Đã xóa khỏi danh sách yêu thích");
    }
}
