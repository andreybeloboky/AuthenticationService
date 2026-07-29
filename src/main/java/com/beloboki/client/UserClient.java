package com.beloboki.client;

import com.beloboki.dto.UserRequest;
import com.beloboki.dto.UserResponse;
import com.beloboki.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserClient {

    private final WebClient userWebClient;

    public UserResponse save(User user) {
        return userWebClient.post()
                .uri("/api/users")
                .bodyValue(user)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .block();
    }
}
