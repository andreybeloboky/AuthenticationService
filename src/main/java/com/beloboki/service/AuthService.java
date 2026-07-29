package com.beloboki.service;

import com.beloboki.client.UserClient;
import com.beloboki.dao.AuthDAO;
import com.beloboki.dto.LoginRequest;
import com.beloboki.dto.RegisterRequest;
import com.beloboki.dto.UserResponse;
import com.beloboki.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserClient userClient;
    private final AuthDAO authDAO;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest registerRequest) {
        UserResponse userResponse = userClient.save(registerRequest.authRequest());

        AuthUser authUser = new AuthUser();
        authUser.setRole(registerRequest.role());
        authUser.setUsername(registerRequest.username());
        authUser.setPasswordHash(passwordEncoder.encode(registerRequest.password()));
        authUser.setUserId(userResponse.id());

        authDAO.saveAndFlush(authUser);
    }

    public void logIn(LoginRequest loginRequest){

    }
}
