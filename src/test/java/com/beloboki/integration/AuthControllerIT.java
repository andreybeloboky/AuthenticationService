package com.beloboki.integration;

import com.beloboki.client.UserClient;
import com.beloboki.dao.AuthDAO;
import com.beloboki.dto.*;
import com.beloboki.model.AuthUser;
import com.beloboki.model.Role;
import com.beloboki.model.User;
import com.beloboki.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

public class AuthControllerIT extends AbstractIT {

    @Autowired private WebTestClient webTestClient;

    @Autowired private AuthService authService;

    @Autowired private AuthDAO authDAO;

    @Autowired private PasswordEncoder passwordEncoder;

    @MockitoBean private UserClient userClient;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserResponse userResponse;
    private LoginRequest loginRequestWrong;
    private LoginRequest loginRequestWrongPassword;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final String USERNAME = "testUser";
    private static final String USERNAME_FIRST = "FIRST_EVER";
    private static final String SURNAME = "last";
    private static final String EMAIL = "example@gmail.com";
    private static final String PASSWORD = "passssssswooooordddd";
    private static final Role ADMIN = Role.ADMIN;
    private static final String WRONG = "WRONG_NAME_AND_PASSWORD";

    @BeforeEach
    void setUp() {
        authDAO.deleteAll();
        loginRequest = new LoginRequest(USERNAME_FIRST, PASSWORD);
        loginRequestWrong = new LoginRequest(WRONG, WRONG);
        loginRequestWrongPassword = new LoginRequest(USERNAME_FIRST, WRONG);

        UserRequest userRequest =
                new UserRequest(USERNAME, SURNAME, LocalDate.of(2000, Month.JULY, 1), EMAIL, true);

        registerRequest = new RegisterRequest(userRequest, USERNAME, PASSWORD, ADMIN);

        userResponse =
                new UserResponse(
                        1L,
                        USERNAME,
                        SURNAME,
                        LocalDate.of(2000, Month.JULY, 1),
                        EMAIL,
                        true,
                        LocalDateTime.now(),
                        LocalDateTime.now());

        AuthUser user = new AuthUser();
        user.setUsername(USERNAME_FIRST);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(ADMIN);
        user.setUserId(1L);

        authDAO.saveAndFlush(user);
    }

    @Test
    void register_shouldReturnValidToken() {
        Mockito.when(userClient.save(Mockito.any(User.class))).thenReturn(userResponse);

        TokenResponse tokenResponse =
                webTestClient
                        .post()
                        .uri("/api/auth/register")
                        .bodyValue(registerRequest)
                        .exchange()
                        .expectBody(TokenResponse.class)
                        .returnResult()
                        .getResponseBody();

        Assertions.assertNotNull(tokenResponse);

        String access = tokenResponse.accessToken();
        Assertions.assertNotNull(access);

        Claims claims =
                Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                        .build()
                        .parseClaimsJws(access)
                        .getBody();

        Assertions.assertEquals(USERNAME, claims.getSubject());
        Assertions.assertNotNull(claims.get("userId", Long.class));
        Assertions.assertEquals(Role.ADMIN.toString(), claims.get("role", String.class));
    }

    @Test
    void logIn_shouldReturnValidToken() {
        TokenResponse tokenResponse =
                webTestClient
                        .post()
                        .uri("/api/auth/login")
                        .bodyValue(loginRequest)
                        .exchange()
                        .expectBody(TokenResponse.class)
                        .returnResult()
                        .getResponseBody();

        Assertions.assertNotNull(tokenResponse);

        String access = tokenResponse.accessToken();
        Assertions.assertNotNull(access);

        Claims claims =
                Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                        .build()
                        .parseClaimsJws(access)
                        .getBody();

        Assertions.assertEquals(USERNAME_FIRST, claims.getSubject());
        Assertions.assertNotNull(claims.get("userId", Long.class));
        Assertions.assertEquals(Role.ADMIN.toString(), claims.get("role", String.class));
    }

    @Test
    void valid_shouldReturnValidToken() {
        TokenResponse tokenResponseKey = authService.logIn(loginRequest);

        TokenValidationResponse tokenResponse =
                webTestClient
                        .post()
                        .uri("/api/auth/validate")
                        .header("Authorization", "Bearer " + tokenResponseKey.accessToken())
                        .exchange()
                        .expectBody(TokenValidationResponse.class)
                        .returnResult()
                        .getResponseBody();

        Assertions.assertNotNull(tokenResponse);
        Assertions.assertEquals(USERNAME_FIRST, tokenResponse.subject());
        Assertions.assertTrue(tokenResponse.valid());
    }

    @Test
    void refresh_shouldReturnAccessAndRefreshTokens() {
        TokenResponse tokenResponseKey = authService.logIn(loginRequest);

        TokenRefreshRequest tokenRefreshRequest =
                new TokenRefreshRequest(tokenResponseKey.refreshToken());

        TokenResponse tokenResponse =
                webTestClient
                        .post()
                        .uri("/api/auth/refresh")
                        .bodyValue(tokenRefreshRequest)
                        .exchange()
                        .expectBody(TokenResponse.class)
                        .returnResult()
                        .getResponseBody();

        Assertions.assertNotNull(tokenResponse);
        Assertions.assertNotNull(tokenResponse.accessToken());
        Assertions.assertNotNull(tokenResponse.refreshToken());
    }

    @Test
    void valid_shouldReturnException() {
        webTestClient
                .post()
                .uri("/api/auth/validate")
                .header("Authorization", "Bearer wrong")
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo("Token is invalid");
    }

    @Test
    void sendNotExistingUser_shouldReturnException() {
        webTestClient
                .post()
                .uri("/api/auth/login")
                .bodyValue(loginRequestWrong)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo("User not found");
    }

    @Test
    void sendWrongPasswordExistingUser_shouldReturnException() {
        webTestClient
                .post()
                .uri("/api/auth/login")
                .bodyValue(loginRequestWrongPassword)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo("Invalid password");
    }
}
