package com.innowise.authentication.mapper;

import com.innowise.authentication.dto.UserRoleDTO;
import com.innowise.authentication.entity.UserRole;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserRoleMapper {

  UserRoleDTO toUserRoleDTO(UserRole userRole);
}
