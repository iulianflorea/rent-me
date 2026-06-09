package com.singularity.rentit.repository;

import com.singularity.rentit.entity.SmtpConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmtpConfigRepository extends JpaRepository<SmtpConfig, Long> {

    Optional<SmtpConfig> findFirstByActiveTrueOrderByUpdatedAtDesc();
}
