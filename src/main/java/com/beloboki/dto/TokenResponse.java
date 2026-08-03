package com.beloboki.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenResponse(@NotBlank String accessToken, @NotBlank String refreshToken) {}
