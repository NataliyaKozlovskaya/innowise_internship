package com.java.project.userservice.mapper;

import com.java.project.userservice.dto.card.CardDTO;
import com.java.project.userservice.entity.Card;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CardMapper {

  CardDTO toCardDTO(Card card);
}