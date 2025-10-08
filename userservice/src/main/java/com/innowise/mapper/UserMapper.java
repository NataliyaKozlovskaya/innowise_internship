package com.java.project.userservice.mapper;

import com.java.project.userservice.dto.user.UserDTO;
import com.java.project.userservice.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {

  UserDTO toUserDTO(User user);

  User toUser(UserDTO userDTO);
}
