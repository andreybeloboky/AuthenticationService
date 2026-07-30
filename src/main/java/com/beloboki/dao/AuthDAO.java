package com.beloboki.dao;

import com.beloboki.model.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthDAO extends JpaRepository<AuthUser, Long> {

    @Query(
            value = "SELECT user_id FROM auth_users WHERE username = ? AND password_hash = ?",
            nativeQuery = true)
    Long findUserIdByUsernameAndPassword(String username, String password);

    @Query(value = "SELECT username FROM auth_users WHERE username = ?", nativeQuery = true)
    String existingNaming(String username);
}
