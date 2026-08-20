package com.beloboki.client;

import com.beloboki.dto.UserResponse;
import com.beloboki.exception.UsernameNotFoundException;
import com.beloboki.model.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserClient {

    private final WebClient userWebClient;

    @CircuitBreaker(name = "authService", fallbackMethod = "fallback")
    public UserResponse save(User user) {
        try {
            return userWebClient
                    .post()
                    .uri("/api/users")
                    .bodyValue(user)
                    .retrieve()
                    .bodyToMono(UserResponse.class)
                    .block();
        } catch (WebClientException e) {
            log.error("Error fetching user by id: {}", e.getMessage());
            throw new UsernameNotFoundException("User not found or service unavailable");
        }
    }

    @CircuitBreaker(name = "authService", fallbackMethod = "rollbackFallback")
    public void deleteUser(Long userId) {
        try {
            userWebClient
                    .delete()
                    .uri("/api/users/" + userId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            log.info("Rollback successful: User {} deleted", userId);
        } catch (WebClientException e) {
            log.error("Failed to rollback user creation for ID {}: {}", userId, e.getMessage());
        }
    }

    public void rollbackFallback(Long userId, Exception e) {
        log.error(
                "Circuit breaker active. Rollback failed for user {}. Manual intervention required."
                        + " Error: {}",
                userId,
                e.getMessage());
    }

    public UserResponse fallback(User user, Exception e) {
        log.error("Circuit breaker fallback: {}", e.getMessage());
        throw new UsernameNotFoundException("User not found or service unavailable");
    }
}
