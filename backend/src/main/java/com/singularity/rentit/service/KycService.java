package com.singularity.rentit.service;

import com.singularity.rentit.dto.request.KycReviewRequest;
import com.singularity.rentit.dto.request.KycSubmitRequest;
import com.singularity.rentit.dto.response.KycStatusResponse;
import com.singularity.rentit.entity.KycVerification;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.enums.KycStatus;
import com.singularity.rentit.exception.BusinessException;
import com.singularity.rentit.exception.ResourceNotFoundException;
import com.singularity.rentit.repository.KycRepository;
import com.singularity.rentit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycService {

    private final KycRepository kycRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final EmailService emailService;

    @Transactional
    public KycStatusResponse uploadSelfie(User user, MultipartFile file) {
        KycVerification kyc = getOrCreateKyc(user);
        String url = storageService.uploadFile(file, "kyc/" + user.getId() + "/selfie");
        kyc.setSelfieUrl(url);
        kyc.setUpdatedAt(LocalDateTime.now());

        if (kyc.getStatus() != KycStatus.PENDING && kyc.getStatus() != KycStatus.REJECTED) {
            kyc.setStatus(KycStatus.PENDING);
        }

        kycRepository.save(kyc);
        log.info("KYC selfie uploaded for user {}", user.getId());
        return toResponse(kyc);
    }

    @Transactional
    public KycStatusResponse uploadIdFront(User user, MultipartFile file) {
        KycVerification kyc = getOrCreateKyc(user);
        String url = storageService.uploadFile(file, "kyc/" + user.getId() + "/id");
        kyc.setIdFrontUrl(url);
        kyc.setUpdatedAt(LocalDateTime.now());
        kycRepository.save(kyc);
        return toResponse(kyc);
    }

    @Transactional
    public KycStatusResponse uploadIdBack(User user, MultipartFile file) {
        KycVerification kyc = getOrCreateKyc(user);
        String url = storageService.uploadFile(file, "kyc/" + user.getId() + "/id");
        kyc.setIdBackUrl(url);
        kyc.setUpdatedAt(LocalDateTime.now());
        kycRepository.save(kyc);
        return toResponse(kyc);
    }

    @Transactional
    public KycStatusResponse submitData(User user, KycSubmitRequest request) {
        KycVerification kyc = getOrCreateKyc(user);

        kyc.setIdSeries(request.idSeries());
        kyc.setIdNumber(request.idNumber());
        kyc.setCnp(request.cnp());
        kyc.setBirthDate(request.birthDate());
        kyc.setIdExpiryDate(request.idExpiryDate());
        kyc.setStatus(KycStatus.PENDING);
        kyc.setUpdatedAt(LocalDateTime.now());

        user.setKycStatus(KycStatus.PENDING);
        userRepository.save(user);

        kycRepository.save(kyc);
        log.info("KYC data submitted for user {}", user.getId());
        return toResponse(kyc);
    }

    @Transactional(readOnly = true)
    public KycStatusResponse getStatus(Long userId) {
        KycVerification kyc = kycRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC verification for user " + userId));
        return toResponse(kyc);
    }

    @Transactional
    public KycStatusResponse reviewKyc(Long userId, KycReviewRequest request, Long adminId) {
        KycVerification kyc = kycRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC verification for user " + userId));

        User user = kyc.getUser();

        if (request.status() == KycStatus.VERIFIED || request.status() == KycStatus.REJECTED) {
            kyc.setStatus(request.status());
            kyc.setReviewedBy(adminId);
            kyc.setReviewedAt(LocalDateTime.now());
            kyc.setRejectionReason(request.rejectionReason());
            kyc.setUpdatedAt(LocalDateTime.now());

            user.setKycStatus(request.status());
            userRepository.save(user);

            if (request.status() == KycStatus.VERIFIED) {
                emailService.sendKycApprovedNotification(user);
            }

            kycRepository.save(kyc);
            log.info("KYC {} for user {} by admin {}", request.status(), userId, adminId);
        } else {
            throw new BusinessException("Invalid KYC review status", HttpStatus.BAD_REQUEST);
        }

        return toResponse(kyc);
    }

    private KycVerification getOrCreateKyc(User user) {
        return kycRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    KycVerification kyc = KycVerification.builder()
                            .user(user)
                            .status(KycStatus.PENDING)
                            .build();
                    return kycRepository.save(kyc);
                });
    }

    private KycStatusResponse toResponse(KycVerification kyc) {
        return new KycStatusResponse(
                kyc.getId(),
                kyc.getStatus(),
                kyc.getRejectionReason(),
                kyc.getSubmittedAt(),
                kyc.getReviewedAt(),
                kyc.getSelfieUrl() != null,
                kyc.getIdFrontUrl() != null,
                kyc.getIdBackUrl() != null,
                kyc.getIdNumber() != null
        );
    }
}
