package com.beloboki.mapper;

import com.beloboki.dto.UserRequest;
import com.beloboki.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toUser(UserRequest userRequest);
}
