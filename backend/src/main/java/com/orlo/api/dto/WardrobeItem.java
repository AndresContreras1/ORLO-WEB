package com.orlo.api.dto;

import jakarta.validation.constraints.Size;

public record WardrobeItem(@Size(max = 80) String name, @Size(max = 40) String category, @Size(max = 40) String color) {
}
