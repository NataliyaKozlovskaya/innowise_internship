package com.java.project.util;

import com.java.project.dto.card.CardDTO;
import com.java.project.entity.Card;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CardMapper {

  @Mapping(target = "expirationDate", dateFormat = "dd/MMM/yyyy")
  CardDTO toCardDTO(Card card);
}
