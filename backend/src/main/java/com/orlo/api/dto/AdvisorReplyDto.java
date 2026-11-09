package com.orlo.api.dto;

import java.util.List;

public record AdvisorReplyDto(String reply, List<OutfitDto> outfits, String followUpQuestion, int remainingMessages) {
}
