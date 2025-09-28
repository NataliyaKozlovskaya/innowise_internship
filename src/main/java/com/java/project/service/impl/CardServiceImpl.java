package com.java.project.service.impl;

import com.java.project.dto.card.CardDTO;
import com.java.project.dto.card.CreateCardRequest;
import com.java.project.dto.card.UpdateCardRequest;
import com.java.project.entity.Card;
import com.java.project.entity.User;
import com.java.project.exception.CardNotFoundException;
import com.java.project.exception.UserNotFoundException;
import com.java.project.repository.CardRepository;
import com.java.project.repository.UserRepository;
import com.java.project.service.CardService;
import com.java.project.util.CardMapper;
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
  private final UserRepository userRepository;
  private final CardMapper cardMapper;

  @Transactional
  @Override
  public CardDTO createCard(Long userId, CreateCardRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

    Card card = new Card();
    card.setNumber(request.getNumber());
    card.setHolder(request.getHolder());
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

    card.setHolder(request.getHolder());
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
