package com.deeptechhub.ollama.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DesignReviewRequest(
    @NotBlank
    String topic,

    @NotNull
    Difficulty difficulty,

    @NotBlank
    String focus
) { }
