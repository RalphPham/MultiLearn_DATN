package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.ChatMessageDTO;
import org.example.multileanproject.dto.ChatMessageRequest;
import org.example.multileanproject.dto.ContactDTO;
import org.example.multileanproject.entity.ChatMessage;
import org.example.multileanproject.entity.Role;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.repository.ChatMessageRepository;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.service.WebSocketPresenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ChatController {

    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final WebSocketPresenceService presenceService;

    @MessageMapping("/chat.typing")
    public void typingIndicator(@Payload ChatMessageRequest request, Principal principal) {
        if (principal == null) return;
        Long senderId = Long.parseLong(principal.getName());
        // Gửi event "đang gõ" tới người nhận
        messagingTemplate.convertAndSendToUser(
                request.getReceiverId().toString(),
                "/queue/typing",
                java.util.Map.of("senderId", senderId, "typing", request.getContent().equals("true"))
        );
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        Long senderId = Long.parseLong(principal.getName());
        Student sender = studentRepository.findById(senderId).orElseThrow();

        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .attachmentUrl(request.getAttachmentUrl())
                .attachmentType(request.getAttachmentType())
                .build();
        ChatMessage savedMsg = chatMessageRepository.save(message);

        ChatMessageDTO dto = ChatMessageDTO.builder()
                .id(savedMsg.getId())
                .senderId(senderId)
                .senderRole(sender.getRole().toString())
                .content(savedMsg.getContent())
                .createdAt(savedMsg.getTimestamp())
                .attachmentUrl(savedMsg.getAttachmentUrl())
                .attachmentType(savedMsg.getAttachmentType())
                .build();

        messagingTemplate.convertAndSendToUser(
                request.getReceiverId().toString(),
                "/queue/messages",
                dto
        );

        // LOGIC AUTO-REPLY NẾU NGƯỜI NHẬN OFFLINE
        String receiverIdStr = request.getReceiverId().toString();
        if (!presenceService.isUserOnline(receiverIdStr)) {
            Student receiver = studentRepository.findById(request.getReceiverId()).orElse(null);

            if (receiver != null && receiver.getRole() == Role.INSTRUCTOR) {
                // Lấy trực tiếp khóa học student đang học với instructor này (dùng JPQL JOIN tránh lazy load)
                List<Course> enrolledCourses = enrollmentRepository.findCoursesByStudentAndInstructor(senderId, request.getReceiverId());

                String autoReplyContent = generateAutoReply(request.getContent(), enrolledCourses);

                ChatMessage autoReply = ChatMessage.builder()
                        .senderId(request.getReceiverId())
                        .receiverId(senderId)
                        .content(autoReplyContent)
                        .timestamp(LocalDateTime.now().plusSeconds(1))
                        .isRead(true)
                        .build();

                ChatMessage savedAutoReply = chatMessageRepository.save(autoReply);

                messagingTemplate.convertAndSendToUser(
                        senderId.toString(),
                        "/queue/messages",
                        ChatMessageDTO.builder()
                                .id(savedAutoReply.getId())
                                .senderId(request.getReceiverId())
                                .senderRole("INSTRUCTOR")
                                .content(savedAutoReply.getContent())
                                .createdAt(savedAutoReply.getTimestamp())
                                .build()
                );
            }
        }
    }

    @GetMapping("/api/messages/history/{partnerId}/paged")
    public ResponseEntity<Map<String, Object>> getHistoryPaged(
            @PathVariable Long partnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Student current = getCurrentUser();
        // Query size+1 để biết có trang tiếp không, tránh false positive
        List<ChatMessage> messages = chatMessageRepository.findConversationPaged(
                current.getId(), partnerId, PageRequest.of(page, size + 1));
        boolean hasMore = messages.size() > size;
        if (hasMore) messages = messages.subList(0, size);
        // Đảo lại để hiển thị cũ → mới
        java.util.Collections.reverse(messages);
        List<ChatMessageDTO> dtos = messages.stream().map(m -> {
            Student sender = studentRepository.findById(m.getSenderId()).orElse(null);
            return ChatMessageDTO.builder()
                    .id(m.getId())
                    .senderId(m.getSenderId())
                    .senderRole(sender != null ? sender.getRole().toString() : "UNKNOWN")
                    .content(m.isDeleted() ? "Tin nhắn đã bị thu hồi" : m.getContent())
                    .createdAt(m.getTimestamp())
                    .deleted(m.isDeleted())
                    .attachmentUrl(m.isDeleted() ? null : m.getAttachmentUrl())
                    .attachmentType(m.isDeleted() ? null : m.getAttachmentType())
                    .build();
        }).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("messages", dtos, "hasMore", hasMore));
    }

    @GetMapping("/api/messages/history/{partnerId}")
    public ResponseEntity<List<ChatMessageDTO>> getHistory(@PathVariable Long partnerId) {
        Student current = getCurrentUser();
        List<ChatMessage> messages = chatMessageRepository.findConversation(current.getId(), partnerId);

        List<ChatMessageDTO> dtos = messages.stream().map(m -> {
            Student sender = studentRepository.findById(m.getSenderId()).orElse(null);
            return ChatMessageDTO.builder()
                    .id(m.getId())
                    .senderId(m.getSenderId())
                    .senderRole(sender != null ? sender.getRole().toString() : "UNKNOWN")
                    .content(m.isDeleted() ? "Tin nhắn đã bị thu hồi" : m.getContent())
                    .createdAt(m.getTimestamp())
                    .deleted(m.isDeleted())
                    .attachmentUrl(m.isDeleted() ? null : m.getAttachmentUrl())
                    .attachmentType(m.isDeleted() ? null : m.getAttachmentType())
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/api/messages/instructor/contacts")
    public ResponseEntity<List<ContactDTO>> getInstructorContacts() {
        return ResponseEntity.ok(getContactsByHistory());
    }

    @GetMapping("/api/messages/student/contacts")
    public ResponseEntity<List<ContactDTO>> getStudentContacts() {
        Student currentStudent = getCurrentUser();

        List<Student> myInstructors = enrollmentRepository.findInstructorsByStudentId(currentStudent.getId());
        List<ChatMessage> latestMessages = chatMessageRepository.findLatestMessagesByUser(currentStudent.getId());

        List<ContactDTO> contacts = myInstructors.stream().map(instructor -> {
            ChatMessage lastMsg = latestMessages.stream()
                    .filter(m -> m.getSenderId().equals(instructor.getId()) || m.getReceiverId().equals(instructor.getId()))
                    .findFirst()
                    .orElse(null);

            return ContactDTO.builder()
                    .userId(instructor.getId())
                    .userName(instructor.getFullName())
                    .lastMessageContent(lastMsg != null ? lastMsg.getContent() : "Chưa có cuộc trò chuyện nào. Bắt đầu ngay!")
                    .lastMessageTime(lastMsg != null ? lastMsg.getTimestamp() : null)
                    .unreadCount(chatMessageRepository.countUnreadFromPartner(instructor.getId(), currentStudent.getId()))
                    .online(presenceService.isUserOnline(instructor.getId().toString()))
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(contacts);
    }

    private List<ContactDTO> getContactsByHistory() {
        Student current = getCurrentUser();
        List<ChatMessage> latestMessages = chatMessageRepository.findLatestMessagesByUser(current.getId());

        return latestMessages.stream().map(m -> {
            Long partnerId = m.getSenderId().equals(current.getId()) ? m.getReceiverId() : m.getSenderId();
            Student partner = studentRepository.findById(partnerId).orElse(null);

            return ContactDTO.builder()
                    .userId(partnerId)
                    .userName(partner != null ? partner.getFullName() : "User ẩn danh")
                    .lastMessageContent(m.getContent())
                    .lastMessageTime(m.getTimestamp())
                    .unreadCount(chatMessageRepository.countUnreadFromPartner(partnerId, current.getId()))
                    .online(presenceService.isUserOnline(partnerId.toString()))
                    .build();
        }).collect(Collectors.toList());
    }

    private String generateAutoReply(String message, List<Course> enrolledCourses) {
        String msg = message.toLowerCase().trim();

        // Xây dựng context khóa học cụ thể
        String courseContext = "";
        String courseListText = "";
        Course primaryCourse = enrolledCourses.isEmpty() ? null : enrolledCourses.get(0);

        if (!enrolledCourses.isEmpty()) {
            if (enrolledCourses.size() == 1) {
                Course c = enrolledCourses.get(0);
                courseContext = String.format("khóa học \"%s\"", c.getTitle());
                int lessons = c.getTotalLessons() != null ? c.getTotalLessons() : 0;
                int duration = c.getTotalDuration() != null ? c.getTotalDuration() : 0;
                double rating = c.getAverageRating() != null ? c.getAverageRating() : 0.0;
                String level = c.getLevel() != null ? c.getLevel() : "Chưa xác định";
                courseListText = String.format(
                    "📚 Khóa học của bạn: **%s**\n• Số bài học: %d bài\n• Tổng thời lượng: %d phút (~%.1f giờ)\n• Cấp độ: %s\n• Đánh giá: %.1f/5.0 ⭐",
                    c.getTitle(), lessons, duration, duration / 60.0, level, rating
                );
            } else {
                courseContext = enrolledCourses.size() + " khóa học";
                StringBuilder sb = new StringBuilder("📚 Các khóa học bạn đang học:\n");
                for (Course c : enrolledCourses) {
                    sb.append(String.format("• %s (%d bài, %d phút)\n", c.getTitle(), c.getTotalLessons(), c.getTotalDuration()));
                }
                courseListText = sb.toString().trim();
            }
        }

        // 1. CHÀO HỎI
        if (containsAny(msg, "xin chào", "hello", "hi ", "chào bạn", "chào thầy", "chào cô", "hey")) {
            if (primaryCourse != null) {
                return String.format("🤖 Trợ lý tự động: Xin chào! Giảng viên hiện không online. Tôi thấy bạn đang học %s. Nếu có thắc mắc về khóa học, cứ nhắn tin để lại — giảng viên sẽ phản hồi sớm nhất có thể! 😊", courseContext);
            }
            return "🤖 Trợ lý tự động: Xin chào! Giảng viên hiện không online. Tôi có thể giúp bạn một số thông tin cơ bản. Hãy để lại câu hỏi, giảng viên sẽ phản hồi sớm nhất có thể!";
        }

        // 2. CẢM ƠN
        if (containsAny(msg, "cảm ơn", "camon", "thanks", "thank you", "tks")) {
            return "🤖 Trợ lý tự động: Không có gì! Chúc bạn học tốt 😊 Giảng viên sẽ phản hồi khi quay lại nếu bạn cần thêm hỗ trợ.";
        }

        // 3. CHỨNG CHỈ
        if (containsAny(msg, "chứng chỉ", "certificate", "bằng", "chứng nhận", "hoàn thành")) {
            if (primaryCourse != null) {
                int totalLessons = primaryCourse.getTotalLessons() != null ? primaryCourse.getTotalLessons() : 0;
                return String.format("🤖 Trợ lý tự động: Sau khi hoàn thành toàn bộ %d bài học trong \"%s\", bạn sẽ được cấp chứng chỉ hoàn thành. Chứng chỉ có thể tải về tại trang \"Khoá học của tôi\". Giảng viên sẽ xác nhận thêm nếu cần!", totalLessons, primaryCourse.getTitle());
            }
            return "🤖 Trợ lý tự động: Sau khi hoàn thành toàn bộ bài học, bạn sẽ được cấp chứng chỉ. Tải về tại trang \"Khoá học của tôi\".";
        }

        // 4. HỌC PHÍ / HOÀN TIỀN
        if (containsAny(msg, "giá", "học phí", "chi phí", "hoàn tiền", "refund", "thanh toán", "mua", "mất tiền")) {
            if (primaryCourse != null) {
                String priceInfo = (primaryCourse.getPrice() == null || primaryCourse.getPrice().compareTo(java.math.BigDecimal.ZERO) == 0)
                    ? "miễn phí"
                    : String.format("%,.0f VNĐ", primaryCourse.getPrice());
                return String.format("🤖 Trợ lý tự động: Khóa học \"%s\" có giá %s. Về chính sách hoàn tiền, vui lòng liên hệ bộ phận hỗ trợ hoặc đợi giảng viên phản hồi trực tiếp.", primaryCourse.getTitle(), priceInfo);
            }
            return "🤖 Trợ lý tự động: Thông tin học phí xem tại trang chi tiết khóa học. Về hoàn tiền, liên hệ bộ phận hỗ trợ hoặc đợi giảng viên phản hồi.";
        }

        // 5. LỖI KỸ THUẬT
        if (containsAny(msg, "video", "không xem được", "lỗi", "error", "không load", "bị lỗi", "không phát", "lag", "chậm", "bị đứng")) {
            return "🤖 Trợ lý tự động: Nếu gặp lỗi kỹ thuật, bạn thử:\n1️⃣ Tải lại trang (F5)\n2️⃣ Xóa cache trình duyệt\n3️⃣ Thử trình duyệt khác (Chrome/Edge)\n4️⃣ Kiểm tra kết nối mạng\n\nNếu vẫn lỗi, giảng viên sẽ hỗ trợ khi online!";
        }

        // 6. THỜI LƯỢNG / TIẾN ĐỘ
        if (containsAny(msg, "bao lâu", "mất bao nhiêu", "thời gian", "lịch học", "deadline", "hạn nộp", "bao nhiêu giờ")) {
            if (primaryCourse != null) {
                int dur = primaryCourse.getTotalDuration() != null ? primaryCourse.getTotalDuration() : 0;
                int les = primaryCourse.getTotalLessons() != null ? primaryCourse.getTotalLessons() : 0;
                return String.format("🤖 Trợ lý tự động: Khóa học \"%s\" có tổng %d phút (~%.1f giờ) với %d bài học. Bạn hoàn toàn tự sắp xếp tốc độ học, không có deadline cố định. Học lại bài bất kỳ lúc nào!",
                    primaryCourse.getTitle(), dur, dur / 60.0, les);
            }
            return "🤖 Trợ lý tự động: Bạn tự sắp xếp tốc độ học, không có deadline. Xem thời lượng chi tiết tại trang khóa học.";
        }

        // 7. NỘI DUNG / CHƯƠNG TRÌNH HỌC
        if (containsAny(msg, "nội dung", "chương trình", "học gì", "gồm những gì", "bao nhiêu bài", "bài học", "khóa học có")) {
            if (!enrolledCourses.isEmpty()) {
                return "🤖 Trợ lý tự động: " + courseListText + "\n\nXem chi tiết từng bài học tại trang \"Góc học tập\". Giảng viên sẽ tư vấn thêm khi online!";
            }
            return "🤖 Trợ lý tự động: Toàn bộ nội dung khóa học được liệt kê tại trang chi tiết. Giảng viên sẽ tư vấn thêm khi online!";
        }

        // 8. ĐÁNH GIÁ / NHẬN XÉT
        if (containsAny(msg, "đánh giá", "review", "nhận xét", "rating", "chất lượng", "có tốt không", "có hay không")) {
            if (primaryCourse != null) {
                double avgRating = primaryCourse.getAverageRating() != null ? primaryCourse.getAverageRating() : 0.0;
                int studentCount = primaryCourse.getStudentCount() != null ? primaryCourse.getStudentCount() : 0;
                return String.format("🤖 Trợ lý tự động: Khóa học \"%s\" đang được đánh giá %.1f/5.0 ⭐ từ %d học viên. Nếu bạn muốn để lại đánh giá, vào trang chi tiết khóa học nhé!",
                    primaryCourse.getTitle(), avgRating, studentCount);
            }
            return "🤖 Trợ lý tự động: Bạn có thể xem và để lại đánh giá tại trang chi tiết khóa học.";
        }

        // 9. YÊU CẦU ĐỐI VỚI KHÓA HỌC / KIẾN THỨC NỀN
        if (containsAny(msg, "cần biết gì", "yêu cầu", "điều kiện", "trước khi học", "kiến thức nền", "prerequisite", "cần có", "cần học gì trước")) {
            if (primaryCourse != null) {
                String level = primaryCourse.getLevel();
                String levelDesc = switch (level != null ? level.toUpperCase() : "") {
                    case "BEGINNER" -> "người mới bắt đầu, không yêu cầu kiến thức nền đặc biệt";
                    case "INTERMEDIATE" -> "người đã có kiến thức cơ bản";
                    case "ADVANCED" -> "người có kinh nghiệm, cần nền tảng vững";
                    default -> "nhiều cấp độ khác nhau";
                };
                return String.format("🤖 Trợ lý tự động: Khóa học \"%s\" được thiết kế cho %s. Bạn có thể xem yêu cầu chi tiết tại trang khóa học. Giảng viên sẽ tư vấn thêm khi online!", primaryCourse.getTitle(), levelDesc);
            }
            return "🤖 Trợ lý tự động: Yêu cầu kiến thức nền tảng được ghi rõ tại trang chi tiết khóa học. Giảng viên sẽ tư vấn thêm khi online!";
        }

        // 10. THIẾT BỊ / NỀN TẢNG
        if (containsAny(msg, "điện thoại", "máy tính", "tablet", "ipad", "android", "ios", "trình duyệt", "xem trên", "thiết bị")) {
            return "🤖 Trợ lý tự động: Bạn có thể học trên mọi thiết bị — máy tính, điện thoại, hoặc máy tính bảng — thông qua trình duyệt web (Chrome, Edge, Safari). Không cần cài ứng dụng riêng! 📱💻";
        }

        // 11. TÀI KHOẢN / ĐĂNG NHẬP
        if (containsAny(msg, "đăng nhập", "tài khoản", "mật khẩu", "quên mật khẩu", "đổi mật khẩu", "login", "không vào được", "bị khóa tài khoản")) {
            return "🤖 Trợ lý tự động: Nếu gặp vấn đề đăng nhập, bạn thử:\n1️⃣ Dùng chức năng \"Quên mật khẩu\" tại trang đăng nhập\n2️⃣ Kiểm tra email xác nhận trong hộp thư (kể cả Spam)\n3️⃣ Đảm bảo đăng ký đúng email\n\nNếu vẫn không được, giảng viên sẽ hỗ trợ khi online!";
        }

        // 12. TIẾN ĐỘ HỌC TẬP
        if (containsAny(msg, "tiến độ", "đã học đến đâu", "bao nhiêu phần trăm", "còn bao nhiêu", "học đến bài", "xem lại bài")) {
            if (primaryCourse != null) {
                return String.format("🤖 Trợ lý tự động: Bạn có thể xem tiến độ học tập của mình trong \"%s\" tại mục \"Góc học tập\" → chọn khóa học → thanh tiến trình sẽ hiển thị bài đã hoàn thành. Học lại bài cũ bất kỳ lúc nào nhé! 📊", primaryCourse.getTitle());
            }
            return "🤖 Trợ lý tự động: Tiến độ học tập của bạn được lưu tự động. Vào \"Góc học tập\" để xem các bài đã hoàn thành và tiếp tục từ chỗ đang dở!";
        }

        // 13. NHÓM / CỘNG ĐỒNG HỌC TẬP
        if (containsAny(msg, "nhóm", "cộng đồng", "group", "discord", "facebook", "zalo", "kết nối", "học cùng")) {
            return "🤖 Trợ lý tự động: Thông tin về nhóm học tập hoặc cộng đồng sẽ được giảng viên chia sẻ trực tiếp. Hãy để lại câu hỏi, giảng viên sẽ phản hồi sớm khi online! 👥";
        }

        // 14. HỖ TRỢ / LIÊN HỆ
        if (containsAny(msg, "liên hệ", "hotline", "hỗ trợ", "support", "giúp đỡ", "admin", "ban quản trị")) {
            return "🤖 Trợ lý tự động: Để được hỗ trợ nhanh nhất:\n• Nhắn tin trực tiếp tại đây — giảng viên sẽ phản hồi khi online\n• Hoặc liên hệ bộ phận hỗ trợ qua trang Liên hệ trên website\n\nChúng tôi luôn sẵn sàng giúp bạn! 🙏";
        }

        // 15. TẢI VỀ / HỌC OFFLINE
        if (containsAny(msg, "tải về", "download", "học offline", "không có mạng", "lưu về máy")) {
            return "🤖 Trợ lý tự động: Hiện tại các bài học được phát trực tuyến (streaming). Tính năng tải về để học offline đang trong lộ trình phát triển. Bạn có thể học khi có kết nối internet trên bất kỳ thiết bị nào! 🌐";
        }

        // 17. KHÔNG THUỘC PHẠM VI → cần giảng viên trả lời trực tiếp
        String waitMsg = "🤖 Trợ lý tự động: Câu hỏi của bạn cần giảng viên hỗ trợ trực tiếp.";
        if (primaryCourse != null) {
            waitMsg += String.format(" Giảng viên phụ trách \"%s\" sẽ phản hồi bạn sớm nhất khi online trở lại. Vui lòng chờ! ⏳", primaryCourse.getTitle());
        } else {
            waitMsg += " Giảng viên sẽ phản hồi sớm nhất khi online trở lại. Vui lòng chờ! ⏳";
        }
        return waitMsg;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    private static final Path CHAT_UPLOAD_DIR = Paths.get("uploads/chat").toAbsolutePath().normalize();

    static {
        try { Files.createDirectories(CHAT_UPLOAD_DIR); } catch (Exception ignored) {}
    }

    @PostMapping("/api/messages/upload")
    public ResponseEntity<Map<String, String>> uploadChatFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        if (file.getSize() > 10 * 1024 * 1024)
            return ResponseEntity.badRequest().body(Map.of("error", "File tối đa 10MB"));

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        String mime = file.getContentType() != null ? file.getContentType() : "";

        boolean isImage = mime.startsWith("image/");
        boolean isFile = original.endsWith(".pdf") || original.endsWith(".docx") || original.endsWith(".xlsx")
                || original.endsWith(".txt") || original.endsWith(".zip") || original.endsWith(".doc");

        if (!isImage && !isFile)
            return ResponseEntity.badRequest().body(Map.of("error", "Chỉ chấp nhận ảnh hoặc PDF/DOCX/XLSX/TXT/ZIP"));

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), CHAT_UPLOAD_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

        String url = backendUrl + "/uploads/chat/" + fileName;
        String type = isImage ? "image" : "file";
        return ResponseEntity.ok(Map.of("url", url, "type", type, "name", file.getOriginalFilename()));
    }

    private Student getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    @DeleteMapping("/api/messages/{messageId}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        Student current = getCurrentUser();
        ChatMessage msg = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Tin nhắn không tồn tại"));

        if (!msg.getSenderId().equals(current.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn chỉ có thể thu hồi tin nhắn của mình");
        }

        msg.setDeleted(true);
        msg.setContent("Tin nhắn đã bị thu hồi");
        chatMessageRepository.save(msg);

        // Broadcast realtime cho người nhận
        messagingTemplate.convertAndSendToUser(
                msg.getReceiverId().toString(),
                "/queue/deleted",
                java.util.Map.of("messageId", messageId)
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping({"/api/messages/student/unread-count", "/api/messages/instructor/unread-count"})
    public ResponseEntity<Long> getUnreadCount() {
        Student current = getCurrentUser();
        return ResponseEntity.ok(chatMessageRepository.countUnreadMessages(current.getId()));
    }

    @PutMapping("/api/messages/read/{partnerId}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Void> markAsRead(@PathVariable Long partnerId) {
        Student current = getCurrentUser();
        chatMessageRepository.markMessagesAsRead(partnerId, current.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/messages/send")
    public ResponseEntity<ChatMessageDTO> sendMessageRest(@RequestBody ChatMessageRequest request) {
        Student sender = getCurrentUser();

        ChatMessage message = ChatMessage.builder()
                .senderId(sender.getId())
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();

        ChatMessage savedMsg = chatMessageRepository.save(message);

        ChatMessageDTO dto = ChatMessageDTO.builder()
                .id(savedMsg.getId())
                .senderId(sender.getId())
                .senderRole(sender.getRole().toString())
                .content(savedMsg.getContent())
                .createdAt(savedMsg.getTimestamp())
                .build();

        messagingTemplate.convertAndSendToUser(
                request.getReceiverId().toString(),
                "/queue/messages",
                dto
        );

        return ResponseEntity.ok(dto);
    }
}