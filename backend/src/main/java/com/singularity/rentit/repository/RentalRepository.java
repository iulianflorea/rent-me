package com.singularity.rentit.repository;

import com.singularity.rentit.entity.Rental;
import com.singularity.rentit.enums.RentalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    Page<Rental> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    Page<Rental> findByOwnerIdOrderByCreatedAtDesc(Long ownerId, Pageable pageable);

    Optional<Rental> findByQrCodeToken(String qrCodeToken);

    Optional<Rental> findByReferenceNumber(String referenceNumber);

    boolean existsByListingIdAndStatusNotInAndStartDateLessThanAndEndDateGreaterThan(
            Long listingId,
            List<RentalStatus> excludedStatuses,
            LocalDate endDate,
            LocalDate startDate
    );

    @Query("""
        SELECT SUM(r.subtotal)
        FROM Rental r
        JOIN Payment p ON p.rental.id = r.id
        WHERE r.owner.id = :ownerId
        AND r.status IN ('RETURNED', 'ACTIVE', 'READY_TO_PICKUP')
        AND r.createdAt BETWEEN :from AND :to
        """)
    Optional<BigDecimal> sumEarningsByOwnerAndPeriod(
            @Param("ownerId") Long ownerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT SUM(r.totalAmount)
        FROM Rental r
        WHERE r.tenant.id = :tenantId
        AND r.status NOT IN ('CANCELLED', 'PENDING_PAYMENT')
        AND r.createdAt BETWEEN :from AND :to
        """)
    Optional<BigDecimal> sumSpendingByTenantAndPeriod(
            @Param("tenantId") Long tenantId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT SUM(p.platformFee)
        FROM Payment p
        WHERE p.status IN ('HELD', 'RELEASED')
        AND p.paidAt BETWEEN :from AND :to
        """)
    Optional<BigDecimal> sumPlatformFeesByPeriod(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT SUM(p.stripeFee)
        FROM Payment p
        WHERE p.status IN ('HELD', 'RELEASED')
        AND p.paidAt BETWEEN :from AND :to
        """)
    Optional<BigDecimal> sumStripeFeesByPeriod(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    long countByTenantId(Long tenantId);

    long countByOwnerId(Long ownerId);
}
