package com.beloboki.service;

import com.beloboki.client.UserClient;
import com.beloboki.dao.AuthDAO;
import com.beloboki.dto.*;
import com.beloboki.exception.InvalidTokenException;
import com.beloboki.exception.InvalidUsernameOrPasswordException;
import com.beloboki.exception.UsernameAlreadyExists;
import com.beloboki.exception.UsernameNotFoundException;
import com.beloboki.mapper.AuthMapper;
import com.beloboki.mapper.UserMapper;
import com.beloboki.model.AuthUser;
import com.beloboki.model.Role;
import com.beloboki.model.User;
import io.jsonwebtoken.Claims;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserClient userClient;
    private final JwtService jwtService;
    private final AuthDAO authDAO;
    private final AuthMapper authMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private static final String BEARER_PREFIX = "Bearer ";

    public TokenResponse register(RegisterRequest registerRequest) {
        AuthUser authUser = authMapper.toEntity(registerRequest);
        User user = userMapper.toUser(registerRequest.userRequest());

        if (authDAO.existingNaming(authUser.getUsername()) != null) {
            throw new UsernameAlreadyExists(
                    "Such username = %s was already created".formatted(authUser.getUsername()));
        }

        UserResponse userResponse = userClient.save(user);

        authUser.setPasswordHash(passwordEncoder.encode(registerRequest.password()));
        authUser.setUserId(userResponse.id());

        authDAO.saveAndFlush(authUser);

        String accessToken =
                jwtService.generateToken(
                        authUser.getUsername(), userResponse.id(), authUser.getRole());
        String refreshToken =
                jwtService.generateRefreshToken(
                        authUser.getUsername(), userResponse.id(), authUser.getRole());
        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse logIn(LoginRequest loginRequest) {
        AuthUser authUserEnter = authMapper.toLogin(loginRequest);

        AuthUser user =
                findUserByUsername(authUserEnter.getUsername())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
            throw new InvalidUsernameOrPasswordException("Invalid password or username");
        }

        String accessToken =
                jwtService.generateToken(user.getUsername(), user.getUserId(), user.getRole());
        String refreshToken =
                jwtService.generateRefreshToken(
                        user.getUsername(), user.getUserId(), user.getRole());

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenValidationResponse validate(String header) {
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new InvalidTokenException("Invalid token");
        }
        String token = header.substring(7);
        Claims claim = jwtService.parse(token);
        String username = claim.getSubject();
        return new TokenValidationResponse(true, username);
    }

    public TokenResponse refreshToken(TokenRefreshRequest tokenRequest) {
        Claims claims = jwtService.parse(tokenRequest.refreshToken());
        String username = claims.getSubject();
        Role role = Role.valueOf(claims.get("role", String.class));
        Long userId = claims.get("userId", Long.class);

        String accessToken = jwtService.generateToken(username, userId, role);
        String refreshToken = jwtService.generateRefreshToken(username, userId, role);
        return new TokenResponse(accessToken, refreshToken);
    }

    private Optional<AuthUser> findUserByUsername(String username) {
        return authDAO.findUserByUsername(username);
    }
}
