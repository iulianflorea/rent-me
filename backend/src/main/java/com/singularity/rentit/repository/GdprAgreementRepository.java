package com.singularity.rentit.repository;

import com.singularity.rentit.entity.GdprAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GdprAgreementRepository extends JpaRepository<GdprAgreement, Long> {

    Optional<GdprAgreement> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserId(Long userId);
}
