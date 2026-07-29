package com.beloboki.service;

import com.beloboki.client.UserClient;
import com.beloboki.dao.AuthDAO;
import com.beloboki.dto.LoginRequest;
import com.beloboki.dto.RegisterRequest;
import com.beloboki.dto.UserResponse;
import com.beloboki.mapper.AuthMapper;
import com.beloboki.mapper.UserMapper;
import com.beloboki.model.AuthUser;
import com.beloboki.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserClient userClient;
    private final AuthDAO authDAO;
    private final AuthMapper authMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public Long register(RegisterRequest registerRequest) {
        AuthUser authUser = authMapper.toEntity(registerRequest);
        User user = userMapper.toUser(registerRequest.userRequest());

        UserResponse userResponse = userClient.save(user);

        authUser.setPasswordHash(passwordEncoder.encode(registerRequest.password()));
        authUser.setUserId(userResponse.id());

        authDAO.saveAndFlush(authUser);

        return findUserIdByUsernameAndPassword(authUser.getUsername(), authUser.getPasswordHash());
    }

    public Long logIn(LoginRequest loginRequest) {
        AuthUser authUser = authMapper.toLogin(loginRequest);
        authUser.setPasswordHash(passwordEncoder.encode(loginRequest.password()));

        return findUserIdByUsernameAndPassword(authUser.getUsername(), authUser.getPasswordHash());
    }

    private Long findUserIdByUsernameAndPassword(String username, String hashPassword) {
        return authDAO.findUserByUsernameAndPassword(username,
                passwordEncoder.encode(hashPassword));
    }
}
