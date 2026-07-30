package com.beloboki.service;

import com.beloboki.client.UserClient;
import com.beloboki.dao.AuthDAO;
import com.beloboki.dto.*;
import com.beloboki.exception.UsernameAlreadyExists;
import com.beloboki.exception.UsernameNotFoundException;
import com.beloboki.mapper.AuthMapper;
import com.beloboki.mapper.UserMapper;
import com.beloboki.model.AuthUser;
import com.beloboki.model.Role;
import com.beloboki.model.User;
import io.jsonwebtoken.Claims;
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

        Long userId =
                findUserIdByUsernameAndPassword(authUser.getUsername(), authUser.getPasswordHash());
        String accessToken =
                jwtService.generateToken(authUser.getUsername(), userId, authUser.getRole());
        String refreshToken =
                jwtService.generateRefreshToken(authUser.getUsername(), userId, authUser.getRole());

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse logIn(LoginRequest loginRequest) {
        AuthUser authUser = authMapper.toLogin(loginRequest);
        authUser.setPasswordHash(passwordEncoder.encode(loginRequest.password()));

        if (authDAO.existingNaming(authUser.getUsername()) == null) {
            throw new UsernameNotFoundException(
                    "Such username = %s isn't created yet".formatted(authUser.getUsername()));
        }

        Long userId =
                findUserIdByUsernameAndPassword(authUser.getUsername(), authUser.getPasswordHash());
        String accessToken =
                jwtService.generateToken(authUser.getUsername(), userId, authUser.getRole());
        String refreshToken =
                jwtService.generateRefreshToken(authUser.getUsername(), userId, authUser.getRole());

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenValidationResponse validate(String header) {
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

    private Long findUserIdByUsernameAndPassword(String username, String hashPassword) {
        return authDAO.findUserIdByUsernameAndPassword(
                username, passwordEncoder.encode(hashPassword));
    }
}
