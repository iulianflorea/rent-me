package com.singularity.rentit.service;

import com.singularity.rentit.dto.request.CreateReviewRequest;
import com.singularity.rentit.dto.response.ReviewResponse;
import com.singularity.rentit.entity.Rental;
import com.singularity.rentit.entity.Review;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.enums.RentalStatus;
import com.singularity.rentit.exception.BusinessException;
import com.singularity.rentit.exception.ResourceNotFoundException;
import com.singularity.rentit.repository.RentalRepository;
import com.singularity.rentit.repository.ReviewRepository;
import com.singularity.rentit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, Long reviewerId) {
        Rental rental = rentalRepository.findById(request.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException("Rental", request.rentalId()));

        if (rental.getStatus() != RentalStatus.RETURNED) {
            throw new BusinessException("Can only review completed rentals", HttpStatus.BAD_REQUEST, "review.rental_not_completed");
        }

        if (!rental.getTenant().getId().equals(reviewerId) && !rental.getOwner().getId().equals(reviewerId)) {
            throw new BusinessException("Not authorized to review this rental", HttpStatus.FORBIDDEN, "review.not_participant");
        }

        if (reviewRepository.existsByRentalIdAndReviewerId(request.rentalId(), reviewerId)) {
            throw new BusinessException("Already reviewed this rental", HttpStatus.CONFLICT, "review.already_exists");
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", reviewerId));

        User reviewed = rental.getTenant().getId().equals(reviewerId)
                ? rental.getOwner()
                : rental.getTenant();

        Review review = Review.builder()
                .rental(rental)
                .reviewer(reviewer)
                .reviewed(reviewed)
                .rating((byte) (int) request.rating())
                .comment(request.comment())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review created by user {} for rental {}", reviewerId, request.rentalId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsForUser(Long userId, Pageable pageable) {
        return reviewRepository.findByReviewedIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(Long userId) {
        return reviewRepository.findAverageRatingByUserId(userId).orElse(null);
    }

    @Transactional(readOnly = true)
    public long getReviewCount(Long userId) {
        return reviewRepository.countByReviewedId(userId);
    }

    private ReviewResponse toResponse(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getRental().getId(),
                new ReviewResponse.ReviewerSummary(
                        r.getReviewer().getId(),
                        r.getReviewer().getFirstName(),
                        r.getReviewer().getLastName(),
                        r.getReviewer().getKycStatus().name().equals("VERIFIED")
                ),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}
