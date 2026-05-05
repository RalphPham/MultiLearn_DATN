package org.example.multileanproject.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.multileanproject.entity.Certificate;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.Order;
import org.example.multileanproject.entity.OrderDetail;
import org.example.multileanproject.entity.Student;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String FROM_EMAIL = "famutantempest@gmail.com";

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendBaseUrl;

    private final JavaMailSender mailSender;

    public void sendOrderSuccessEmail(Order order) {
        if (order == null || order.getStudent() == null) {
            return;
        }

        String toEmail = order.getStudent().getEmail();
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }

        String studentName = safeDisplayName(order.getStudent().getFullName());
        String orderCode = order.getId() != null ? "#" + order.getId() : "N/A";
        String createdAt = order.getCreatedAt() != null
                ? order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "N/A";

        StringBuilder itemsHtml = new StringBuilder();
        if (order.getOrderItems() != null) {
            for (OrderDetail item : order.getOrderItems()) {
                String courseTitle = item.getCourse() != null && item.getCourse().getTitle() != null
                        ? item.getCourse().getTitle()
                        : "Khóa học";

                String priceText = formatCurrency(item.getPrice());

                String thumbnail = item.getCourse() != null && item.getCourse().getThumbnail() != null
                        ? item.getCourse().getThumbnail().trim()
                        : "";

                String imageCell = isValidPublicImageUrl(thumbnail)
                        ? "<td style=\"padding:12px;border-bottom:1px solid #eeeeee;width:90px;\">" +
                        "<img src=\"" + escapeHtml(thumbnail) + "\" alt=\"Course\" " +
                        "style=\"width:72px;height:48px;object-fit:cover;border-radius:8px;border:1px solid #e5e7eb;display:block;\"/>" +
                        "</td>"
                        : "<td style=\"padding:12px;border-bottom:1px solid #eeeeee;width:90px;\">" +
                        "<div style=\"width:72px;height:48px;border-radius:8px;border:1px solid #e5e7eb;background:#f8fafc;" +
                        "display:flex;align-items:center;justify-content:center;color:#94a3b8;font-size:11px;font-weight:600;\">No image</div>" +
                        "</td>";

                itemsHtml.append("<tr>")
                        .append(imageCell)
                        .append("<td style=\"padding:12px;border-bottom:1px solid #eeeeee;\">")
                        .append(escapeHtml(courseTitle))
                        .append("</td>")
                        .append("<td style=\"padding:12px;border-bottom:1px solid #eeeeee;text-align:right;font-weight:bold;color:#2c3e50;\">")
                        .append(priceText)
                        .append("</td>")
                        .append("</tr>");
            }
        }

        BigDecimal originalAmount = order.getOriginalAmount() != null ? order.getOriginalAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = order.getFinalAmount() != null ? order.getFinalAmount() : BigDecimal.ZERO;
        BigDecimal discountAmount = originalAmount.subtract(finalAmount).max(BigDecimal.ZERO);

        String discountHtml = "";
        if (order.getCouponCode() != null && !order.getCouponCode().isBlank()) {
            discountHtml =
                    "<tr>" +
                            "<td style=\"padding:8px 12px;text-align:right;color:#7f8c8d;\">Mã giảm giá (" + escapeHtml(order.getCouponCode()) + "):</td>" +
                            "<td style=\"padding:8px 12px;text-align:right;color:#e74c3c;\">-" + formatCurrency(discountAmount) + "</td>" +
                            "</tr>";
        }

        String noteHtml = "";
        if (order.getNote() != null && !order.getNote().isBlank()) {
            noteHtml =
                    "<div style=\"background-color:#fffbeb;padding:12px 14px;border-radius:10px;margin-top:20px;color:#92400e;border:1px solid #fde68a;\">" +
                            "<strong>Ghi chú:</strong> " + escapeHtml(order.getNote()) +
                            "</div>";
        }

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Xác nhận đơn hàng thành công</title>
            </head>
            <body style="font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;background-color:#f4f7f6;margin:0;padding:24px;">
                <div style="max-width:680px;margin:0 auto;background-color:#ffffff;border-radius:16px;box-shadow:0 8px 24px rgba(0,0,0,0.08);overflow:hidden;border:1px solid #e5e7eb;">
                    <div style="background:linear-gradient(135deg,#2563eb 0%%,#1d4ed8 100%%);color:#ffffff;padding:28px 24px;text-align:center;">
                        <h1 style="margin:0;font-size:26px;">Xác Nhận Đơn Hàng Thành Công</h1>
                    </div>
                    <div style="padding:30px;color:#333333;line-height:1.6;">
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Cảm ơn bạn đã tin tưởng và lựa chọn các khóa học tại <strong>EduStar</strong>. Đơn hàng của bạn đã được thanh toán thành công và các khóa học đã được kích hoạt.</p>

                        <div style="background-color:#f8fafc;padding:16px;border-radius:12px;margin-bottom:24px;border-left:4px solid #2563eb;">
                            <p style="margin:4px 0;font-size:14px;"><strong>Mã đơn hàng:</strong> %s</p>
                            <p style="margin:4px 0;font-size:14px;"><strong>Ngày giao dịch:</strong> %s</p>
                        </div>

                        <table style="width:100%%;border-collapse:collapse;margin-bottom:20px;">
                            <thead>
                                <tr>
                                    <th style="background-color:#f1f5f9;padding:12px;text-align:left;font-size:14px;color:#64748b;width:90px;">Ảnh</th>
                                    <th style="background-color:#f1f5f9;padding:12px;text-align:left;font-size:14px;color:#64748b;">Tên khóa học</th>
                                    <th style="background-color:#f1f5f9;padding:12px;text-align:right;font-size:14px;color:#64748b;">Giá tiền</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>

                        <table style="width:100%%;margin-top:20px;">
                            <tr>
                                <td style="padding:8px 12px;text-align:right;color:#7f8c8d;">Tạm tính:</td>
                                <td style="padding:8px 12px;text-align:right;font-weight:bold;">%s</td>
                            </tr>
                            %s
                            <tr>
                                <td style="padding:12px;text-align:right;border-top:2px solid #eeeeee;font-size:18px;font-weight:bold;color:#1e293b;">Tổng thanh toán:</td>
                                <td style="padding:12px;text-align:right;border-top:2px solid #eeeeee;font-size:18px;font-weight:bold;color:#2563eb;">%s</td>
                            </tr>
                        </table>

                        %s

                        <div style="text-align:center;margin-top:35px;">
                            <a href="%s/my-courses" style="background-color:#10b981;color:white;padding:14px 28px;text-decoration:none;border-radius:10px;font-weight:bold;font-size:16px;display:inline-block;">Vào Học Ngay</a>
                        </div>
                    </div>
                    <div style="text-align:center;padding:20px;color:#94a3b8;font-size:12px;background-color:#f8fafc;border-top:1px solid #eeeeee;">
                        <p style="margin:0 0 6px 0;">Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi qua email hỗ trợ.</p>
                        <p style="margin:0;">&copy; 2026 EduStar. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.formatted(
                studentName,
                orderCode,
                createdAt,
                itemsHtml.toString(),
                formatCurrency(originalAmount),
                discountHtml,
                formatCurrency(finalAmount),
                noteHtml,
                frontendBaseUrl
        );

        sendHtmlEmail(toEmail, "🎉 Xác nhận mua khóa học thành công - EduStar", htmlContent);
    }

    public void sendCourseCompletionEmail(Student student, Course course) {
        if (student == null || course == null || student.getEmail() == null || student.getEmail().isBlank()) {
            return;
        }

        String studentName = safeDisplayName(student.getFullName());
        String courseTitle = course.getTitle() != null && !course.getTitle().isBlank()
                ? course.getTitle()
                : "khóa học";

        String completedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String thumbnail = course.getThumbnail() != null ? course.getThumbnail().trim() : "";
        String imageHtml = buildCourseImageBlock(thumbnail, courseTitle);
        String courseUrl = course.getId() != null
                ? frontendBaseUrl + "/learning/course/" + course.getId()
                : frontendBaseUrl + "/my-courses";

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Hoàn thành khóa học</title></head>
            <body style="margin:0;padding:0;background:#f4f7fb;font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;color:#1f2937;">
                <div style="max-width:640px;margin:32px auto;background:#ffffff;border-radius:16px;overflow:hidden;border:1px solid #e5e7eb;box-shadow:0 8px 24px rgba(15,23,42,0.08);">
                    <div style="background:linear-gradient(135deg,#2563eb 0%%,#1d4ed8 100%%);padding:32px 24px;text-align:center;color:#ffffff;">
                        <div style="font-size:40px;line-height:1;margin-bottom:10px;">🎓</div>
                        <h1 style="margin:0;font-size:28px;font-weight:700;">Chúc mừng bạn đã hoàn thành khóa học!</h1>
                        <p style="margin:10px 0 0 0;font-size:15px;opacity:0.95;">Một cột mốc tuyệt vời trên hành trình học tập của bạn tại EduStar</p>
                    </div>
                    <div style="padding:32px 24px;">
                        <p style="margin:0 0 16px 0;">Xin chào <strong>%s</strong>,</p>
                        <p style="margin:0 0 14px 0;line-height:1.7;">Chúc mừng bạn đã hoàn thành khóa học <strong>%s</strong>.</p>
                        <p style="margin:0 0 14px 0;line-height:1.7;">Đây là một thành quả rất đáng tự hào. Cảm ơn bạn đã đồng hành cùng EduStar trong quá trình học tập và phát triển kỹ năng.</p>
                        %s
                        <div style="background:#f8fafc;border:1px solid #e5e7eb;border-left:4px solid #2563eb;border-radius:12px;padding:16px 18px;margin:24px 0;">
                            <p style="margin:0 0 8px 0;font-size:14px;"><strong>Khóa học:</strong> %s</p>
                            <p style="margin:0;font-size:14px;"><strong>Thời gian hoàn thành:</strong> %s</p>
                        </div>
                        <div style="text-align:center;margin:28px 0 12px 0;">
                            <a href="%s" style="display:inline-block;background:#10b981;color:#ffffff;text-decoration:none;padding:14px 24px;border-radius:10px;font-weight:700;font-size:15px;">Vào học lại</a>
                        </div>
                    </div>
                    <div style="padding:18px 24px;background:#f8fafc;border-top:1px solid #e5e7eb;text-align:center;color:#64748b;font-size:12px;">
                        <p style="margin:0 0 6px 0;">EduStar - Learn smarter, grow faster.</p>
                        <p style="margin:0;">&copy; 2026 EduStar. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.formatted(
                studentName,
                escapeHtml(courseTitle),
                imageHtml,
                escapeHtml(courseTitle),
                completedAt,
                courseUrl
        );

        sendHtmlEmail(student.getEmail(), "🎓 Chúc mừng bạn đã hoàn thành khóa học - EduStar", htmlContent);
    }

    public void sendCertificateIssuedEmail(Student student, Course course, Certificate certificate) {
        if (student == null || course == null || certificate == null || student.getEmail() == null || student.getEmail().isBlank()) {
            return;
        }

        String studentName = safeDisplayName(student.getFullName());
        String courseTitle = course.getTitle() != null && !course.getTitle().isBlank()
                ? course.getTitle()
                : "khóa học";

        String thumbnail = course.getThumbnail() != null ? course.getThumbnail().trim() : "";
        String imageHtml = buildCourseImageBlock(thumbnail, courseTitle);

        String verifyUrl = certificate.getVerifyUrl() != null && !certificate.getVerifyUrl().isBlank()
                ? certificate.getVerifyUrl()
                : frontendBaseUrl + "/certificate/verify/" + certificate.getCertificateCode();

        String pdfUrl = certificate.getPdfUrl() != null ? certificate.getPdfUrl().trim() : "";

        String issuedAt = certificate.getIssuedAt() != null
                ? certificate.getIssuedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        String completedAt = certificate.getCompletedAt() != null
                ? certificate.getCompletedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : issuedAt;

        String pdfButtonHtml = isValidHttpUrl(pdfUrl)
                ? "<a href=\"" + escapeHtml(pdfUrl) + "\" " +
                "style=\"display:inline-block;background:#10b981;color:#ffffff;text-decoration:none;padding:14px 24px;border-radius:10px;font-weight:700;font-size:15px;margin-left:12px;\">" +
                "Tải PDF chứng chỉ</a>"
                : "";

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Chứng chỉ đã được cấp</title></head>
            <body style="margin:0;padding:0;background:#f8fafc;font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;color:#1f2937;">
                <div style="max-width:680px;margin:32px auto;background:#ffffff;border-radius:16px;overflow:hidden;border:1px solid #e5e7eb;box-shadow:0 10px 30px rgba(15,23,42,0.08);">
                    <div style="background:linear-gradient(135deg,#0f766e 0%%,#0ea5a4 100%%);padding:34px 24px;text-align:center;color:#ffffff;">
                        <div style="font-size:42px;line-height:1;margin-bottom:10px;">🏅</div>
                        <h1 style="margin:0;font-size:30px;font-weight:700;">Chứng chỉ của bạn đã sẵn sàng</h1>
                        <p style="margin:10px 0 0 0;font-size:15px;opacity:0.95;">EduStar ghi nhận thành tích học tập của bạn</p>
                    </div>

                    <div style="padding:32px 24px;">
                        <p style="margin:0 0 14px 0;">Xin chào <strong>%s</strong>,</p>
                        <p style="margin:0 0 14px 0;line-height:1.7;">
                            Bạn đã được cấp chứng chỉ hoàn thành cho khóa học <strong>%s</strong>.
                        </p>
                        <p style="margin:0 0 8px 0;line-height:1.7;color:#475569;">
                            Chúc mừng bạn đã hoàn thành một cột mốc quan trọng trên hành trình học tập của mình.
                        </p>

                        %s

                        <div style="background:#f8fafc;border:1px solid #e5e7eb;border-radius:14px;padding:18px 20px;margin:24px 0;">
                            <p style="margin:0 0 10px 0;font-size:14px;"><strong>Mã chứng chỉ:</strong> %s</p>
                            <p style="margin:0 0 10px 0;font-size:14px;"><strong>Khóa học:</strong> %s</p>
                            <p style="margin:0 0 10px 0;font-size:14px;"><strong>Ngày hoàn thành:</strong> %s</p>
                            <p style="margin:0 0 10px 0;font-size:14px;"><strong>Ngày cấp:</strong> %s</p>
                            <p style="margin:0;font-size:14px;"><strong>Trạng thái:</strong> Hợp lệ</p>
                        </div>

                        <div style="text-align:center;margin-top:28px;">
                            <a href="%s" style="display:inline-block;background:#2563eb;color:#ffffff;text-decoration:none;padding:14px 24px;border-radius:10px;font-weight:700;font-size:15px;">Xem / xác minh chứng chỉ</a>
                            %s
                        </div>
                    </div>

                    <div style="padding:18px 24px;background:#f8fafc;border-top:1px solid #e5e7eb;text-align:center;color:#64748b;font-size:12px;">
                        <p style="margin:0 0 6px 0;">EduStar - Learn smarter, grow faster.</p>
                        <p style="margin:0;">&copy; 2026 EduStar. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.formatted(
                studentName,
                escapeHtml(courseTitle),
                imageHtml,
                escapeHtml(certificate.getCertificateCode()),
                escapeHtml(courseTitle),
                completedAt,
                issuedAt,
                escapeHtml(verifyUrl),
                pdfButtonHtml
        );

        sendHtmlEmail(student.getEmail(), "🏅 Chứng chỉ của bạn đã sẵn sàng - EduStar", htmlContent);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(FROM_EMAIL);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("HTML email sent successfully to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    private String buildCourseImageBlock(String thumbnail, String courseTitle) {
        if (!isValidPublicImageUrl(thumbnail)) {
            return "";
        }

        return "<div style=\"text-align:center;margin:24px 0;\">" +
                "<img src=\"" + escapeHtml(thumbnail) + "\" alt=\"" + escapeHtml(courseTitle) + "\" " +
                "style=\"width:100%;max-width:420px;border-radius:12px;border:1px solid #e5e7eb;display:block;margin:0 auto;object-fit:cover;\"/>" +
                "</div>";
    }

    private boolean isValidPublicImageUrl(String value) {
        return isValidHttpUrl(value)
                && !value.contains("localhost")
                && !value.contains("127.0.0.1");
    }

    private boolean isValidHttpUrl(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String lower = value.trim().toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private String formatCurrency(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN"))
                .format(value != null ? value : BigDecimal.ZERO);
    }

    private String safeDisplayName(String value) {
        return value != null && !value.isBlank() ? escapeHtml(value) : "bạn";
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // ====================================================================================
    // EMAIL CHÀO MỪNG KHI TẠO TÀI KHOẢN
    // ====================================================================================

    public void sendWelcomeEmail(String toEmail, String fullName) {
        if (toEmail == null || toEmail.isBlank()) return;

        String safeName = safeDisplayName(fullName);
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Chào mừng đến với MultiLearn</title></head>
            <body style="margin:0;padding:0;background:#f4f7fb;font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;color:#1f2937;">
                <div style="max-width:620px;margin:32px auto;background:#ffffff;border-radius:16px;overflow:hidden;border:1px solid #e5e7eb;box-shadow:0 8px 24px rgba(15,23,42,0.08);">
                    <div style="background:linear-gradient(135deg,#2563eb 0%%,#7c3aed 100%%);padding:36px 24px;text-align:center;color:#ffffff;">
                        <div style="font-size:44px;line-height:1;margin-bottom:12px;">🎉</div>
                        <h1 style="margin:0;font-size:28px;font-weight:700;">Chào mừng đến với MultiLearn!</h1>
                        <p style="margin:10px 0 0 0;font-size:15px;opacity:0.9;">Hành trình học tập của bạn bắt đầu từ đây</p>
                    </div>
                    <div style="padding:36px 28px;">
                        <p style="margin:0 0 16px 0;font-size:16px;">Xin chào <strong>%s</strong>,</p>
                        <p style="margin:0 0 14px 0;line-height:1.7;color:#374151;">
                            Chúc mừng bạn đã tạo tài khoản thành công trên <strong>MultiLearn</strong> — nền tảng học trực tuyến với hàng trăm khóa học chất lượng cao từ các giảng viên hàng đầu.
                        </p>
                        <p style="margin:0 0 24px 0;line-height:1.7;color:#374151;">
                            Tài khoản của bạn đã sẵn sàng. Hãy khám phá các khóa học phù hợp với bạn ngay hôm nay!
                        </p>

                        <div style="background:#f8fafc;border:1px solid #e5e7eb;border-radius:12px;padding:18px 20px;margin-bottom:28px;">
                            <p style="margin:0 0 8px 0;font-size:14px;color:#6b7280;"><strong style="color:#1f2937;">Email:</strong> %s</p>
                            <p style="margin:0;font-size:14px;color:#6b7280;"><strong style="color:#1f2937;">Trạng thái:</strong> <span style="color:#10b981;font-weight:600;">Hoạt động</span></p>
                        </div>

                        <div style="text-align:center;margin-bottom:12px;">
                            <a href="%s/home" style="display:inline-block;background:linear-gradient(135deg,#2563eb,#7c3aed);color:#ffffff;text-decoration:none;padding:14px 32px;border-radius:10px;font-weight:700;font-size:15px;">Khám phá khóa học ngay</a>
                        </div>
                    </div>
                    <div style="padding:18px 24px;background:#f8fafc;border-top:1px solid #e5e7eb;text-align:center;color:#9ca3af;font-size:12px;">
                        <p style="margin:0 0 4px 0;">Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email.</p>
                        <p style="margin:0;">&copy; 2026 MultiLearn. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.formatted(safeName, escapeHtml(toEmail), frontendBaseUrl);

        sendHtmlEmail(toEmail, "🎉 Chào mừng bạn đến với MultiLearn!", htmlContent);
    }

    // ====================================================================================
    // 🔥 TÍNH NĂNG MỚI: EMAIL KHÓA TÀI KHOẢN & EMAIL CẢNH BÁO
    // ====================================================================================

    public void sendAccountLockedEmail(String toEmail, String studentName, String reason) {
        if (toEmail == null || toEmail.isBlank()) return;

        String safeName = safeDisplayName(studentName);
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Tài khoản bị khóa</title></head>
            <body style="font-family:'Segoe UI',Tahoma,sans-serif;background-color:#f4f7f6;margin:0;padding:24px;">
                <div style="max-width:600px;margin:0 auto;background-color:#ffffff;border-radius:16px;overflow:hidden;border:1px solid #fee2e2;">
                    <div style="background-color:#ef4444;color:#ffffff;padding:24px;text-align:center;">
                        <h1 style="margin:0;font-size:24px;">⚠️ Thông báo Khóa tài khoản</h1>
                    </div>
                    <div style="padding:30px;color:#333;line-height:1.6;">
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Chúng tôi rất tiếc phải thông báo rằng tài khoản EduStar của bạn đã bị <strong>tạm khóa</strong> do vi phạm chính sách của hệ thống.</p>
                        <div style="background-color:#fef2f2;padding:16px;border-radius:12px;margin:20px 0;border-left:4px solid #ef4444;">
                            <p style="margin:0;color:#991b1b;font-size:14px;"><strong>Lý do khóa:</strong> %s</p>
                        </div>
                        <p>Nếu bạn cho rằng đây là sự nhầm lẫn, vui lòng truy cập trang đăng nhập và gửi <strong>Đơn khiếu nại</strong> để được hỗ trợ.</p>
                        <br>
                        <p>Trân trọng,<br><strong>Đội ngũ Quản trị EduStar</strong></p>
                    </div>
                </div>
            </body>
            </html>
        """.formatted(safeName, escapeHtml(reason));

        sendHtmlEmail(toEmail, "⚠️ [EduStar] Thông báo Khóa tài khoản", htmlContent);
    }

    public void sendAccountUnlockedEmail(String toEmail, String studentName, String reason) {
        if (toEmail == null || toEmail.isBlank()) return;

        String safeName = safeDisplayName(studentName);
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Mở khóa tài khoản</title></head>
            <body style="font-family:'Segoe UI',Tahoma,sans-serif;background-color:#f4f7f6;margin:0;padding:24px;">
                <div style="max-width:600px;margin:0 auto;background-color:#ffffff;border-radius:16px;overflow:hidden;border:1px solid #d1fae5;">
                    <div style="background-color:#10b981;color:#ffffff;padding:24px;text-align:center;">
                        <h1 style="margin:0;font-size:24px;">🎉 Thông báo Mở khóa tài khoản</h1>
                    </div>
                    <div style="padding:30px;color:#333;line-height:1.6;">
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Chúng tôi rất vui báo tin tài khoản EduStar của bạn đã được <strong>MỞ KHÓA</strong> và có thể hoạt động lại bình thường.</p>
                        <div style="background-color:#ecfdf5;padding:16px;border-radius:12px;margin:20px 0;border-left:4px solid #10b981;">
                            <p style="margin:0;color:#065f46;font-size:14px;"><strong>Chi tiết quyết định:</strong> %s</p>
                        </div>
                        <p>Bạn có thể đăng nhập vào hệ thống và tiếp tục hành trình học tập của mình ngay bây giờ.</p>
                        <br>
                        <p>Trân trọng,<br><strong>Đội ngũ Quản trị EduStar</strong></p>
                    </div>
                </div>
            </body>
            </html>
        """.formatted(safeName, escapeHtml(reason));

        sendHtmlEmail(toEmail, "🎉 [EduStar] Thông báo Mở khóa tài khoản", htmlContent);
    }

    public void sendWarningEmail(String toEmail, String studentName, String reason) {
        if (toEmail == null || toEmail.isBlank()) return;

        String safeName = safeDisplayName(studentName);
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Cảnh báo vi phạm</title></head>
            <body style="font-family:'Segoe UI',Tahoma,sans-serif;background-color:#f4f7f6;margin:0;padding:24px;">
                <div style="max-width:600px;margin:0 auto;background-color:#ffffff;border-radius:16px;overflow:hidden;border:1px solid #fef08a;">
                    <div style="background-color:#f59e0b;color:#ffffff;padding:24px;text-align:center;">
                        <h1 style="margin:0;font-size:24px;">🚨 Cảnh báo Vi phạm Chính sách</h1>
                    </div>
                    <div style="padding:30px;color:#333;line-height:1.6;">
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Tài khoản của bạn vừa được Mở Khóa, tuy nhiên chúng tôi gửi email này để <strong>cảnh cáo</strong> về hành vi vi phạm trước đó của bạn.</p>
                        <div style="background-color:#fffbeb;padding:16px;border-radius:12px;margin:20px 0;border-left:4px solid #f59e0b;">
                            <p style="margin:0;color:#92400e;font-size:14px;"><strong>Lý do vi phạm:</strong> %s</p>
                        </div>
                        <p>Nếu tiếp tục vi phạm, tài khoản của bạn sẽ bị khóa vĩnh viễn và không thể khiếu nại. Mong bạn tuân thủ các quy định của hệ thống để có môi trường học tập tốt nhất.</p>
                        <br>
                        <p>Trân trọng,<br><strong>Đội ngũ Quản trị EduStar</strong></p>
                    </div>
                </div>
            </body>
            </html>
        """.formatted(safeName, escapeHtml(reason));

        sendHtmlEmail(toEmail, "🚨 [EduStar] Cảnh báo Vi phạm Chính sách", htmlContent);
    }
}