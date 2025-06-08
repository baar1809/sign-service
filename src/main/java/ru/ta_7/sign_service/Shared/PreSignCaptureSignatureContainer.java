package ru.ta_7.sign_service.Shared;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.signatures.IExternalSignatureContainer;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

public class PreSignCaptureSignatureContainer implements IExternalSignatureContainer {
    private PdfDictionary sigDic;
    public PreSignCaptureSignatureContainer(PdfName filter, PdfName subFilter) {
        sigDic = new PdfDictionary();
        sigDic.put(PdfName.Filter, filter);
        sigDic.put(PdfName.SubFilter, subFilter);
    }

    @Getter
    private byte[] capturedContent;
    public static byte[] signedPDF = new byte[0];

    @Override
    public byte[] sign(InputStream is) throws GeneralSecurityException {
        try {
            this.capturedContent = is.readAllBytes();
        } catch (IOException e) {
            throw new GeneralSecurityException("Ошибка чтения данных для подписи", e);
        }
        return new byte[0]; // не подписываем — только сохраняем
    }

    @Override
    public void modifySigningDictionary(PdfDictionary signDic) {
        signDic.putAll(sigDic);
    }
}
