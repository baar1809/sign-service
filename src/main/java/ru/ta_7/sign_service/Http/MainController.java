package ru.ta_7.sign_service.Http;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.signatures.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import ru.ta_7.sign_service.Shared.PreSignCaptureSignatureContainer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/api/pdf")
public class MainController {
    private static final String[] NAMES = {
        "Иванов И.И.", "Петров П.П.", "Сидоров С.С.",
        "Кузнецова А.В.", "Смирнова О.Л.", "Васильев Д.К."
    };

    @GetMapping("/generate")
    public Map<String, String> generatePdf() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc);

        PdfFont font = PdfFontFactory.createFont("fonts/timesnewromanpsmt.ttf", PdfEncodings.IDENTITY_H);
        doc.setFont(font);

        // Заголовок
        Paragraph title = new Paragraph("Сотрудники")
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
        doc.close();

        ByteArrayOutputStream signedOutput = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(new ByteArrayInputStream(outputStream.toByteArray()));
        PdfSigner signer = new PdfSigner(reader, signedOutput, new StampingProperties());
        PdfFont font2 = PdfFontFactory.createFont("fonts/timesnewromanpsmt.ttf", PdfEncodings.IDENTITY_H);

        signer.setFieldName("Signature1");
        signer.getSignatureAppearance()
            .setReason("Подпись")
            .setLocation("Мир")
            .setLayer2Font(font2)
            .setPageRect(new Rectangle(100, 100, 200, 50))
            .setPageNumber(1)
            .setLayer2Text("Место для подписи");

        PreSignCaptureSignatureContainer captureContainer = new PreSignCaptureSignatureContainer(PdfName.Adobe_PPKLite, PdfName.Adbe_pkcs7_detached);
        signer.signExternalContainer(captureContainer, 8192);
        PreSignCaptureSignatureContainer.signedPDF = signedOutput.toByteArray();

        byte[] pdfBytes = captureContainer.getCapturedContent();
        return Map.of("pdfBase64", Base64.getEncoder().encodeToString(pdfBytes));
    }

    @PostMapping(value = "/save")
    public ResponseEntity<byte[]> signPdf(
        @RequestBody SignatureRequest request
    ) {
        try {
            byte[] pdfBytes = PreSignCaptureSignatureContainer.signedPDF;
            byte[] cmsSignature = Base64.getDecoder().decode(request.getSignature());

            ByteArrayOutputStream signedPdfOutput = new ByteArrayOutputStream();
            PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));

            IExternalSignatureContainer externalSignatureContainer = new IExternalSignatureContainer() {
                @Override
                public byte[] sign(InputStream data) {
                    return cmsSignature;
                }

                @Override
                public void modifySigningDictionary(com.itextpdf.kernel.pdf.PdfDictionary signDic) {
                }
            };
            PdfSigner signer = new PdfSigner(reader, signedPdfOutput, new StampingProperties());
            PdfSigner.signDeferred(signer.getDocument(), "Signature1", signedPdfOutput, externalSignatureContainer);

            byte[] signedPdfBytes = signedPdfOutput.toByteArray();

            String outputDir = "signed_pdfs";
            Files.createDirectories(Paths.get(outputDir));
            String fileName = "signed.pdf";
            Files.write(Paths.get(outputDir, fileName), signedPdfBytes);

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(signedPdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(("Ошибка: " + e.getMessage()).getBytes());
        }
    }
}
