package com.innowise.user.mapper;

import com.innowise.user.dto.user.UserDTO;
import com.innowise.user.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {

  UserDTO toUserDTO(User user);

  User toUser(UserDTO userDTO);
}
