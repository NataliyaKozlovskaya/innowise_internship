package com.java.project.mapper;

import com.java.project.dto.card.CardDTO;
import com.java.project.entity.Card;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CardMapper {

  CardDTO toCardDTO(Card card);
}