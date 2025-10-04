package com.java.project.mapper;

import com.java.project.dto.user.UserDTO;
import com.java.project.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {

  UserDTO toUserDTO(User user);

  User toUser(UserDTO userDTO);
}
