package com.beloboki.controller;

import com.beloboki.dto.LoginRequest;
import com.beloboki.dto.RegisterRequest;
import com.beloboki.dto.TokenResponse;
import com.beloboki.service.AuthService;
import com.beloboki.service.JwtService;
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
        authService.register(registerRequest);

        Long user_id = authService.findUserIdByUsernameAndPassword(registerRequest);

        String accessToken = jwtService.generateToken(registerRequest.username(), user_id, registerRequest.role());
        String refreshToken = jwtService.generateRefreshToken(registerRequest.username(), user_id, registerRequest.role());

        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
    }

    @PostMapping("/login")
    public ResponseEntity<Void> logIn(@Valid @RequestBody LoginRequest loginRequest) {
        authService.logIn(loginRequest);

      //  Long user_id = authService.findUserIdByUsernameAndPassword(loginRequest.username(),loginRequest.password());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validate() {
        return null;
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh() {
        return null;
    }
}
