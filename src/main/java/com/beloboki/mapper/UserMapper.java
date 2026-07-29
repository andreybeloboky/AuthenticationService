package com.beloboki.mapper;

import com.beloboki.dto.RegisterRequest;
import com.beloboki.model.User;

public interface UserMapper {
    User toUser(RegisterRequest registerRequest);
}
