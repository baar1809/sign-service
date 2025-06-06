package ru.ta_7.sign_service.Http;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.signatures.ExternalBlankSignatureContainer;
import com.itextpdf.signatures.IExternalSignatureContainer;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import lombok.extern.slf4j.Slf4j;
import com.itextpdf.kernel.geom.Rectangle;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import ru.ta_7.sign_service.Shared.PdfCreator;
import ru.ta_7.sign_service.Shared.PdfCreatorOld;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pdf")
public class MainController {
    // Первый вариант без itext
//    @GetMapping(value = "/generate", produces = MediaType.APPLICATION_PDF_VALUE)
//    public byte[] generatePdf() throws Exception {
//        return PdfCreatorOld.createPdfWithSignatureField();
//    }
//
//    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<byte[]> signPdf(
//        @RequestParam("pdf") MultipartFile pdfFile,
//        @RequestParam("signature") String base64Signature
//    ) {
//        try {
//            byte[] pdfBytes = pdfFile.getBytes();
//            byte[] signatureBytes = Base64.getDecoder().decode(base64Signature);
//
//            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
//
//                PDPage page = document.getPage(0);
//
//                PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
//                if (acroForm == null) {
//                    acroForm = new PDAcroForm(document);
//                    document.getDocumentCatalog().setAcroForm(acroForm);
//                }
//                PDSignatureField signatureField = new PDSignatureField(acroForm);
//                signatureField.setPartialName("Signature1");
//                acroForm.getFields().add(signatureField);
//
//                PDAnnotationWidget widget = new PDAnnotationWidget();
//                PDRectangle rect = new PDRectangle(100, 600, 200, 50); // x, y, width, height
//                widget.setRectangle(rect);
//                widget.setPage(page);
//                widget.setPrinted(true);
//                signatureField.getWidgets().add(widget);
//
//                page.getAnnotations().add(widget);
//
//                PDSignature pdSignature = new PDSignature();
//                pdSignature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
//                pdSignature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
//                pdSignature.setName("Подписант");
//                pdSignature.setLocation("Москва");
//                pdSignature.setReason("Подпись документа");
//                pdSignature.setSignDate(Calendar.getInstance());
//
//                PDAppearanceStream appearanceStream = new PDAppearanceStream(document);
//                appearanceStream.setResources(new PDResources());
//                appearanceStream.setBBox(rect);
//
//                PDAppearanceDictionary appearanceDict = new PDAppearanceDictionary();
//                appearanceDict.setNormalAppearance(appearanceStream);
//                widget.setAppearance(appearanceDict);
//
//                PDType0Font font = PDType0Font.load(document,
//                    getClass().getResourceAsStream("/fonts/timesnewromanpsmt.ttf"));
//
//
//                try (PDPageContentStream cs = new PDPageContentStream(document, appearanceStream)) {
//                    cs.setNonStrokingColor(Color.BLACK);
//                    cs.setFont(font, 10);
//                    cs.beginText();
//                    cs.newLineAtOffset(10, rect.getHeight() - 15);
//                    cs.showText("Документ подписан");
//                    cs.newLineAtOffset(0, -12);
//                    cs.showText("Подписант");
//                    cs.endText();
//
//                    cs.setStrokingColor(Color.DARK_GRAY);
//                    cs.setLineWidth(1);
//                    cs.addRect(0, 0, rect.getWidth(), rect.getHeight());
//                    cs.stroke();
//                }
//

//                signatureField.setValue(pdSignature);
//                document.addSignature(pdSignature);
//
//                ByteArrayOutputStream signedPdf = new ByteArrayOutputStream();
//                ExternalSigningSupport signing = document.saveIncrementalForExternalSigning(signedPdf);
//                signing.setSignature(signatureBytes);
//
//                // Сохраняем файл
//                String outputDir = "signed_pdfs";
//                Files.createDirectories(Paths.get(outputDir));
//                String fileName = "signed_" + System.currentTimeMillis() + ".pdf";
//                Path outputPath = Paths.get(outputDir, fileName);
//                Files.write(outputPath, signedPdf.toByteArray());
//
//                return ResponseEntity.ok()
//                    .contentType(MediaType.APPLICATION_PDF)
//                    .body(signedPdf.toByteArray());
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError()
//                .body(("Ошибка: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
//        }
//    }

    // Второй вариант с itext (работает лучше)
    @GetMapping("/generate")
    public Map<String, String> generatePdf() throws Exception {
        byte[] pdfBytes = PdfCreator.createPdfWithSignatureField();
        return Map.of("pdfBase64", Base64.getEncoder().encodeToString(pdfBytes));
    }

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> signPdf(
        @RequestParam("pdf") MultipartFile pdfFile,
        @RequestParam("signature") String base64CmsSignature
    ) {
        try {
            byte[] pdfBytes = pdfFile.getBytes();
            byte[] cmsSignature = Base64.getDecoder().decode(base64CmsSignature);

            ByteArrayOutputStream signedPdfOutput = new ByteArrayOutputStream();

            PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
            reader.setUnethicalReading(true);

            PdfSigner signer = new PdfSigner(
                reader,
                signedPdfOutput,
                new StampingProperties().useAppendMode()
            );
            PdfFont font = PdfFontFactory.createFont("fonts/timesnewromanpsmt.ttf", PdfEncodings.IDENTITY_H);

            signer.setFieldName("Signature1");
            Rectangle rect = new Rectangle(100, 50, 200, 50);
            signer.getSignatureAppearance()
                .setLayer2Font(font)
                .setPageRect(rect)
                .setPageNumber(1)
                .setLayer2Text("Подписано с помощью PAdES")
                .setReason("Подписано")
                .setLocation("Россия");

            IExternalSignatureContainer externalSignatureContainer = new IExternalSignatureContainer() {
                @Override
                public byte[] sign(InputStream data) {
                    return cmsSignature;
                }

                @Override
                public void modifySigningDictionary(com.itextpdf.kernel.pdf.PdfDictionary signDic) {
                }
            };

            signer.signExternalContainer(externalSignatureContainer, cmsSignature.length);

            byte[] signedPdfBytes = signedPdfOutput.toByteArray();

            String outputDir = "signed_pdfs";
            Files.createDirectories(Paths.get(outputDir));
            String fileName = "signed_" + System.currentTimeMillis() + ".pdf";
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
