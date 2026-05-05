package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.CartItemResponse;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final StudentRepository studentRepository;

    // Lấy userId từ JWT — không tin client tự khai báo
    private Long getCurrentStudentId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"))
                .getId();
    }

    @GetMapping
    public ResponseEntity<?> getCart() {
        try {
            List<CartItemResponse> cartItems = cartService.getCartItems(getCurrentStudentId());
            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi Server: " + e.getMessage());
        }
    }

    // Giữ path /{studentId} để frontend không cần đổi URL, nhưng bỏ qua giá trị truyền lên
    @GetMapping("/{studentId}")
    public ResponseEntity<?> getCartByPath(@PathVariable Long studentId) {
        return getCart();
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestParam Long courseId) {
        try {
            cartService.addToCart(getCurrentStudentId(), courseId);
            return ResponseEntity.ok("Thêm thành công");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("đã có trong giỏ")) {
                return ResponseEntity.status(409).body("Khóa học này đã có trong giỏ của bạn rồi!");
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi hệ thống");
        }
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long cartItemId) {
        try {
            cartService.removeFromCart(cartItemId);
            return ResponseEntity.ok("Đã xóa sản phẩm");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xóa: " + e.getMessage());
        }
    }
}
