package com.campusseekers.mapper;

import com.campusseekers.dto.CurrentUserResponse;
import com.campusseekers.dto.LoginResponse;
import com.campusseekers.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface AuthMapper {

    CurrentUserResponse toCurrentUserResponse(User user);

    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "tokenType", ignore = true)
    @Mapping(target = "expiresIn", ignore = true)
    LoginResponse toLoginResponse(User user);
}
