package com.orlo.api.dto;

import java.util.List;

public record DailyOutfitDto(String title, List<String> items, String why) {
}
