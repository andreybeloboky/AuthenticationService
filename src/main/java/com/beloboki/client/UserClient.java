package com.beloboki.client;

import com.beloboki.dto.UserResponse;
import com.beloboki.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserClient {

    private final WebClient userWebClient;

    public UserResponse save(User user) {
        return userWebClient
                .post()
                .uri("/api/users")
                .bodyValue(user)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .block();
    }
}
