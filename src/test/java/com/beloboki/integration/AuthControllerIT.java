package com.beloboki.integration;

import com.beloboki.client.UserClient;
import com.beloboki.dao.AuthDAO;
import com.beloboki.dto.*;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

public class AuthControllerIT extends AbstractIT {

    @Autowired private WebTestClient webTestClient;

    @Autowired private AuthService authService;

    @MockitoBean private UserClient userClient;

    @MockitoBean private AuthDAO authDAO;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserResponse userResponse;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final String USERNAME = "testUser";
    private static final String SURNAME = "last";
    private static final String EMAIL = "example@gmail.com";
    private static final String PASSWORD = "passssssswooooordddd";
    private static final Role ADMIN = Role.ADMIN;

    @BeforeEach
    void setUp() {
        authDAO.deleteAll();
        loginRequest = new LoginRequest(USERNAME, PASSWORD, ADMIN);

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
        Mockito.when(authDAO.existingNaming(USERNAME)).thenReturn(USERNAME);

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

        Assertions.assertEquals(USERNAME, claims.getSubject());
        Assertions.assertNotNull(claims.get("userId", Long.class));
        Assertions.assertEquals(Role.ADMIN.toString(), claims.get("role", String.class));
    }

    @Test
    void valid_shouldReturnValidToken() {
        Mockito.when(authDAO.existingNaming(USERNAME)).thenReturn(USERNAME);
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
        Assertions.assertEquals(USERNAME, tokenResponse.subject());
        Assertions.assertTrue(tokenResponse.valid());
    }

    @Test
    void refresh_shouldReturnAccessAndRefreshTokens() {
        Mockito.when(authDAO.existingNaming(USERNAME)).thenReturn(USERNAME);
        TokenResponse tokenResponseKey = authService.logIn(loginRequest);

        TokenResponse tokenResponse =
                webTestClient
                        .post()
                        .uri("/api/auth/refresh")
                        .bodyValue(tokenResponseKey.refreshToken())
                        .exchange()
                        .expectBody(TokenResponse.class)
                        .returnResult()
                        .getResponseBody();

        Assertions.assertNotNull(tokenResponse);
        Assertions.assertNotNull(tokenResponse.refreshToken());
        Assertions.assertNotNull(tokenResponse.accessToken());
    }
}
