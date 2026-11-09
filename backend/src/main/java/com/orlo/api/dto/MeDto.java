package com.orlo.api.dto;

public record MeDto(String uid, boolean premium, int dailyLimit, int remainingMessages) {
}
