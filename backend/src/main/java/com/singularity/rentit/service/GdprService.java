package com.singularity.rentit.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.singularity.rentit.entity.GdprAgreement;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.exception.BusinessException;
import com.singularity.rentit.repository.GdprAgreementRepository;
import com.singularity.rentit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class GdprService {

    private final GdprAgreementRepository gdprAgreementRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final EmailService emailService;

    @Transactional
    public GdprAgreement signAgreement(User user, String ipAddress) {
        if (gdprAgreementRepository.existsByUserId(user.getId())) {
            throw new BusinessException("GDPR agreement already signed", HttpStatus.CONFLICT, "gdpr.already_signed");
        }

        LocalDateTime signedAt = LocalDateTime.now(ZoneId.of("Europe/Bucharest"));
        byte[] pdfBytes = generateGdprPdf(user, signedAt, ipAddress);
        String pdfUrl = storageService.uploadBytes(pdfBytes, "gdpr", "acord_gdpr.pdf", "application/pdf");

        GdprAgreement agreement = GdprAgreement.builder()
                .user(user)
                .signedAt(signedAt)
                .ipAddress(ipAddress)
                .pdfUrl(pdfUrl)
                .version("1.0")
                .build();

        GdprAgreement saved = gdprAgreementRepository.save(agreement);

        user.setGdprSigned(true);
        userRepository.save(user);

        emailService.sendGdprAgreement(user, pdfBytes);

        log.info("GDPR agreement signed by user {} from IP {}", user.getId(), ipAddress);
        return saved;
    }

    public boolean hasSignedGdpr(Long userId) {
        return gdprAgreementRepository.existsByUserId(userId);
    }

    private byte[] generateGdprPdf(User user, LocalDateTime signedAt, String ipAddress) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            String dateStr = signedAt.format(fmt);

            document.add(new Paragraph("ACORD DE PRELUCRARE A DATELOR PERSONALE")
                    .setFont(bold).setFontSize(16));
            document.add(new Paragraph("Conform Regulamentului (UE) 2016/679 (GDPR) si Legii nr. 190/2018")
                    .setFont(regular).setFontSize(10));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Utilizator: " + user.getFirstName() + " " + user.getLastName()).setFont(regular));
            document.add(new Paragraph("Email: " + user.getEmail()).setFont(regular));
            document.add(new Paragraph("Data semnarii: " + dateStr).setFont(regular));
            document.add(new Paragraph("IP: " + ipAddress).setFont(regular));
            document.add(new Paragraph("Versiune acord: 1.0").setFont(regular));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("DATE PRELUCRATE:").setFont(bold));
            document.add(new Paragraph(
                    "- Date de identificare: nume, prenume, email, telefon\n" +
                    "- Date KYC: serie CI, CNP, fotografie, selfie\n" +
                    "- Date financiare: tranzactii, plati, garantii\n" +
                    "- Date tehnice: adresa IP, date de utilizare a platformei"
            ).setFont(regular));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("SCOPUL PRELUCRARII:").setFont(bold));
            document.add(new Paragraph(
                    "Prestarea serviciilor platformei RentIt, inclusiv verificarea identitatii, " +
                    "procesarea platilor si prevenirea fraudelor."
            ).setFont(regular));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("DREPTURILE DUMNEAVOASTRA:").setFont(bold));
            document.add(new Paragraph(
                    "- Dreptul de acces la datele personale\n" +
                    "- Dreptul la rectificarea datelor inexacte\n" +
                    "- Dreptul la stergerea datelor (dreptul de a fi uitat)\n" +
                    "- Dreptul la portabilitatea datelor\n" +
                    "- Dreptul de a va opune prelucrarii\n\n" +
                    "Pentru exercitarea drepturilor: dpo@rentit.ro"
            ).setFont(regular));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "Prin semnarea electronica a acestui acord, confirmati ca ati citit, inteles si " +
                    "sunteti de acord cu prelucrarea datelor personale conform celor descrise mai sus.\n\n" +
                    "Temeiul legal: Art. 6(1)(a) GDPR - consimtamantul persoanei vizate."
            ).setFont(regular));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Semnat electronic la: " + dateStr).setFont(bold));
            document.add(new Paragraph("IP: " + ipAddress).setFont(regular));

            document.close();
        } catch (Exception e) {
            log.error("Failed to generate GDPR PDF", e);
            throw new RuntimeException("GDPR PDF generation failed", e);
        }
        return baos.toByteArray();
    }
}
