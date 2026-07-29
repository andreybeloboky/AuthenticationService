package com.beloboki.dao;

import com.beloboki.model.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthDAO extends JpaRepository<AuthUser, Long> {

    @Query("SELECT u FROM AuthUser u WHERE u.username = :username AND u.password = :password")
    Long findUserByUsernameAndPassword(String username, String password);
}