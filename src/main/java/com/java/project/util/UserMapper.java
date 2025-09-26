package com.java.project.util;

import com.java.project.dto.user.UserDTO;
import com.java.project.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {

  @Mapping(target = "birthDate", dateFormat = "dd/MMM/yyyy")
  UserDTO toUserDTO(User user);
}
