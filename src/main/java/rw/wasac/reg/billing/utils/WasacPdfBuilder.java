/**
 * Builds styled WASAC-branded PDF documents with the official blue water theme.
 */
package rw.wasac.reg.billing.utils;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public final class WasacPdfBuilder {

    public static final Color WASAC_BLUE = new Color(0, 102, 179);
    public static final Color WASAC_DARK = new Color(0, 64, 128);
    public static final Color WASAC_LIGHT = new Color(232, 244, 252);
    public static final Color WASAC_ACCENT = new Color(0, 163, 224);
    public static final Color TEXT_DARK = new Color(51, 51, 51);
    public static final Color TEXT_MUTED = new Color(120, 120, 120);

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.WHITE);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(204, 229, 255));
    private static final Font HEADING_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, WASAC_DARK);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 11, TEXT_DARK);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED);
    private static final Font TABLE_HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final Font TABLE_BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_DARK);

    private WasacPdfBuilder() {
    }

    public static byte[] build(String documentTitle, Consumer<Document> contentWriter) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, output);
            document.open();
            addHeader(document, documentTitle);
            contentWriter.accept(document);
            addFooter(document);
            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate PDF document: " + ex.getMessage(), ex);
        }
    }

    public static void addSectionTitle(Document document, String title) throws DocumentException {
        Paragraph section = new Paragraph(title, HEADING_FONT);
        section.setSpacingBefore(14f);
        section.setSpacingAfter(8f);
        document.add(section);

        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBorder(Rectangle.NO_BORDER);
        lineCell.setFixedHeight(3f);
        lineCell.setBackgroundColor(WASAC_ACCENT);
        line.addCell(lineCell);
        line.setSpacingAfter(10f);
        document.add(line);
    }

    public static void addKeyValueRow(Document document, String label, String value) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35f, 65f});
        addLabelValueCell(table, label, true);
        addLabelValueCell(table, value, false);
        table.setSpacingAfter(4f);
        document.add(table);
    }

    public static void addDataTable(Document document, String[] headers, List<String[]> rows) throws DocumentException {
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setSpacingBefore(6f);
        table.setSpacingAfter(12f);

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, TABLE_HEADER_FONT));
            cell.setBackgroundColor(WASAC_BLUE);
            cell.setPadding(8f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        boolean alternate = false;
        for (String[] row : rows) {
            Color bg = alternate ? WASAC_LIGHT : Color.WHITE;
            for (String value : row) {
                PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "-", TABLE_BODY_FONT));
                cell.setBackgroundColor(bg);
                cell.setPadding(6f);
                table.addCell(cell);
            }
            alternate = !alternate;
        }
        document.add(table);
    }

    public static void addParagraph(Document document, String text) throws DocumentException {
        Paragraph p = new Paragraph(text, BODY_FONT);
        p.setSpacingAfter(6f);
        document.add(p);
    }

    public static void addHighlightBox(Document document, String text) throws DocumentException {
        PdfPTable box = new PdfPTable(1);
        box.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, WASAC_DARK)));
        cell.setBackgroundColor(WASAC_LIGHT);
        cell.setBorderColor(WASAC_ACCENT);
        cell.setBorderWidth(1.5f);
        cell.setPadding(12f);
        box.addCell(cell);
        box.setSpacingAfter(12f);
        document.add(box);
    }

    private static void addHeader(Document document, String title) throws DocumentException {
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);

        PdfPCell brandCell = new PdfPCell();
        brandCell.setBackgroundColor(WASAC_BLUE);
        brandCell.setBorder(Rectangle.NO_BORDER);
        brandCell.setPadding(18f);

        Paragraph brand = new Paragraph("WASAC", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.WHITE));
        brand.setAlignment(Element.ALIGN_CENTER);
        Paragraph sub = new Paragraph("Water & Sanitation Corporation — Rwanda", SUBTITLE_FONT);
        sub.setAlignment(Element.ALIGN_CENTER);
        Paragraph utility = new Paragraph("Utility Billing System", SUBTITLE_FONT);
        utility.setAlignment(Element.ALIGN_CENTER);

        brandCell.addElement(brand);
        brandCell.addElement(sub);
        brandCell.addElement(utility);
        header.addCell(brandCell);

        PdfPCell titleCell = new PdfPCell(new Phrase(title, TITLE_FONT));
        titleCell.setBackgroundColor(WASAC_DARK);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPadding(10f);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(titleCell);

        header.setSpacingAfter(16f);
        document.add(header);
    }

    private static void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph(
                "Generated on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
                        + "  |  WASAC/REG Rwanda  |  wasac.rw",
                SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20f);
        document.add(footer);
    }

    private static void addLabelValueCell(PdfPTable table, String text, boolean label) {
        Font font = label
                ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, WASAC_DARK)
                : BODY_FONT;
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4f);
        table.addCell(cell);
    }
}
