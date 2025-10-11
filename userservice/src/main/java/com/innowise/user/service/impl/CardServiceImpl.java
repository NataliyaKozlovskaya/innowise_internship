package com.innowise.user.service.impl;

import com.innowise.user.dto.card.CardDTO;
import com.innowise.user.dto.card.CreateCardRequest;
import com.innowise.user.dto.card.UpdateCardRequest;
import com.innowise.user.entity.Card;
import com.innowise.user.entity.User;
import com.innowise.user.exception.CardNotFoundException;
import com.innowise.user.mapper.CardMapper;
import com.innowise.user.repository.CardRepository;
import com.innowise.user.service.CardService;
import com.innowise.user.service.UserService;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AllArgsConstructor
@Transactional
public class CardServiceImpl implements CardService {

  private static final String CARD_NOT_FOUND = "Card not found with id: ";
  private final CardRepository cardInfoRepository;
  private final UserService userService;
  private final CardMapper cardMapper;

  @Transactional
  @Override
  public CardDTO createCard(String userId, CreateCardRequest request) {
    User user = userService.getUserEntityById(userId);

    Card card = new Card();
    card.setNumber(request.number());
    card.setHolder(request.holder());
    card.setExpirationDate(LocalDate.now().plusYears(4));
    card.setUser(user);
    cardInfoRepository.save(card);

    return cardMapper.toCardDTO(card);
  }

  @Transactional(readOnly = true)
  @Override
  public CardDTO getCardById(Long id) {
    return cardInfoRepository.findById(id)
        .map(cardMapper::toCardDTO)
        .orElseThrow(() -> new CardNotFoundException(CARD_NOT_FOUND + id));
  }

  @Transactional(readOnly = true)
  @Override
  public List<CardDTO> getCardsByIds(List<Long> ids) {
    List<CardDTO> result = cardInfoRepository.findByIdIn(ids).stream()
        .map(cardMapper::toCardDTO)
        .toList();

    if (result.isEmpty()) {
      throw new CardNotFoundException("Cards not found with ids: " + ids);
    }
    return result;
  }

  @Transactional
  @Override
  public CardDTO updateCard(Long id, UpdateCardRequest request) {
    Card card = cardInfoRepository.findById(id)
        .orElseThrow(() -> new CardNotFoundException(CARD_NOT_FOUND + id));

    card.setHolder(request.holder());
    Card updatedCard = cardInfoRepository.save(card);

    return cardMapper.toCardDTO(updatedCard);
  }

  @Transactional
  @Override
  public void deleteCard(Long id) {
    Card card = cardInfoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException(CARD_NOT_FOUND + id));
    cardInfoRepository.delete(card);
  }
}
