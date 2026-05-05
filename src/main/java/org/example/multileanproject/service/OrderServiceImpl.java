package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.CheckoutPreviewResponseDTO;
import org.example.multileanproject.dto.OrderHistoryItemDTO;
import org.example.multileanproject.dto.OrderHistoryResponseDTO;
import org.example.multileanproject.entity.CartItem;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.CourseStatus;
import org.example.multileanproject.entity.Instructor;
import org.example.multileanproject.entity.Notification;
import org.example.multileanproject.entity.Order;
import org.example.multileanproject.entity.OrderDetail;
import org.example.multileanproject.entity.OrderStatus;
import org.example.multileanproject.entity.RefundRequest;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.entity.SaleCampaignItem;
import org.example.multileanproject.repository.CartItemRepository;
import org.example.multileanproject.repository.CouponRepository;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.InstructorRepository;
import org.example.multileanproject.repository.OrderRepository;
import org.example.multileanproject.repository.RefundRequestRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.repository.SaleCampaignItemRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal MIN_COUPON_ELIGIBLE_PRICE = BigDecimal.valueOf(100_000);
    private static final BigDecimal MIN_FINAL_PRICE_PER_COURSE = BigDecimal.valueOf(50_000);

    private final OrderRepository orderRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final CartItemRepository cartItemRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final EnrollmentService enrollmentService;
    private final NotificationService notificationService;
    private final CouponRepository couponRepository;
    private final CouponService couponService;
    private final EmailService emailService;
    private final OrderReceiptPdfService orderReceiptPdfService;

    // Gá»ŒI REPOSITORY Cá»¦A FLASH SALE VÃ€O ÄÃ‚Y
    private final SaleCampaignItemRepository campaignItemRepository;

    @Override
    public List<Order> getOrdersByStudentId(Long studentId) {
        Student student = getValidatedCurrentStudentById(studentId);
        return orderRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());
    }

    @Override
    public List<Order> getOrdersByCurrentUser(String email) {
        Student student = getStudentByEmail(email);
        return orderRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());
    }

    @Override
    public List<OrderHistoryResponseDTO> getOrderHistoryByStudentId(Long studentId) {
        List<Order> orders = getOrdersByStudentId(studentId);
        Set<Long> approvedRefundOrderIds = getApprovedRefundOrderIds(orders);
        return orders.stream()
                .map(order -> mapToOrderHistoryResponseDTO(order, approvedRefundOrderIds))
                .toList();
    }

    @Override
    public List<OrderHistoryResponseDTO> getOrderHistoryByCurrentUser(String email) {
        List<Order> orders = getOrdersByCurrentUser(email);
        Set<Long> approvedRefundOrderIds = getApprovedRefundOrderIds(orders);
        return orders.stream()
                .map(order -> mapToOrderHistoryResponseDTO(order, approvedRefundOrderIds))
                .toList();
    }

    @Override
    @Transactional
    public Order createOrderForCurrentUser(String email, Long courseId, String couponCode, String note) {
        Student student = getStudentByEmail(email);

        Order order = new Order();
        order.setStudent(student);
        order.setCouponCode(
                couponCode != null && !couponCode.isBlank() ? couponCode.trim().toUpperCase() : null
        );
        order.setNote(note);
        order.setStatus(OrderStatus.PENDING);

        List<OrderDetail> orderItems = new ArrayList<>();
        BigDecimal totalOriginal = BigDecimal.ZERO;
        BigDecimal totalFinal = BigDecimal.ZERO;
        BigDecimal couponEligibleAmount = BigDecimal.ZERO;
        BigDecimal maxCouponDiscountAmount = BigDecimal.ZERO;
        List<Long> couponEligibleInstructorUserIds = new ArrayList<>();

        if (courseId != null) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y khÃ³a há»c ID: " + courseId));

            validateCoursePurchasable(course, student);

            boolean isOwned = hasActiveOwnership(student.getId(), courseId);

            if (isOwned) {
                throw new RuntimeException("Báº¡n Ä‘Ã£ sá»Ÿ há»¯u khÃ³a há»c nÃ y rá»“i!");
            }

            OrderDetail item = new OrderDetail();
            item.setOrder(order);
            item.setCourse(course);

            // Äá»”I THÃ€NH HÃ€M TÃNH GIÃ CÃ“ FLASH SALE
            BigDecimal sellPrice = getDynamicSellPrice(course);
            item.setPrice(sellPrice);

            orderItems.add(item);

            totalOriginal = totalOriginal.add(sellPrice);
            totalFinal = totalFinal.add(sellPrice);
            if (isCouponEligible(course, sellPrice)) {
                couponEligibleAmount = couponEligibleAmount.add(sellPrice);
                maxCouponDiscountAmount = maxCouponDiscountAmount.add(getMaxCouponDiscountForCourse(sellPrice));
                couponEligibleInstructorUserIds.add(course.getInstructor().getUser().getId());
            }

        } else {
            List<CartItem> cartItems = cartItemRepository.findByStudentId(student.getId());

            if (cartItems == null || cartItems.isEmpty()) {
                throw new RuntimeException("Giá» hÃ ng trá»‘ng! Vui lÃ²ng chá»n khÃ³a há»c.");
            }

            for (CartItem cartItem : cartItems) {
                Course course = cartItem.getCourse();
                if (course == null) {
                    continue;
                }

                validateCoursePurchasable(course, student);

                boolean isOwned = hasActiveOwnership(student.getId(), course.getId());

                if (isOwned) {
                    throw new RuntimeException("Bạn đã sở hữu khóa học: " + course.getTitle());
                }

                OrderDetail item = new OrderDetail();
                item.setOrder(order);
                item.setCourse(course);

                // Äá»”I THÃ€NH HÃ€M TÃNH GIÃ CÃ“ FLASH SALE
                BigDecimal sellPrice = getDynamicSellPrice(course);
                item.setPrice(sellPrice);

                orderItems.add(item);

                totalOriginal = totalOriginal.add(sellPrice);
                totalFinal = totalFinal.add(sellPrice);
                if (isCouponEligible(course, sellPrice)) {
                    couponEligibleAmount = couponEligibleAmount.add(sellPrice);
                    maxCouponDiscountAmount = maxCouponDiscountAmount.add(getMaxCouponDiscountForCourse(sellPrice));
                    couponEligibleInstructorUserIds.add(course.getInstructor().getUser().getId());
                }
            }

            if (orderItems.isEmpty()) {
                throw new RuntimeException("KhÃ´ng cÃ³ khÃ³a há»c há»£p lá»‡ Ä‘á»ƒ thanh toÃ¡n.");
            }

            cartItemRepository.deleteAll(cartItems);
        }

        order.setOrderItems(orderItems);
        order.setOriginalAmount(totalOriginal);

        BigDecimal finalAmount = totalFinal;

        if (order.getCouponCode() != null) {
            if (couponEligibleAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Mã giảm giá không áp dụng cho khóa học đang flash sale hoặc giá quá thấp.");
            }

            List<Long> instructorUserIds = couponEligibleInstructorUserIds.stream()
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            BigDecimal discount = couponService.calculateDiscount(order.getCouponCode(), couponEligibleAmount, instructorUserIds);
            if (discount.compareTo(maxCouponDiscountAmount) > 0) {
                discount = maxCouponDiscountAmount;
            }
            finalAmount = finalAmount.subtract(discount);

            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }
        }

        order.setFinalAmount(finalAmount);

        Order saved = orderRepository.save(order);

        // KhÃ³a há»c miá»…n phÃ­ (giÃ¡ = 0): tá»± Ä‘á»™ng kÃ­ch hoáº¡t luÃ´n, khÃ´ng cáº§n qua VNPay
        if (finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            processPaymentSuccess(saved.getId());
        }

        return saved;
    }

    @Override
    public CheckoutPreviewResponseDTO previewCheckout(String email, Long courseId, String couponCode) {
        Student student = getStudentByEmail(email);

        BigDecimal originalAmount = BigDecimal.ZERO;
        BigDecimal couponEligibleAmount = BigDecimal.ZERO;
        BigDecimal maxCouponDiscountAmount = BigDecimal.ZERO;
        List<Long> couponEligibleInstructorUserIds = new ArrayList<>();

        if (courseId != null) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y khÃ³a há»c"));

            validateCoursePurchasable(course, student);

            boolean isOwned = hasActiveOwnership(student.getId(), courseId);

            if (isOwned) {
                throw new RuntimeException("Báº¡n Ä‘Ã£ sá»Ÿ há»¯u khÃ³a há»c nÃ y rá»“i!");
            }

            // Äá»”I THÃ€NH HÃ€M TÃNH GIÃ CÃ“ FLASH SALE
            BigDecimal sellPrice = getDynamicSellPrice(course);
            originalAmount = sellPrice;
            if (isCouponEligible(course, sellPrice)) {
                couponEligibleAmount = sellPrice;
                maxCouponDiscountAmount = getMaxCouponDiscountForCourse(sellPrice);
                couponEligibleInstructorUserIds.add(course.getInstructor().getUser().getId());
            }
        } else {
            List<CartItem> cartItems = cartItemRepository.findByStudentId(student.getId());

            if (cartItems == null || cartItems.isEmpty()) {
                throw new RuntimeException("Giá» hÃ ng trá»‘ng! Vui lÃ²ng chá»n khÃ³a há»c.");
            }

            for (CartItem cartItem : cartItems) {
                Course course = cartItem.getCourse();
                if (course == null) {
                    continue;
                }

                validateCoursePurchasable(course, student);

                boolean isOwned = hasActiveOwnership(student.getId(), course.getId());

                if (isOwned) {
                    throw new RuntimeException("Bạn đã sở hữu khóa học: " + course.getTitle());
                }

                // Äá»”I THÃ€NH HÃ€M TÃNH GIÃ CÃ“ FLASH SALE
                BigDecimal sellPrice = getDynamicSellPrice(course);
                originalAmount = originalAmount.add(sellPrice);
                if (isCouponEligible(course, sellPrice)) {
                    couponEligibleAmount = couponEligibleAmount.add(sellPrice);
                    maxCouponDiscountAmount = maxCouponDiscountAmount.add(getMaxCouponDiscountForCourse(sellPrice));
                    couponEligibleInstructorUserIds.add(course.getInstructor().getUser().getId());
                }
            }

            if (originalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("KhÃ´ng cÃ³ khÃ³a há»c há»£p lá»‡ Ä‘á»ƒ thanh toÃ¡n.");
            }
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        boolean valid = true;
        String normalizedCoupon = couponCode != null && !couponCode.isBlank()
                ? couponCode.trim().toUpperCase()
                : null;
        String message = "KhÃ´ng cÃ³ mÃ£ giáº£m giÃ¡";

        if (normalizedCoupon != null) {
            try {
                if (couponEligibleAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Mã giảm giá không áp dụng cho khóa học đang flash sale hoặc giá quá thấp.");
                }

                List<Long> instructorUserIds = couponEligibleInstructorUserIds.stream()
                        .distinct()
                        .collect(java.util.stream.Collectors.toList());
                discountAmount = couponService.calculateDiscount(normalizedCoupon, couponEligibleAmount, instructorUserIds);
                if (discountAmount.compareTo(maxCouponDiscountAmount) > 0) {
                    discountAmount = maxCouponDiscountAmount;
                }
                message = "Ãp dá»¥ng mÃ£ giáº£m giÃ¡ thÃ nh cÃ´ng";
            } catch (Exception e) {
                valid = false;
                message = e.getMessage();
            }
        }

        BigDecimal finalAmount = originalAmount.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        return new CheckoutPreviewResponseDTO(
                originalAmount,
                couponEligibleAmount,
                discountAmount,
                finalAmount,
                normalizedCoupon,
                valid,
                message
        );
    }

    @Override
    public void processPaymentSuccess(Long orderId) {
        processPaymentSuccess(orderId, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateReceiptPdfByCurrentUser(String email, Long orderId) {
        Student student = getStudentByEmail(email);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng."));

        Long ownerId = order.getStudent() != null ? order.getStudent().getId() : null;
        if (!Objects.equals(ownerId, student.getId())) {
            throw new RuntimeException("Bạn không có quyền tải biên nhận của đơn hàng này.");
        }

        Optional<RefundRequest> approvedRefundOpt = refundRequestRepository
                .findTopByOrderIdAndStatusOrderByResolvedAtDescIdDesc(orderId, "APPROVED");

        return orderReceiptPdfService.generateOrderReceipt(order, approvedRefundOpt.orElse(null));
    }

    @Override
    @Transactional
    public void processPaymentSuccess(Long orderId, String transactionRef, String paymentMethod) {
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order != null && order.getStatus() != OrderStatus.COMPLETED) {
            if (transactionRef != null && !transactionRef.isBlank()) {
                order.setTransactionRef(transactionRef.trim());
            }
            if (paymentMethod != null && !paymentMethod.isBlank()) {
                order.setPaymentMethod(paymentMethod.trim());
            }
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);

            if (order.getCouponCode() != null && !order.getCouponCode().isBlank()) {
                couponService.incrementUsageCount(order.getCouponCode());
            }

            Student buyer = order.getStudent();

            // ðŸ”¥ THÃŠM Cá»¦A DÅ¨NG: LÆ°u láº¡i ID cÃ¡c khÃ³a há»c vá»«a mua thÃ nh cÃ´ng
            List<Long> completedCourseIds = new ArrayList<>();

            // Kiá»ƒm tra rental order
            String note = order.getNote();
            boolean isRental = note != null && note.startsWith("RENTAL:");
            int rentalDays = 30;
            if (isRental) {
                try { rentalDays = Integer.parseInt(note.split(":")[1]); } catch (Exception ignored) {}
            }

            for (OrderDetail item : order.getOrderItems()) {
                try {
                    Course course = item.getCourse();

                    if (isRental) {
                        enrollmentService.activateRental(buyer.getId(), course.getId(), rentalDays);
                    } else {
                        enrollmentService.activateCourse(buyer.getId(), course.getId());
                    }

                    // ðŸ”¥ THÃŠM Cá»¦A DÅ¨NG: Äáº©y vÃ o danh sÃ¡ch
                    completedCourseIds.add(course.getId());

                    if (course.getInstructor() != null) {
                        creditInstructorWallet(course.getInstructor(), course, item.getPrice());
                    }

                    // Cá»˜NG THÃŠM 1 VÃ€O Sá» LÆ¯á»¢NG ÄÃƒ BÃN (TRá»ª SLOT FLASH SALE)
                    Optional<SaleCampaignItem> activeSaleOpt = campaignItemRepository.findActiveSaleForCourse(course.getId(), LocalDateTime.now());
                    if (activeSaleOpt.isPresent()) {
                        SaleCampaignItem saleItem = activeSaleOpt.get();
                        saleItem.setSoldSlots(saleItem.getSoldSlots() + 1);
                        campaignItemRepository.save(saleItem);
                    }

                    // ðŸ”¥ THÃ”NG BÃO CHO Há»ŒC VIÃŠN LÃ€ THANH TOÃN THÃ€NH CÃ”NG ðŸ”¥
                    notificationService.createNotification(
                            buyer.getId(),
                            "Thanh toÃ¡n thÃ nh cÃ´ng! ðŸŽ‰",
                            "Báº¡n Ä‘Ã£ Ä‘Äƒng kÃ½ thÃ nh cÃ´ng khÃ³a há»c '" + course.getTitle() + "'.",
                            "/my-courses",
                            "STUDENT_PAYMENT_SUCCESS", // Tiá»n tá»‘ STUDENT_
                            Notification.NotificationCategory.SYSTEM_NOTIFICATION,
                            true,
                            course.getTitle(),
                            buyer.getFullName()
                    );

                    // ðŸ”¥ THÃ”NG BÃO CHO GIáº¢NG VIÃŠN BIáº¾T CÃ“ Há»ŒC VIÃŠN ÄÄ‚NG KÃ Má»šI ðŸ”¥
                    if (course.getInstructor() != null
                            && course.getInstructor().getUser() != null) {

                        Long instructorId = course.getInstructor().getUser().getId();
                        notificationService.createNotification(
                                instructorId,
                                "ðŸŽ‰ CÃ³ há»c viÃªn má»›i Ä‘Äƒng kÃ½",
                                "Há»c viÃªn " + buyer.getFullName() + " vá»«a mua khÃ³a há»c cá»§a báº¡n.",
                                "/instructor/performance/students", // CÃ³ thá»ƒ Ä‘á»•i link tá»›i danh sÃ¡ch há»c viÃªn
                                "INSTRUCTOR_NEW_ENROLLMENT", // Tiá»n tá»‘ INSTRUCTOR_
                                Notification.NotificationCategory.INSTRUCTOR_ANNOUNCEMENT,
                                false,
                                course.getTitle(),
                                buyer.getFullName()
                        );
                    }

                } catch (Exception e) {
                    System.err.println("âš ï¸ Lá»—i khi xá»­ lÃ½ thanh toÃ¡n thÃ nh cÃ´ng cho item: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // ðŸ”¥ Báº®T Äáº¦U PHáº¦N NÃ‚NG Cáº¤P Cá»¦A DÅ¨NG: Dá»n rÃ¡c cÃ¡c Ä‘Æ¡n PENDING bá»‹ trÃ¹ng
            if (!completedCourseIds.isEmpty()) {
                List<Order> duplicates = orderRepository.findDuplicatePendingOrders(
                        buyer.getId(), orderId, completedCourseIds);
                if (!duplicates.isEmpty()) {
                    for (Order dup : duplicates) {
                        dup.setStatus(OrderStatus.CANCELLED);
                        dup.setNote("Tá»± Ä‘á»™ng huá»· â€” khÃ³a há»c Ä‘Ã£ Ä‘Æ°á»£c mua qua Ä‘Æ¡n #" + orderId);
                    }
                    orderRepository.saveAll(duplicates);
                }
            }
            // ðŸ”¥ Káº¾T THÃšC PHáº¦N NÃ‚NG Cáº¤P Cá»¦A DÅ¨NG

            try {
                emailService.sendOrderSuccessEmail(order);
            } catch (Exception e) {
                System.err.println("âš ï¸ Gá»­i mail xÃ¡c nháº­n Ä‘Æ¡n hÃ ng tháº¥t báº¡i: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private OrderHistoryResponseDTO mapToOrderHistoryResponseDTO(Order order, Set<Long> approvedRefundOrderIds) {
        List<OrderHistoryItemDTO> items = new ArrayList<>();
        String status = order.getStatus() != null ? order.getStatus().name() : null;

        if (status != null
                && "COMPLETED".equalsIgnoreCase(status)
                && approvedRefundOrderIds.contains(order.getId())) {
            status = "REFUNDED";
        }

        String paymentMethod = order.getPaymentMethod();
        if ((paymentMethod == null || paymentMethod.isBlank())
                && "COMPLETED".equalsIgnoreCase(status)
                && order.getFinalAmount() != null
                && order.getFinalAmount().compareTo(BigDecimal.ZERO) > 0) {
            paymentMethod = "VNPAY";
        }

        if (order.getOrderItems() != null) {
            for (OrderDetail item : order.getOrderItems()) {
                items.add(mapToOrderHistoryItemDTO(item));
            }
        }

        return OrderHistoryResponseDTO.builder()
                .id(order.getId())
                .originalAmount(order.getOriginalAmount())
                .finalAmount(order.getFinalAmount())
                .couponCode(order.getCouponCode())
                .note(order.getNote())
                .status(status)
                .paymentMethod(paymentMethod)
                .transactionRef(order.getTransactionRef())
                .createdAt(order.getCreatedAt())
                .orderItems(items)
                .build();
    }

    private Set<Long> getApprovedRefundOrderIds(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Set.of();
        }

        List<Long> orderIds = orders.stream()
                .map(Order::getId)
                .filter(java.util.Objects::nonNull)
                .toList();

        if (orderIds.isEmpty()) {
            return Set.of();
        }

        return refundRequestRepository.findApprovedRefundOrderIds(orderIds);
    }

    private OrderHistoryItemDTO mapToOrderHistoryItemDTO(OrderDetail item) {
        Course course = item.getCourse();

        return OrderHistoryItemDTO.builder()
                .id(item.getId())
                .courseId(course != null ? course.getId() : null)
                .title(course != null ? course.getTitle() : null)
                .thumbnail(course != null ? course.getThumbnail() : null)
                .slug(course != null ? course.getSlug() : null)
                .price(item.getPrice())
                .build();
    }

    private void creditInstructorWallet(Instructor instructor, Course course, BigDecimal salePrice) {
        if (instructor == null) {
            return;
        }

        BigDecimal actualPrice = salePrice != null ? salePrice : BigDecimal.ZERO;
        if (actualPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        double commissionRate = course != null && course.getCommissionRate() != null
                ? course.getCommissionRate()
                : 0.8d;
        if (commissionRate < 0d) commissionRate = 0d;
        if (commissionRate > 1d) commissionRate = 1d;

        BigDecimal instructorShare = actualPrice
                .multiply(BigDecimal.valueOf(commissionRate))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal currentBalance = instructor.getWalletBalance() != null
                ? instructor.getWalletBalance()
                : BigDecimal.ZERO;

        instructor.setWalletBalance(currentBalance.add(instructorShare));
        instructorRepository.save(instructor);
    }

    private boolean hasActiveOwnership(Long studentId, Long courseId) {
        if (enrollmentService.isStudentEnrolled(studentId, courseId)) {
            return true;
        }

        boolean hasCompletedOrder = orderRepository.existsByStudentIdAndCourseIdAndStatus(
                studentId,
                courseId,
                OrderStatus.COMPLETED
        );
        if (!hasCompletedOrder) {
            return false;
        }

        long approvedRefundCount = refundRequestRepository
                .countApprovedRefundByStudentAndCourse(studentId, courseId);
        return approvedRefundCount <= 0;
    }

    private Student getStudentByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("KhÃ´ng xÃ¡c Ä‘á»‹nh Ä‘Æ°á»£c ngÆ°á»i dÃ¹ng hiá»‡n táº¡i.");
        }

        return studentRepository.findByEmail(email.trim())
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n vá»›i email: " + email));
    }

    private Student getValidatedCurrentStudentById(Long studentId) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sinh viÃªn ID: " + studentId));

        if (student.getEmail() == null || !currentEmail.equalsIgnoreCase(student.getEmail())) {
            throw new RuntimeException("Báº¡n khÃ´ng cÃ³ quyá»n truy cáº­p dá»¯ liá»‡u Ä‘Æ¡n hÃ ng cá»§a tÃ i khoáº£n nÃ y.");
        }

        return student;
    }

    // THAY THáº¾ HÃ€M getSellPrice CÅ¨ Báº°NG HÃ€M NÃ€Y Äá»‚ ÃP Dá»¤NG GIÃ FLASH SALE KHI Táº O ÄÆ N
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

    private boolean isCouponEligible(Course course, BigDecimal effectivePrice) {
        if (effectivePrice == null || effectivePrice.compareTo(MIN_COUPON_ELIGIBLE_PRICE) < 0) {
            return false;
        }

        Optional<SaleCampaignItem> activeSaleOpt = campaignItemRepository.findActiveSaleForCourse(course.getId(), LocalDateTime.now());
        return activeSaleOpt.isEmpty()
                || (activeSaleOpt.get().getCampaign() != null
                && activeSaleOpt.get().getCampaign().isAllowCouponStacking());
    }

    private BigDecimal getMaxCouponDiscountForCourse(BigDecimal effectivePrice) {
        if (effectivePrice == null || effectivePrice.compareTo(MIN_FINAL_PRICE_PER_COURSE) <= 0) {
            return BigDecimal.ZERO;
        }

        return effectivePrice.subtract(MIN_FINAL_PRICE_PER_COURSE);
    }

    private void validateCoursePurchasable(Course course, Student student) {
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new RuntimeException("Khóa học hiện không mở bán.");
        }

        if (course.getInstructor() != null
                && course.getInstructor().getUser() != null
                && !course.getInstructor().getUser().isActive()) {
            throw new RuntimeException("Giảng viên của khóa học hiện đang bị khóa. Tạm thời không thể mua mới.");
        }

        if (course.getInstructor() != null
                && course.getInstructor().getUser() != null
                && course.getInstructor().getUser().getEmail() != null
                && student.getEmail() != null
                && student.getEmail().equalsIgnoreCase(course.getInstructor().getUser().getEmail())) {
            throw new RuntimeException("Bạn không thể mua khóa học của chính mình.");
        }
    }
}
