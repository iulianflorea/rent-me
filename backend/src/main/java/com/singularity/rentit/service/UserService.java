package com.singularity.rentit.service;

import com.singularity.rentit.dto.request.UpdateProfileRequest;
import com.singularity.rentit.dto.response.UserProfileResponse;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.enums.ListingStatus;
import com.singularity.rentit.exception.ResourceNotFoundException;
import com.singularity.rentit.repository.ListingRepository;
import com.singularity.rentit.repository.RentalRepository;
import com.singularity.rentit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final ListingRepository listingRepository;
    private final RentalRepository rentalRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        User user = findByEmail(email);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.preferredLanguage() != null) user.setPreferredLanguage(request.preferredLanguage());
        if (request.preferredTheme() != null) user.setPreferredTheme(request.preferredTheme());

        User saved = userRepository.save(user);
        log.info("Profile updated for user: {}", email);
        return toResponse(saved);
    }

    @Transactional
    public void deleteAccount(String email) {
        User user = findByEmail(email);
        user.setActive(false);
        userRepository.save(user);
        log.info("Account deleted (deactivated) for user: {}", email);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public UserProfileResponse toResponse(User user) {
        Double avg = reviewService.getAverageRating(user.getId());
        long reviewCount = reviewService.getReviewCount(user.getId());
        long activeListings = listingRepository.countByOwnerIdAndStatus(user.getId(), ListingStatus.ACTIVE);
        long totalRentals = rentalRepository.countByTenantId(user.getId());
        return new UserProfileResponse(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getPhone(), user.getRole(), user.getKycStatus(),
                user.getPreferredLanguage(), user.getPreferredTheme(),
                user.isGdprSigned(), user.isActive(), avg, reviewCount,
                activeListings, totalRentals, user.getCreatedAt()
        );
    }
}
