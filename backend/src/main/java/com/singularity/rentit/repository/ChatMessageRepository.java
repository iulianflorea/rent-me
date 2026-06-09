package com.singularity.rentit.repository;

import com.singularity.rentit.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId, Pageable pageable);

    long countByRoomIdAndReadFalseAndSenderIdNot(Long roomId, Long senderId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.read = true WHERE m.room.id = :roomId AND m.sender.id != :userId AND m.read = false")
    void markAllAsRead(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
