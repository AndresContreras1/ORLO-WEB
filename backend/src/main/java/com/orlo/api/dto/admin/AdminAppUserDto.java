package com.orlo.api.dto.admin;

import com.orlo.api.entities.AppUser;

import java.time.Instant;

public record AdminAppUserDto(String uid, String email, boolean premium, Instant createdAt, Instant lastSeenAt) {

    public static AdminAppUserDto from(AppUser user) {
        return new AdminAppUserDto(user.getUid(), user.getEmail(), user.isPremium(), user.getCreatedAt(),
                user.getLastSeenAt());
    }
}
