package com.innowise.user.mapper;

import com.innowise.user.dto.card.CardDTO;
import com.innowise.user.dto.card.CreateCardRequest;
import com.innowise.user.entity.Card;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CardMapper {

  CardDTO toCardDTO(Card card);

  Card toCard(CreateCardRequest сreateCardRequest);

}