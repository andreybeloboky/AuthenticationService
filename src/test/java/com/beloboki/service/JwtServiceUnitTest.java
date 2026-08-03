package com.beloboki.service;

import com.beloboki.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JwtServiceUnitTest {

    private JwtService jwtService;

    private static final String USERNAME = "TEST";
    private static final Long USER_ID = 1L;
    private static final Role ROLE = Role.USER;
    private static final String VALUE_USER_ID = "userId";
    private static final String VALUE_ROLE = "role";

    @BeforeEach
    void setUp() {
        String secret = "01234567890123456789012345678901";
        jwtService = new JwtService(secret);
    }

    @Test
    void generateAndParseToken_shouldContainClaimsAndSubject() {
        String token = jwtService.generateToken(USERNAME, USER_ID, ROLE);
        Assertions.assertNotNull(token);
        Assertions.assertFalse(token.isBlank());

        Claims claims = jwtService.parse(token);
        Assertions.assertNotNull(claims);

        Assertions.assertEquals(USERNAME, claims.getSubject());
        Assertions.assertEquals(USER_ID, claims.get(VALUE_USER_ID, Long.class));

        Assertions.assertEquals(ROLE.toString(), claims.get(VALUE_ROLE, String.class));
    }

    @Test
    void generateTokenAndRefreshToken_shouldBeDifferent() {
        String token = jwtService.generateToken(USERNAME, USER_ID, ROLE);
        String refresh = jwtService.generateRefreshToken(USERNAME, USER_ID, ROLE);

        Assertions.assertNotNull(token);
        Assertions.assertNotNull(refresh);
        Assertions.assertNotEquals(token, refresh);
    }

    @Test
    void parse_invalidToken_shouldThrowJwtExceptionWithCustomMessage() {
        String badToken = "bad";
        Assertions.assertThrows(JwtException.class, () -> jwtService.parse(badToken));
    }
}
