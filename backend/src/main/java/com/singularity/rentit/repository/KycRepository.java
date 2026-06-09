package com.singularity.rentit.repository;

import com.singularity.rentit.entity.KycVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycRepository extends JpaRepository<KycVerification, Long> {

    Optional<KycVerification> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
