package com.beloboki.unit;

import com.beloboki.client.UserClient;
import com.beloboki.dao.AuthDAO;
import com.beloboki.mapper.AuthMapper;
import com.beloboki.mapper.UserMapper;
import com.beloboki.service.AuthService;
import com.beloboki.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {
    @Mock
    private UserClient userClient;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthDAO authDAO;
    @Mock
    private AuthMapper authMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AuthService authService;


    @Test
    void register_ShouldSaveUserToDAO(){

    }
}
