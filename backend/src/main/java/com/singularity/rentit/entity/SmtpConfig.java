package com.singularity.rentit.entity;

import com.singularity.rentit.enums.SmtpSecurity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "smtp_config")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmtpConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String host;

    @Builder.Default
    @Column(nullable = false)
    private Integer port = 587;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SmtpSecurity security = SmtpSecurity.STARTTLS;

    @Column(nullable = false, length = 255)
    private String username;

    @Column(name = "encrypted_password", nullable = false, length = 500)
    private String encryptedPassword;

    @Builder.Default
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName = "RentIt";

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
