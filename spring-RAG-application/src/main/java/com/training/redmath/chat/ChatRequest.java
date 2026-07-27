package com.training.redmath.chat;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank String question) {
}
