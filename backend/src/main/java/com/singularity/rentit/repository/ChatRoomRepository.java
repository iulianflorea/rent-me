package com.singularity.rentit.repository;

import com.singularity.rentit.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByRentalId(Long rentalId);

    @Query("""
        SELECT cr FROM ChatRoom cr
        WHERE cr.participant1.id = :userId OR cr.participant2.id = :userId
        ORDER BY cr.lastMessageAt DESC NULLS LAST
        """)
    Page<ChatRoom> findByParticipant(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT cr FROM ChatRoom cr
        WHERE (cr.participant1.id = :userId1 AND cr.participant2.id = :userId2)
           OR (cr.participant1.id = :userId2 AND cr.participant2.id = :userId1)
        """)
    Optional<ChatRoom> findByParticipants(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
