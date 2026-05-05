package org.example.multileanproject.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.multileanproject.entity.Order;
import org.example.multileanproject.entity.OrderDetail;
import org.example.multileanproject.entity.OrderStatus;
import org.example.multileanproject.entity.RefundRequest;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class OrderReceiptPdfServiceImpl implements OrderReceiptPdfService {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat VND_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    @Override
    public byte[] generateOrderReceipt(Order order, RefundRequest approvedRefund) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36f, 36f, 36f, 36f);
            PdfWriter.getInstance(document, output);
            document.open();

            BaseFont baseFont = loadUnicodeBaseFont();
            Font titleFont = new Font(baseFont, 18, Font.BOLD, new Color(17, 24, 39));
            Font subtitleFont = new Font(baseFont, 11, Font.NORMAL, new Color(71, 85, 105));
            Font sectionFont = new Font(baseFont, 13, Font.BOLD, new Color(31, 41, 55));
            Font labelFont = new Font(baseFont, 10, Font.BOLD, new Color(100, 116, 139));
            Font valueFont = new Font(baseFont, 11, Font.NORMAL, new Color(15, 23, 42));
            Font headerCellFont = new Font(baseFont, 10, Font.BOLD, Color.WHITE);

            Paragraph title = new Paragraph("MULTILEARN - BIEN NHAN GIAO DICH", titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            title.setSpacingAfter(4f);
            document.add(title);

            Paragraph subTitle = new Paragraph(
                    "Phat hanh luc: " + LocalDateTime.now().format(DATE_TIME_FMT),
                    subtitleFont
            );
            subTitle.setSpacingAfter(14f);
            document.add(subTitle);

            document.add(buildSummaryTable(order, approvedRefund, labelFont, valueFont));

            Paragraph sectionTitle = new Paragraph("Khoa hoc trong don", sectionFont);
            sectionTitle.setSpacingBefore(14f);
            sectionTitle.setSpacingAfter(8f);
            document.add(sectionTitle);

            document.add(buildItemsTable(order, headerCellFont, valueFont));

            Paragraph note = new Paragraph(
                    "Tai lieu nay la bien nhan giao dich noi bo. Neu don da hoan tien, bien nhan se hien trang thai DA HOAN TIEN.",
                    subtitleFont
            );
            note.setSpacingBefore(12f);
            document.add(note);

            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Khong the tao bien nhan PDF.", ex);
        }
    }

    private PdfPTable buildSummaryTable(Order order, RefundRequest approvedRefund, Font labelFont, Font valueFont)
            throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{1.2f, 2.8f});
        table.setSpacingAfter(8f);

        BigDecimal originalAmount = safeAmount(order.getOriginalAmount());
        BigDecimal finalAmount = safeAmount(order.getFinalAmount());
        BigDecimal discountAmount = originalAmount.subtract(finalAmount);
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            discountAmount = BigDecimal.ZERO;
        }
        BigDecimal refundAmount = approvedRefund != null ? safeAmount(approvedRefund.getAmount()) : BigDecimal.ZERO;

        addInfoRow(table, "Ma don hang", "#" + String.format("%04d", order.getId()), labelFont, valueFont);
        addInfoRow(table, "Thoi gian dat", formatDate(order.getCreatedAt()), labelFont, valueFont);
        addInfoRow(table, "Hoc vien", resolveStudentName(order), labelFont, valueFont);
        addInfoRow(table, "Trang thai", resolveStatus(order, approvedRefund), labelFont, valueFont);
        addInfoRow(table, "Phuong thuc", resolvePaymentMethod(order, approvedRefund), labelFont, valueFont);
        addInfoRow(table, "Ma giao dich", safeText(order.getTransactionRef(), "Khong co"), labelFont, valueFont);
        addInfoRow(table, "Tong gia goc", formatMoney(originalAmount), labelFont, valueFont);
        addInfoRow(table, "Giam gia", formatMoney(discountAmount), labelFont, valueFont);
        addInfoRow(table, "Tong thanh toan", formatMoney(finalAmount), labelFont, valueFont);

        if (approvedRefund != null) {
            addInfoRow(table, "So tien hoan", formatMoney(refundAmount), labelFont, valueFont);
            addInfoRow(table, "Thoi gian hoan", formatDate(approvedRefund.getResolvedAt()), labelFont, valueFont);
        }

        return table;
    }

    private PdfPTable buildItemsTable(Order order, Font headerFont, Font valueFont) {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{0.7f, 1.8f, 3.2f, 1.5f});

        addHeaderCell(table, "STT", headerFont);
        addHeaderCell(table, "Ma khoa hoc", headerFont);
        addHeaderCell(table, "Ten khoa hoc", headerFont);
        addHeaderCell(table, "Gia", headerFont);

        List<OrderDetail> items = order.getOrderItems();
        if (items == null || items.isEmpty()) {
            addValueCell(table, "-", valueFont, Element.ALIGN_CENTER);
            addValueCell(table, "-", valueFont, Element.ALIGN_CENTER);
            addValueCell(table, "Khong co du lieu", valueFont, Element.ALIGN_LEFT);
            addValueCell(table, formatMoney(BigDecimal.ZERO), valueFont, Element.ALIGN_RIGHT);
            return table;
        }

        int index = 1;
        for (OrderDetail item : items) {
            String title = item.getCourse() != null ? item.getCourse().getTitle() : "Khoa hoc";
            String courseCode = resolveCourseCode(item);
            BigDecimal price = safeAmount(item.getPrice());
            addValueCell(table, String.valueOf(index++), valueFont, Element.ALIGN_CENTER);
            addValueCell(table, courseCode, valueFont, Element.ALIGN_CENTER);
            addValueCell(table, safeText(title, "Khoa hoc"), valueFont, Element.ALIGN_LEFT);
            addValueCell(table, formatMoney(price), valueFont, Element.ALIGN_RIGHT);
        }

        return table;
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(7f);
        labelCell.setBackgroundColor(new Color(248, 250, 252));
        labelCell.setBorderColor(new Color(226, 232, 240));
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(7f);
        valueCell.setBorderColor(new Color(226, 232, 240));
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(7f);
        cell.setBackgroundColor(new Color(37, 99, 235));
        cell.setBorderColor(new Color(59, 130, 246));
        table.addCell(cell);
    }

    private void addValueCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(7f);
        cell.setBorderColor(new Color(226, 232, 240));
        table.addCell(cell);
    }

    private String resolveStatus(Order order, RefundRequest approvedRefund) {
        if (approvedRefund != null) {
            return "DA HOAN TIEN";
        }
        if (order.getStatus() == null) {
            return "PENDING";
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            return "DA THANH TOAN";
        }
        return order.getStatus().name();
    }

    private String resolvePaymentMethod(Order order, RefundRequest approvedRefund) {
        String method = order.getPaymentMethod();
        if (method != null && !method.isBlank()) {
            return method;
        }

        if (order.getFinalAmount() != null
                && order.getFinalAmount().compareTo(BigDecimal.ZERO) > 0
                && (order.getStatus() == OrderStatus.COMPLETED || approvedRefund != null)) {
            return "VNPAY";
        }

        return "Khong xac dinh";
    }

    private String resolveStudentName(Order order) {
        if (order.getStudent() == null || order.getStudent().getFullName() == null || order.getStudent().getFullName().isBlank()) {
            return "Hoc vien";
        }
        return order.getStudent().getFullName();
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String formatMoney(BigDecimal amount) {
        return VND_FORMAT.format(safeAmount(amount)) + " VND";
    }

    private String formatDate(LocalDateTime value) {
        return value != null ? value.format(DATE_TIME_FMT) : "--";
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private String resolveCourseCode(OrderDetail item) {
        if (item == null || item.getCourse() == null || item.getCourse().getId() == null) {
            return "-";
        }
        return "KH-" + item.getCourse().getId();
    }

    private BaseFont loadUnicodeBaseFont() {
        List<String> candidatePaths = List.of(
                "C:/Windows/Fonts/arial.ttf",
                "C:/Windows/Fonts/segoeui.ttf",
                "C:/Windows/Fonts/tahoma.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/Library/Fonts/Arial Unicode.ttf",
                "/System/Library/Fonts/Supplemental/Arial Unicode.ttf"
        );

        for (String path : candidatePaths) {
            try {
                File fontFile = new File(path);
                if (fontFile.exists()) {
                    return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                }
            } catch (Exception ignored) {
            }
        }

        try {
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            throw new RuntimeException("Khong the tai font cho PDF", e);
        }
    }
}
