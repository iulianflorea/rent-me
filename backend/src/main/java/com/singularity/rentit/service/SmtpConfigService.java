package com.singularity.rentit.service;

import com.singularity.rentit.dto.request.SmtpConfigRequest;
import com.singularity.rentit.dto.response.SmtpConfigResponse;
import com.singularity.rentit.entity.SmtpConfig;
import com.singularity.rentit.enums.SmtpSecurity;
import com.singularity.rentit.exception.ResourceNotFoundException;
import com.singularity.rentit.repository.SmtpConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpConfigService {

    private final SmtpConfigRepository smtpConfigRepository;
    private final JavaMailSender mailSender;

    @Value("${app.aes.secret}")
    private String aesSecret;

    @Transactional
    public SmtpConfigResponse saveConfig(SmtpConfigRequest request, String adminEmail) {
        SmtpConfig config = smtpConfigRepository.findFirstByActiveTrueOrderByUpdatedAtDesc()
                .orElseGet(SmtpConfig::new);

        config.setHost(request.host());
        config.setPort(request.port());
        config.setSecurity(request.security());
        config.setUsername(request.username());
        config.setDisplayName(request.displayName());
        config.setActive(true);
        config.setUpdatedBy(adminEmail);
        config.setUpdatedAt(LocalDateTime.now());

        if (request.password() != null && !request.password().isBlank()) {
            config.setEncryptedPassword(encrypt(request.password()));
        }

        SmtpConfig saved = smtpConfigRepository.save(config);
        reinitializeMailSender(saved);

        log.info("SMTP config updated by {}", adminEmail);
        return toResponse(saved);
    }

    public SmtpConfigResponse getCurrentConfig() {
        return smtpConfigRepository.findFirstByActiveTrueOrderByUpdatedAtDesc()
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("SMTP configuration not found"));
    }

    public boolean hasConfig() {
        return smtpConfigRepository.findFirstByActiveTrueOrderByUpdatedAtDesc().isPresent();
    }

    public String getSmtpStatus() {
        Optional<SmtpConfig> config = smtpConfigRepository.findFirstByActiveTrueOrderByUpdatedAtDesc();
        if (config.isEmpty()) return "UNCONFIGURED";
        try {
            JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;
            impl.testConnection();
            return "OK";
        } catch (Exception e) {
            log.warn("SMTP connection test failed: {}", e.getMessage());
            return "ERROR";
        }
    }

    public void reinitializeMailSender(SmtpConfig config) {
        if (!(mailSender instanceof JavaMailSenderImpl impl)) return;

        impl.setHost(config.getHost());
        impl.setPort(config.getPort());
        impl.setUsername(config.getUsername());
        impl.setPassword(decrypt(config.getEncryptedPassword()));

        Properties props = impl.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.from.name", config.getDisplayName());

        if (config.getSecurity() == SmtpSecurity.STARTTLS) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.enable", "false");
        } else if (config.getSecurity() == SmtpSecurity.SSL) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.starttls.enable", "false");
        } else {
            props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.smtp.ssl.enable", "false");
        }

        impl.setJavaMailProperties(props);
        log.info("JavaMailSender reinitialized with new SMTP config");
    }

    public void reinitializeFromDb() {
        smtpConfigRepository.findFirstByActiveTrueOrderByUpdatedAtDesc()
                .ifPresent(this::reinitializeMailSender);
    }

    String encrypt(String plainText) {
        try {
            SecretKeySpec key = buildKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    String decrypt(String encryptedText) {
        try {
            SecretKeySpec key = buildKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private SecretKeySpec buildKey() {
        byte[] keyBytes = new byte[32];
        byte[] secretBytes = aesSecret.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(secretBytes, 0, keyBytes, 0, Math.min(secretBytes.length, 32));
        return new SecretKeySpec(keyBytes, "AES");
    }

    private SmtpConfigResponse toResponse(SmtpConfig config) {
        return new SmtpConfigResponse(
                config.getId(),
                config.getHost(),
                config.getPort(),
                config.getSecurity(),
                config.getUsername(),
                config.getDisplayName(),
                config.isActive(),
                config.getUpdatedAt(),
                config.getUpdatedBy()
        );
    }
}
