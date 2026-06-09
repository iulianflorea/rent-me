package com.singularity.rentit.repository;

import com.singularity.rentit.entity.User;
import com.singularity.rentit.enums.KycStatus;
import com.singularity.rentit.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
        SELECT u FROM User u
        WHERE (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
        AND (:role IS NULL OR u.role = :role)
        AND (:kycStatus IS NULL OR u.kycStatus = :kycStatus)
        AND (:active IS NULL OR u.active = :active)
        ORDER BY u.createdAt DESC
        """)
    Page<User> findAllWithFilters(
            @Param("email") String email,
            @Param("role") UserRole role,
            @Param("kycStatus") KycStatus kycStatus,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
