package com.deeptechhub.ollama.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DesignReview(
    @NotBlank
    String scenario,

    @NotNull
    Difficulty difficulty,

    @NotBlank
    String question
) { }
