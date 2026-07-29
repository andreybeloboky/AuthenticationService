package com.beloboki.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record UserRequest(
        @NotBlank(message = "Name shouldn't be empty") String name,
        @NotBlank(message = "Surname shouldn't be empty") String surname,
        @NotNull(message = "Birth date shouldn't be empty") @Past LocalDate birthDate,
        @NotBlank(message = "Email shouldn't be empty") @Email String email,
        @NotNull(message = "Status shouldn't be empty") Boolean active) {}
