package com.beloboki.mapper;

import com.beloboki.model.AuthUser;

public interface AuthMapper {
    AuthUser findByLogin(String login);
}
