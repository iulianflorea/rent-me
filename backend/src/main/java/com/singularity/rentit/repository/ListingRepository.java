package com.singularity.rentit.repository;

import com.singularity.rentit.entity.Listing;
import com.singularity.rentit.enums.CategoryType;
import com.singularity.rentit.enums.ListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    Page<Listing> findByOwnerIdOrderByCreatedAtDesc(Long ownerId, Pageable pageable);

    @Query("""
        SELECT l FROM Listing l
        WHERE l.status = 'ACTIVE'
        AND (:category IS NULL OR l.category = :category)
        AND (:minPrice IS NULL OR l.pricePerDay >= :minPrice)
        AND (:maxPrice IS NULL OR l.pricePerDay <= :maxPrice)
        AND (:city IS NULL OR LOWER(l.city) LIKE LOWER(CONCAT('%', :city, '%')))
        AND (:county IS NULL OR LOWER(l.county) LIKE LOWER(CONCAT('%', :county, '%')))
        AND (:search IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(l.description) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(l.city) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(l.county) LIKE LOWER(CONCAT('%', :search, '%')))
        AND l.id NOT IN (
            SELECT r.listing.id FROM Rental r
            WHERE r.status NOT IN ('CANCELLED', 'RETURNED')
            AND r.startDate < :endDate
            AND r.endDate > :startDate
        )
        """)
    Page<Listing> searchListings(
            @Param("category") CategoryType category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("city") String city,
            @Param("county") String county,
            @Param("search") String search,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("""
        SELECT l FROM Listing l
        WHERE l.status = 'ACTIVE'
        AND l.latitude IS NOT NULL AND l.longitude IS NOT NULL
        AND (6371 * acos(
            cos(radians(:lat)) * cos(radians(l.latitude)) *
            cos(radians(l.longitude) - radians(:lon)) +
            sin(radians(:lat)) * sin(radians(l.latitude))
        )) <= :radiusKm
        """)
    Page<Listing> findNearby(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusKm") double radiusKm,
            Pageable pageable
    );

    long countByOwnerId(Long ownerId);

    long countByOwnerIdAndStatus(Long ownerId, ListingStatus status);
}
