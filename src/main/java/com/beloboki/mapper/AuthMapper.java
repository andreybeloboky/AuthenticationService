package com.beloboki.mapper;

import com.beloboki.dto.LoginRequest;
import com.beloboki.dto.RegisterRequest;
import com.beloboki.model.AuthUser;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface AuthMapper {

    AuthUser toEntity(RegisterRequest request);

    AuthUser toLogin(LoginRequest request);
}
