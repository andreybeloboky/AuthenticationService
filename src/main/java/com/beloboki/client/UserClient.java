package com.beloboki.client;

import com.beloboki.dto.AuthRequest;
import com.beloboki.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserClient {

    private final WebClient userWebClient;

    public UserResponse save(AuthRequest request) {
        return userWebClient.post()
                .uri("/api/users")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .block();
    }
}
