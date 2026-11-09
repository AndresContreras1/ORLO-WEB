package com.orlo.api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Usuario de la app. La identidad vive en Firebase Authentication;
 * aquí solo se guarda lo que decide el backend (premium) y lo que necesita el panel.
 */
@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @Column(length = 128)
    private String uid;

    @Column(length = 200)
    private String email;

    @Column(nullable = false)
    private boolean premium;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    public AppUser(String uid, String email) {
        this.uid = uid;
        this.email = email;
        this.createdAt = Instant.now();
        this.lastSeenAt = this.createdAt;
    }
}
