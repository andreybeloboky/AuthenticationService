package com.beloboki.dao;

import com.beloboki.model.AuthUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthDAO extends JpaRepository<AuthUser, Long> {

    @Query(value = "SELECT * FROM auth_users WHERE username = ?", nativeQuery = true)
    Optional<AuthUser> findUserByUsername(String username);

    @Query(value = "SELECT username FROM auth_users WHERE username = ?", nativeQuery = true)
    String existingNaming(String username);
}
