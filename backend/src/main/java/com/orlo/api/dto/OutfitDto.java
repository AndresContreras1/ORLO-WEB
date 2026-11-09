package com.orlo.api.dto;

import java.util.List;

public record OutfitDto(String title, List<String> items, String why) {
}
