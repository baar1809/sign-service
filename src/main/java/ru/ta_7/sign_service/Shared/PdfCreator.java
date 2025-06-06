package ru.ta_7.sign_service.Shared;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Random;

public class PdfCreator {

    private static final String[] NAMES = {
        "Иванов И.И.", "Петров П.П.", "Сидоров С.С.",
        "Кузнецова А.В.", "Смирнова О.Л.", "Васильев Д.К."
    };

    public static byte[] createPdfWithSignatureField() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc);

        PdfFont font = PdfFontFactory.createFont("fonts/timesnewromanpsmt.ttf", PdfEncodings.IDENTITY_H);
        doc.setFont(font);

        // Заголовок
        Paragraph title = new Paragraph("Достойные сотрудники")
            .setFontSize(16)
            .setTextAlignment(TextAlignment.CENTER);
        doc.add(title);

        // Таблица
        float[] columnWidths = {50f, 100f, 200f};
        Table table = new Table(columnWidths);
        table.addHeaderCell("№");
        table.addHeaderCell("Дата");
        table.addHeaderCell("ФИО");

        Random random = new Random();
        for (int i = 1; i <= 5; i++) {
            table.addCell(String.valueOf(i));
            table.addCell(LocalDate.now().minusDays(random.nextInt(365)).toString());
            table.addCell(NAMES[random.nextInt(NAMES.length)]);
        }

        doc.add(table);

        // Поле подписи (нижняя часть страницы)
        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
        Rectangle signatureRect = new Rectangle(100, 100, 200, 50);
        PdfFormField signatureField = PdfFormField.createSignature(pdfDoc, signatureRect);
        signatureField.setFieldName("Signature1");
        form.addField(signatureField);

        doc.close();
        return outputStream.toByteArray();
    }
}
