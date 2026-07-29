package com.beloboki.dto;

import com.beloboki.model.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest
        (@Valid AuthRequest authRequest,
         @NotBlank @Size(min = 3) String username,
         @NotBlank @Size(min = 8) String password,
         @NotNull Role role
        ) {
}
