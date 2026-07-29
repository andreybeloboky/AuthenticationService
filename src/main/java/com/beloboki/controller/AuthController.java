package com.beloboki.controller;

import com.beloboki.dto.LoginRequest;
import com.beloboki.dto.RegisterRequest;
import com.beloboki.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> logIn(@Valid @RequestBody LoginRequest loginRequest) {
        authService.logIn(loginRequest);
        return ResponseEntity.ok().build();
    }
}
