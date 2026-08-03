package com.beloboki.service;

import static org.mockito.Mockito.*;

import com.beloboki.client.UserClient;
import com.beloboki.dao.AuthDAO;
import com.beloboki.dto.*;
import com.beloboki.exception.InvalidUsernameOrPasswordException;
import com.beloboki.exception.UsernameAlreadyExists;
import com.beloboki.mapper.AuthMapper;
import com.beloboki.mapper.UserMapper;
import com.beloboki.model.AuthUser;
import com.beloboki.model.Role;
import com.beloboki.model.User;
import io.jsonwebtoken.Claims;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {

    @Mock private UserClient userClient;
    @Mock private JwtService jwtService;
    @Mock private AuthDAO authDAO;
    @Mock private AuthMapper authMapper;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AuthService authService;

    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "passssssswooooordddd";
    private static final Long USER_ID = 1L;
    private static final Role USER = Role.USER;
    private static final Role ADMIN = Role.ADMIN;

    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userRequest =
                new UserRequest(
                        "first",
                        "last",
                        LocalDate.of(2000, Month.JULY, 1),
                        "example@gmail.com",
                        true);
        userResponse =
                new UserResponse(
                        USER_ID,
                        "first",
                        "last",
                        LocalDate.of(2000, Month.JULY, 1),
                        "example@gmail.com",
                        true,
                        LocalDateTime.now(),
                        LocalDateTime.now());
    }

    @Test
    void register_shouldReturnTokensAndSaveAuthUser() {
        RegisterRequest registerRequest =
                new RegisterRequest(userRequest, USERNAME, PASSWORD, ADMIN);

        AuthUser authUser = new AuthUser();
        authUser.setUsername(USERNAME);
        authUser.setRole(USER);

        User user = new User();
        user.setName(USERNAME);

        when(authMapper.toEntity(registerRequest)).thenReturn(authUser);
        when(userMapper.toUser(registerRequest.userRequest())).thenReturn(user);
        when(authDAO.existingNaming(USERNAME)).thenReturn(null);
        when(userClient.save(user)).thenReturn(userResponse);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPass");

        when(jwtService.generateToken(USERNAME, USER_ID, USER)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(USERNAME, USER_ID, USER)).thenReturn("refresh-token");

        TokenResponse response = authService.register(registerRequest);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("access-token", response.accessToken());
        Assertions.assertEquals("refresh-token", response.refreshToken());

        verify(passwordEncoder).encode(registerRequest.password());
        verify(authDAO).saveAndFlush(authUser);

        verify(jwtService).generateToken(USERNAME, USER_ID, USER);
        verify(jwtService).generateRefreshToken(USERNAME, USER_ID, USER);
    }

    @Test
    void register_whenUsernameExists_shouldThrowUsernameAlreadyExists() {
        RegisterRequest registerRequest =
                new RegisterRequest(userRequest, USERNAME, PASSWORD, ADMIN);
        AuthUser authUser = new AuthUser();
        authUser.setUsername(USERNAME);

        when(authMapper.toEntity(registerRequest)).thenReturn(authUser);
        when(authDAO.existingNaming(USERNAME)).thenReturn(authUser.getUsername());

        UsernameAlreadyExists ex =
                Assertions.assertThrows(
                        UsernameAlreadyExists.class, () -> authService.register(registerRequest));
        Assertions.assertTrue(ex.getMessage().contains("Such username"));
        verify(authDAO, never()).saveAndFlush(any());
        verify(userClient, never()).save(any());
    }

    @Test
    void logIn_success_shouldReturnTokens() {
        LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD);

        AuthUser authUser = new AuthUser();
        authUser.setUserId(USER_ID);
        authUser.setUsername(USERNAME);
        authUser.setPasswordHash("encodedPwd");
        authUser.setRole(ADMIN);

        when(authMapper.toLogin(loginRequest)).thenReturn(authUser);
        when(authDAO.findUserByUsername(USERNAME)).thenReturn(Optional.of(authUser));
        when(passwordEncoder.matches(PASSWORD, "encodedPwd")).thenReturn(true);
        when(jwtService.generateToken(USERNAME, USER_ID, ADMIN)).thenReturn("access");
        when(jwtService.generateRefreshToken(USERNAME, USER_ID, ADMIN)).thenReturn("refresh");

        TokenResponse response = authService.logIn(loginRequest);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("access", response.accessToken());
        Assertions.assertEquals("refresh", response.refreshToken());
        verify(passwordEncoder).matches(PASSWORD, "encodedPwd");
    }

    @Test
    void logIn_whenUsernameNotFound_shouldThrowUsernameNotFoundException() {
        LoginRequest loginRequest = new LoginRequest(USERNAME, "wrongPassword");

        AuthUser authUser = new AuthUser();
        authUser.setUserId(USER_ID);
        authUser.setUsername(USERNAME);
        authUser.setPasswordHash("encodedPwd");
        authUser.setRole(ADMIN);

        when(authMapper.toLogin(loginRequest)).thenReturn(authUser);
        when(authDAO.findUserByUsername(USERNAME)).thenReturn(Optional.of(authUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPwd")).thenReturn(false);
        Assertions.assertThrows(
                InvalidUsernameOrPasswordException.class, () -> authService.logIn(loginRequest));
    }

    @Test
    void validate_shouldReturnTrueAndUsername() {
        String token = "Bearer sometoken";
        Claims claims = mock(Claims.class);
        when(jwtService.parse("sometoken")).thenReturn(claims);
        when(claims.getSubject()).thenReturn(USERNAME);

        TokenValidationResponse resp = authService.validate(token);

        Assertions.assertNotNull(resp);
        Assertions.assertTrue(resp.valid());
        Assertions.assertEquals(USERNAME, resp.subject());
    }

    @Test
    void refreshToken_shouldReturnNewTokens() {
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest("refresh-token-value");
        Claims claims = mock(Claims.class);
        when(jwtService.parse(refreshRequest.refreshToken())).thenReturn(claims);
        when(claims.getSubject()).thenReturn(USERNAME);
        when(claims.get("role", String.class)).thenReturn(USER.toString());
        when(claims.get("userId", Long.class)).thenReturn(USER_ID);

        when(jwtService.generateToken(USERNAME, USER_ID, USER)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(USERNAME, USER_ID, USER)).thenReturn("new-refresh");

        TokenResponse resp = authService.refreshToken(refreshRequest);

        Assertions.assertNotNull(resp);
        Assertions.assertEquals("new-access", resp.accessToken());
        Assertions.assertEquals("new-refresh", resp.refreshToken());
    }
}
