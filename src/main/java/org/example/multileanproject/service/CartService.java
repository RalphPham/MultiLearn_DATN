package org.example.multileanproject.service;

import org.example.multileanproject.dto.CartItemResponse;
import java.util.List;

public interface CartService {
    void addToCart(Long userId, Long courseId);

    void removeFromCart(Long cartItemId);

    List<CartItemResponse> getCartItems(Long studentId);
}