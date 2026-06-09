package com.singularity.rentit.service;

import com.singularity.rentit.dto.request.*;
import com.singularity.rentit.dto.response.AuthResponse;
import com.singularity.rentit.dto.response.UserProfileResponse;
import com.singularity.rentit.entity.PasswordResetToken;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.enums.KycStatus;
import com.singularity.rentit.enums.UserRole;
import com.singularity.rentit.exception.BusinessException;
import com.singularity.rentit.exception.ResourceNotFoundException;
import com.singularity.rentit.repository.PasswordResetTokenRepository;
import com.singularity.rentit.repository.UserRepository;
import com.singularity.rentit.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final UserService userService;

    @Value("${app.jwt.expiration}")
    private long accessTokenExpiry;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already in use", HttpStatus.CONFLICT, "auth.email_taken");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .role(UserRole.USER)
                .kycStatus(KycStatus.NONE)
                .preferredLanguage("ro")
                .active(true)
                .gdprSigned(false)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getEmail());

        return buildAuthResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new BusinessException("Account is disabled", HttpStatus.FORBIDDEN, "auth.account_disabled");
        }

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.refreshToken();
        if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isRefreshToken(token)) {
            throw new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED, "auth.invalid_token");
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.UNAUTHORIZED, "auth.user_not_found"));

        return buildAuthResponse(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserId(user.getId());

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();

            passwordResetTokenRepository.save(resetToken);
            String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;
            emailService.sendPasswordReset(user, resetUrl);
            log.info("Password reset requested for: {}", user.getEmail());
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(request.token())
                .orElseThrow(() -> new BusinessException("Invalid or expired reset token", HttpStatus.BAD_REQUEST, "auth.invalid_reset_token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Reset token has expired", HttpStatus.BAD_REQUEST, "auth.token_expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset for user: {}", user.getEmail());
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        UserProfileResponse profile = userService.toResponse(user);
        return AuthResponse.of(accessToken, refreshToken, accessTokenExpiry, profile);
    }
}
