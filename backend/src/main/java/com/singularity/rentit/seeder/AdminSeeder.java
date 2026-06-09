package com.singularity.rentit.seeder;

import com.singularity.rentit.entity.User;
import com.singularity.rentit.enums.KycStatus;
import com.singularity.rentit.enums.UserRole;
import com.singularity.rentit.repository.UserRepository;
import com.singularity.rentit.service.SmtpConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmtpConfigService smtpConfigService;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdmin();
        smtpConfigService.reinitializeFromDb();
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin account already exists: {}", adminEmail);
            return;
        }

        User admin = User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .firstName("Admin")
                .lastName("RentIt")
                .role(UserRole.ADMIN)
                .kycStatus(KycStatus.VERIFIED)
                .preferredLanguage("ro")
                .active(true)
                .gdprSigned(true)
                .build();

        userRepository.save(admin);
        log.info("Admin account created: {}", adminEmail);
    }
}
