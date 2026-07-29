package com.beloboki.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name="auth_users")
@EntityListeners(AuditingEntityListener.class)
@Data
public class AuthUser {

        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        private Long id;
        @Column(name = "username", nullable = false, unique = true)
        private String username;
        @Column(name = "password_hash", nullable = false)
        private String passwordHash;
        @Column(name = "role", nullable = false)
        @Enumerated(EnumType.STRING)
        private Role role;
        @Column(name = "created_at", updatable = false)
        @CreatedDate
        private LocalDateTime createdAt;
        @Column(name = "updated_at")
        @LastModifiedDate
        private LocalDateTime updatedAt;
        @Column(name = "user_id", nullable = false)
        private Long userId;
}
