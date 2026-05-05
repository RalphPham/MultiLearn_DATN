package org.example.multileanproject.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.Certificate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfCertificateServiceImpl implements PdfCertificateService {

    private static final String OUTPUT_DIR = "generated-certificates";

    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    @Override
    public String generateCertificatePdf(Certificate certificate) {

        try {
            File dir = new File(OUTPUT_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = certificate.getCertificateCode() + ".pdf";

            File file = new File(dir, fileName);

            Document document = new Document(PageSize.A4.rotate(), 50f, 50f, 50f, 50f);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

            document.open();

            drawBackground(writer, document.getPageSize());

            BaseFont baseFont = loadUnicodeBaseFont();
            Font badge = new Font(baseFont, 14, Font.BOLD, new Color(44, 76, 163));
            Font title = new Font(baseFont, 44, Font.BOLD, new Color(11, 25, 64));
            Font subTitle = new Font(baseFont, 18, Font.NORMAL, new Color(75, 85, 99));
            Font studentName = new Font(baseFont, 38, Font.BOLD, new Color(10, 33, 90));
            Font courseName = new Font(baseFont, 30, Font.BOLD, new Color(34, 95, 192));
            Font infoLabel = new Font(baseFont, 12, Font.BOLD, new Color(107, 114, 128));
            Font infoValue = new Font(baseFont, 13, Font.NORMAL, new Color(17, 24, 39));
            Font footer = new Font(baseFont, 12, Font.NORMAL, new Color(71, 85, 105));

            Paragraph badgeLine = new Paragraph("MULTILEARN OFFICIAL CERTIFICATE", badge);
            badgeLine.setAlignment(Element.ALIGN_CENTER);
            badgeLine.setSpacingAfter(12f);
            document.add(badgeLine);

            Paragraph p1 = new Paragraph("CERTIFICATE OF COMPLETION", title);
            p1.setAlignment(Element.ALIGN_CENTER);
            p1.setSpacingAfter(24f);
            document.add(p1);

            Paragraph p2 = new Paragraph("This certifies that", subTitle);
            p2.setAlignment(Element.ALIGN_CENTER);
            p2.setSpacingAfter(8f);
            document.add(p2);

            Paragraph p3 = new Paragraph(certificate.getStudent().getFullName(), studentName);
            p3.setAlignment(Element.ALIGN_CENTER);
            p3.setSpacingAfter(12f);
            document.add(p3);

            Paragraph p4 = new Paragraph("has successfully completed the course", subTitle);
            p4.setAlignment(Element.ALIGN_CENTER);
            p4.setSpacingAfter(8f);
            document.add(p4);

            Paragraph p5 = new Paragraph(certificate.getCourse().getTitle(), courseName);
            p5.setAlignment(Element.ALIGN_CENTER);
            p5.setSpacingAfter(26f);
            document.add(p5);

            document.add(buildInfoTable(certificate, infoLabel, infoValue));

            Paragraph p6 = new Paragraph(
                    "Issued by MultiLearn - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    footer
            );
            p6.setAlignment(Element.ALIGN_CENTER);
            p6.setSpacingBefore(22f);
            document.add(p6);

            document.close();

            return backendUrl + "/generated-certificates/" + fileName;

        } catch (Exception e) {

            throw new RuntimeException("Cannot generate certificate PDF", e);

        }
    }

    private void drawBackground(PdfWriter writer, Rectangle pageSize) {
        PdfContentByte under = writer.getDirectContentUnder();

        under.saveState();
        under.setColorFill(new Color(245, 248, 255));
        under.rectangle(pageSize.getLeft(), pageSize.getBottom(), pageSize.getWidth(), pageSize.getHeight());
        under.fill();
        under.restoreState();

        float frameMargin = 18f;
        under.saveState();
        under.setColorStroke(new Color(76, 110, 199));
        under.setLineWidth(2.8f);
        under.rectangle(
                pageSize.getLeft() + frameMargin,
                pageSize.getBottom() + frameMargin,
                pageSize.getWidth() - (frameMargin * 2),
                pageSize.getHeight() - (frameMargin * 2)
        );
        under.stroke();
        under.restoreState();

        under.saveState();
        under.setColorStroke(new Color(180, 197, 240));
        under.setLineWidth(1.2f);
        under.rectangle(
                pageSize.getLeft() + (frameMargin + 10),
                pageSize.getBottom() + (frameMargin + 10),
                pageSize.getWidth() - ((frameMargin + 10) * 2),
                pageSize.getHeight() - ((frameMargin + 10) * 2)
        );
        under.stroke();
        under.restoreState();
    }

    private PdfPTable buildInfoTable(Certificate certificate, Font labelFont, Font valueFont) {
        String completedAt = certificate.getCompletedAt() != null
                ? certificate.getCompletedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "-";

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(88f);
        table.setSpacingBefore(4f);
        table.setWidths(new float[]{1.1f, 2.9f});

        addCell(table, "Completion Date", labelFont, true);
        addCell(table, completedAt, valueFont, false);

        addCell(table, "Certificate Code", labelFont, true);
        addCell(table, certificate.getCertificateCode(), valueFont, false);

        addCell(table, "Verify URL", labelFont, true);
        addCell(table, certificate.getVerifyUrl(), valueFont, false);

        return table;
    }

    private void addCell(PdfPTable table, String value, Font font, boolean labelCell) {
        PdfPCell cell = new PdfPCell(new Paragraph(value != null ? value : "-", font));
        cell.setPaddingTop(7f);
        cell.setPaddingBottom(7f);
        cell.setPaddingLeft(8f);
        cell.setPaddingRight(8f);
        cell.setBorderColor(new Color(204, 214, 240));
        cell.setBackgroundColor(labelCell ? new Color(236, 241, 255) : Color.WHITE);
        table.addCell(cell);
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
            throw new RuntimeException("Cannot load font for certificate PDF", e);
        }
    }
}
