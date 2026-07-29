package com.beloboki.controller;

import com.beloboki.dto.*;
import com.beloboki.model.Role;
import com.beloboki.service.AuthService;
import com.beloboki.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        Long userId = authService.register(registerRequest);

        String accessToken = jwtService.generateToken(registerRequest.username(), userId, registerRequest.role());
        String refreshToken = jwtService.generateRefreshToken(registerRequest.username(), userId, registerRequest.role());

        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> logIn(@Valid @RequestBody LoginRequest loginRequest) {
        Long userId = authService.logIn(loginRequest);

        String accessToken = jwtService.generateToken(loginRequest.username(), userId, loginRequest.role());
        String refreshToken = jwtService.generateRefreshToken(loginRequest.username(), userId, loginRequest.role());

        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validate(@RequestHeader("Authorization") String header) {
        String token = header.substring(7);
        Claims claim = jwtService.parse(token);
        String username = claim.getSubject();
        return ResponseEntity.ok(new TokenValidationResponse(true, username));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody TokenRefreshRequest tokenRequest) {
        Claims claims = jwtService.parse(tokenRequest.refreshToken());
        String username = claims.getSubject();
        Role role = Role.valueOf(claims.get("role", String.class));
        Long userId = claims.get("userId", Long.class);

        String accessToken = jwtService.generateToken(username, userId, role);
        String refreshToken = jwtService.generateRefreshToken(username, userId, role);

        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
    }
}
