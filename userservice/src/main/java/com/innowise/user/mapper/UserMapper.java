package com.innowise.user.mapper;

import com.innowise.user.dto.user.UserCreateResponse;
import com.innowise.user.dto.user.UserDTO;
import com.innowise.user.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {

  UserCreateResponse toUserCreateResponse(User user);

  UserDTO toUserDTO(User user);

  User toUser(UserCreateResponse userCreateResponse);
}
