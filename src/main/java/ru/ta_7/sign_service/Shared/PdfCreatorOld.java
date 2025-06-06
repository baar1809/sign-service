package ru.ta_7.sign_service.Shared;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class PdfCreatorOld {
    static {
        System.setProperty("org.apache.pdfbox.forceSystemFontHandling", "false");
    }

    private static final String[] NAMES = {
        "Иванов И.И.", "Петров П.П.", "Сидоров С.С.",
        "Кузнецова А.В.", "Смирнова О.Л.", "Васильев Д.К."
    };

    private static final int margin = 100;

    public static byte[] createPdfWithSignatureField() throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDType0Font font = PDType0Font.load(document,
                PdfCreatorOld.class.getResourceAsStream("/fonts/timesnewromanpsmt.ttf"));

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Заголовок документа
                contentStream.setFont(font, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(100, 750);
                contentStream.showText("Достойные сотрудники");
                contentStream.endText();

                // Параметры таблицы
                float yStart = 700;
                float rowHeight = 20;
                float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
                float[] colWidths = {50, 100, 200}; // Ширина колонок: №, Дата, ФИО

                // Рисуем шапку таблицы
                drawTableHeader(contentStream, margin, yStart, colWidths, font);

                // Заполняем таблицу данными
                drawTableContent(contentStream, margin, yStart - rowHeight, colWidths, rowHeight, font);
            }

            // Добавляем поле для подписи
            addSignatureField(document, page);

            // Сохраняем в массив байт
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    private static void drawTableHeader(PDPageContentStream contentStream, float x, float y,
                                        float[] colWidths, PDType0Font font) throws Exception {
        contentStream.setFont(font, 12);
        String[] headers = {"№", "Дата", "ФИО"};

        for (int i = 0; i < headers.length; i++) {
            contentStream.beginText();
            contentStream.newLineAtOffset(x + 5, y - 15);
            contentStream.showText(headers[i]);
            contentStream.endText();
            x += colWidths[i];
        }

        // Рисуем линии таблицы
        contentStream.setLineWidth(1f);
        contentStream.moveTo(margin, y);
        contentStream.lineTo(margin + colWidths[0] + colWidths[1] + colWidths[2], y);
        contentStream.stroke();
    }

    private static void drawTableContent(PDPageContentStream contentStream, float xStart, float yStart,
                                         float[] colWidths, float rowHeight, PDType0Font font) throws Exception {
        contentStream.setFont(font, 10);
        Random random = new Random();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (int i = 1; i <= 5; i++) {
            float x = xStart;

            // №
            contentStream.beginText();
            contentStream.newLineAtOffset(x + 20, yStart - (i * rowHeight) + 5);
            contentStream.showText(String.valueOf(i));
            contentStream.endText();
            x += colWidths[0];

            // Дата
            LocalDate randomDate = LocalDate.now().minusDays(random.nextInt(365));
            contentStream.beginText();
            contentStream.newLineAtOffset(x + 10, yStart - (i * rowHeight) + 5);
            contentStream.showText(randomDate.format(dateFormat));
            contentStream.endText();
            x += colWidths[1];

            // ФИО
            contentStream.beginText();
            contentStream.newLineAtOffset(x + 10, yStart - (i * rowHeight) + 5);
            contentStream.showText(NAMES[random.nextInt(NAMES.length)]);
            contentStream.endText();

            // Линия разделителя строк
            contentStream.moveTo(margin, yStart - (i * rowHeight));
            contentStream.lineTo(margin + colWidths[0] + colWidths[1] + colWidths[2], yStart - (i * rowHeight));
            contentStream.stroke();
        }
    }

    private static void addSignatureField(PDDocument document, PDPage page) throws Exception {
        PDAcroForm acroForm = new PDAcroForm(document);
        PDSignatureField signatureField = new PDSignatureField(acroForm);
        signatureField.setPartialName("Signature1");

        PDAnnotationWidget widget = new PDAnnotationWidget();
        widget.setRectangle(new PDRectangle(100, 50, 200, 30)); // Настройте размеры
        widget.setPage(page);
        widget.setPrinted(true);
        signatureField.getWidgets().add(widget);
        acroForm.getFields().add(signatureField);
    }
}
