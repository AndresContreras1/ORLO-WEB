package com.orlo.api.dto.admin;

import jakarta.validation.constraints.NotNull;

public record PremiumRequest(@NotNull(message = "Indica si es premium") Boolean premium) {
}
