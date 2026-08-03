package com.beloboki.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenValidationResponse(boolean valid, @NotBlank String subject) {}
