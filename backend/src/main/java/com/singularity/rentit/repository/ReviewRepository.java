package com.singularity.rentit.repository;

import com.singularity.rentit.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByReviewedIdOrderByCreatedAtDesc(Long reviewedId, Pageable pageable);

    boolean existsByRentalIdAndReviewerId(Long rentalId, Long reviewerId);

    Optional<Review> findByRentalIdAndReviewerId(Long rentalId, Long reviewerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewed.id = :userId")
    Optional<Double> findAverageRatingByUserId(@Param("userId") Long userId);

    long countByReviewedId(Long reviewedId);
}
