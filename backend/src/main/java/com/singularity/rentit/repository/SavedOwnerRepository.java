package com.singularity.rentit.repository;

import com.singularity.rentit.entity.SavedOwner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedOwnerRepository extends JpaRepository<SavedOwner, Long> {

    Page<SavedOwner> findByUserId(Long userId, Pageable pageable);

    boolean existsByUserIdAndOwnerId(Long userId, Long ownerId);

    void deleteByUserIdAndOwnerId(Long userId, Long ownerId);
}
