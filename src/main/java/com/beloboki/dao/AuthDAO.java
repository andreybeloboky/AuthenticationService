package com.beloboki.dao;

import com.beloboki.model.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthDAO extends JpaRepository<AuthUser, Long> {
}