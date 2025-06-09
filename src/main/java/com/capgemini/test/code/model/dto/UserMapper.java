package com.capgemini.test.code.model.dto;

import com.capgemini.test.code.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Entidad -> Dto
    UserResponse toUserResponse(User user);

    UserDetailResponse toUserDetail(User user);

    // Dto -> Entidad
    User toEntity(UserRequest request);
}
