package org.example.multileanproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.CartItemResponse;
import org.example.multileanproject.entity.Cart;
import org.example.multileanproject.entity.CartItem;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.CourseStatus;
import org.example.multileanproject.entity.OrderStatus;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.entity.SaleCampaignItem;
import org.example.multileanproject.repository.CartItemRepository;
import org.example.multileanproject.repository.CartRepository;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.OrderRepository;
import org.example.multileanproject.repository.RefundRequestRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.repository.SaleCampaignItemRepository;
import org.example.multileanproject.service.CartService;
import org.example.multileanproject.service.EnrollmentService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final OrderRepository orderRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final SaleCampaignItemRepository campaignItemRepository;
    private final EnrollmentService enrollmentService;

    @Override
    @Transactional
    public void addToCart(Long studentId, Long courseId) {
        Student student = getValidatedCurrentStudent(studentId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoá học"));

        validateCourseCanBePurchasedByStudent(course, student);

        Cart cart = cartRepository.findByStudentId(student.getId());
        if (cart == null) {
            cart = new Cart();
            cart.setStudent(student);
            cart = cartRepository.save(cart);
        }

        if (cartItemRepository.existsByCart_Student_IdAndCourse_Id(student.getId(), courseId)) {
            throw new RuntimeException("Khóa học này đã có trong giỏ hàng rồi!");
        }

        if (hasActiveOwnership(student.getId(), courseId)) {
            throw new RuntimeException("Bạn đã sở hữu khoá học này rồi!");
        }

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setCourse(course);

        BigDecimal finalPrice = getDynamicSellPrice(course);
        cartItem.setPrice(finalPrice);

        cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional
    public List<CartItemResponse> getCartItems(Long studentId) {
        Student student = getValidatedCurrentStudent(studentId);

        Cart cart = cartRepository.findByStudentId(student.getId());
        if (cart == null || cart.getCartItems() == null) {
            return new ArrayList<>();
        }

        List<CartItemResponse> responses = new ArrayList<>();
        List<CartItem> staleItems = new ArrayList<>();

        for (CartItem item : cart.getCartItems()) {
            Course course = item.getCourse();
            if (course == null) {
                staleItems.add(item);
                continue;
            }

            try {
                validateCourseCanBePurchasedByStudent(course, student);
            } catch (RuntimeException ex) {
                staleItems.add(item);
                continue;
            }

            if (hasActiveOwnership(student.getId(), course.getId())) {
                staleItems.add(item);
                continue;
            }

            BigDecimal currentPrice = getDynamicSellPrice(course);

            String instructorName = null;
            try {
                if (course.getInstructor() != null) {
                    instructorName = course.getInstructor().getFullName();
                }
            } catch (Exception ignored) {}

            responses.add(CartItemResponse.builder()
                    .id(item.getId())
                    .courseId(course.getId())
                    .courseTitle(course.getTitle())
                    .courseSlug(course.getSlug())
                    .courseThumbnail(course.getThumbnail())
                    .coursePrice(currentPrice)
                    .instructorName(instructorName)
                    .totalDuration(course.getTotalDuration())
                    .totalLessons(course.getTotalLessons())
                    .build());
        }

        if (!staleItems.isEmpty()) {
            cart.getCartItems().removeAll(staleItems);
            cartItemRepository.deleteAll(staleItems);
            cartItemRepository.flush();
        }

        return responses;
    }

    @Override
    @Transactional
    public void removeFromCart(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại (ID: " + cartItemId + ")"));

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (cartItem.getCart() == null
                || cartItem.getCart().getStudent() == null
                || cartItem.getCart().getStudent().getEmail() == null
                || !currentEmail.equalsIgnoreCase(cartItem.getCart().getStudent().getEmail())) {
            throw new RuntimeException("Bạn không có quyền xóa sản phẩm này khỏi giỏ hàng.");
        }

        cartItemRepository.delete(cartItem);
    }

    private Student getValidatedCurrentStudent(Long studentId) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (student.getEmail() == null || !currentEmail.equalsIgnoreCase(student.getEmail())) {
            throw new RuntimeException("Bạn không có quyền thao tác với giỏ hàng này.");
        }

        return student;
    }

    private void validateCourseCanBePurchasedByStudent(Course course, Student student) {
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new RuntimeException("Khoá học hiện không mở bán.");
        }

        if (course.getInstructor() != null
                && course.getInstructor().getUser() != null
                && !course.getInstructor().getUser().isActive()) {
            throw new RuntimeException("Giảng viên của khoá học hiện đang bị khoá. Tạm thời không thể thêm vào giỏ hàng.");
        }

        if (course.getInstructor() != null
                && course.getInstructor().getUser() != null
                && course.getInstructor().getUser().getEmail() != null
                && student.getEmail() != null
                && student.getEmail().equalsIgnoreCase(course.getInstructor().getUser().getEmail())) {
            throw new RuntimeException("Bạn không thể thêm khoá học của chính mình vào giỏ hàng.");
        }
    }

    private BigDecimal getDynamicSellPrice(Course course) {
        Optional<SaleCampaignItem> activeSaleOpt = campaignItemRepository.findActiveSaleForCourse(course.getId(), LocalDateTime.now());

        if (activeSaleOpt.isPresent()) {
            return activeSaleOpt.get().getPromotionalPrice();
        }

        if (course.getSalePrice() != null && course.getSalePrice().compareTo(BigDecimal.ZERO) > 0) {
            return course.getSalePrice();
        }
        return course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO;
    }

    private boolean hasActiveOwnership(Long studentId, Long courseId) {
        if (enrollmentService.isStudentEnrolled(studentId, courseId)) {
            return true;
        }

        boolean alreadyOwned = orderRepository.existsByStudentIdAndCourseIdAndStatus(
                studentId,
                courseId,
                OrderStatus.COMPLETED
        );
        if (!alreadyOwned) {
            return false;
        }

        return refundRequestRepository.countApprovedRefundByStudentAndCourse(studentId, courseId) <= 0;
    }
}
