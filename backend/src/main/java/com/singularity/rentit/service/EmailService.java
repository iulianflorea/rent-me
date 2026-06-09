package com.singularity.rentit.service;

import com.singularity.rentit.entity.Payment;
import com.singularity.rentit.entity.Rental;
import com.singularity.rentit.entity.User;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendGdprAgreement(User user, byte[] pdfBytes) {
        try {
            Context ctx = new Context(localeFor(user));
            ctx.setVariable("user", user);

            String html = templateEngine.process("email/gdpr-signed", ctx);
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());

            helper.setFrom(buildFrom());
            helper.setTo(user.getEmail());
            helper.setSubject("Acord GDPR semnat — RentIt");
            helper.setText(html, true);
            helper.addAttachment("acord_gdpr.pdf", () ->
                    new java.io.ByteArrayInputStream(pdfBytes), "application/pdf");

            mailSender.send(msg);
            log.info("GDPR email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send GDPR email to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    @Async
    public void sendRentalConfirmationToTenant(Rental rental, Payment payment) {
        try {
            User tenant = rental.getTenant();
            Context ctx = new Context(localeFor(tenant));
            ctx.setVariable("rental", rental);
            ctx.setVariable("payment", payment);
            ctx.setVariable("listing", rental.getListing());

            String html = templateEngine.process("email/rental-confirmation-tenant", ctx);
            sendHtml(tenant.getEmail(), "Plată confirmată — " + rental.getListing().getTitle() + " · RentIt", html);
            log.info("Rental confirmation email sent to tenant {}", tenant.getEmail());
        } catch (Exception e) {
            log.error("Failed to send rental confirmation email: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendRentalNotificationToOwner(Rental rental, Payment payment) {
        try {
            User owner = rental.getOwner();
            Context ctx = new Context(localeFor(owner));
            ctx.setVariable("rental", rental);
            ctx.setVariable("payment", payment);
            ctx.setVariable("listing", rental.getListing());

            String html = templateEngine.process("email/rental-notification-owner", ctx);
            sendHtml(owner.getEmail(), "Ai primit o rezervare pentru " + rental.getListing().getTitle() + " · RentIt", html);
            log.info("Rental notification email sent to owner {}", owner.getEmail());
        } catch (Exception e) {
            log.error("Failed to send rental notification email: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendReturnConfirmationToTenant(Rental rental) {
        try {
            User tenant = rental.getTenant();
            Context ctx = new Context(localeFor(tenant));
            ctx.setVariable("rental", rental);

            String html = templateEngine.process("email/return-confirmed", ctx);
            sendHtml(tenant.getEmail(), "Returnare confirmată — garanția ta a fost eliberată · RentIt", html);
            log.info("Return confirmation email sent to tenant {}", tenant.getEmail());
        } catch (Exception e) {
            log.error("Failed to send return confirmation email: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendKycApprovedNotification(User user) {
        try {
            Context ctx = new Context(localeFor(user));
            ctx.setVariable("user", user);

            String html = templateEngine.process("email/kyc-approved", ctx);
            sendHtml(user.getEmail(), "Contul tău a fost verificat · RentIt", html);
            log.info("KYC approved email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send KYC approved email: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendPasswordReset(User user, String resetUrl) {
        try {
            Context ctx = new Context(localeFor(user));
            ctx.setVariable("user", user);
            ctx.setVariable("resetUrl", resetUrl);

            String html = templateEngine.process("email/password-reset", ctx);
            sendHtml(user.getEmail(), "Resetare parolă — RentIt", html);
            log.info("Password reset email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage(), e);
        }
    }

    private void sendHtml(String to, String subject, String html) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
        helper.setFrom(buildFrom());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(msg);
    }

    private InternetAddress buildFrom() throws Exception {
        String displayName = "RentIt";
        if (mailSender instanceof JavaMailSenderImpl impl) {
            Object nameProp = impl.getJavaMailProperties().get("mail.from.name");
            if (nameProp != null) displayName = nameProp.toString();
        }
        return new InternetAddress(((JavaMailSenderImpl) mailSender).getUsername(), displayName, StandardCharsets.UTF_8.name());
    }

    private Locale localeFor(User user) {
        return "en".equals(user.getPreferredLanguage()) ? Locale.ENGLISH : new Locale("ro");
    }
}
