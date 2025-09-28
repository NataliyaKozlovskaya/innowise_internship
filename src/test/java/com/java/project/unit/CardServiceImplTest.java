package com.java.project.unit;

import static com.java.util.TestDataFactory.getCard;
import static com.java.util.TestDataFactory.getCardDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.java.project.dto.card.CardDTO;
import com.java.project.dto.card.CreateCardRequest;
import com.java.project.dto.card.UpdateCardRequest;
import com.java.project.entity.Card;
import com.java.project.entity.User;
import com.java.project.exception.CardNotFoundException;
import com.java.project.repository.CardRepository;
import com.java.project.repository.UserRepository;
import com.java.project.service.impl.CardServiceImpl;
import com.java.project.util.CardMapper;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

  @Mock
  private CardRepository cardInfoRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private CardMapper cardMapper;

  @InjectMocks
  private CardServiceImpl cardService;

  private final Long USER_ID = 1L;
  private final Long CARD_ID = 1L;
  private final String CARD_NUMBER = "1234567890123456";
  private final String CARD_HOLDER = "Ivan Gyrin";
  private final LocalDate EXPIRATION_DATE = LocalDate.now().plusYears(4);

  private CreateCardRequest getCreateCardRequest(String number, String holder,
      LocalDate expirationDate) {
    CreateCardRequest request = new CreateCardRequest();
    request.setNumber(number);
    request.setHolder(holder);
    request.setExpirationDate(expirationDate);

    return request;
  }

  @Test
  void createCard_WithValidData_ShouldReturnCardDTO() {
    CreateCardRequest request = getCreateCardRequest(CARD_NUMBER, CARD_HOLDER, EXPIRATION_DATE);
    User user = new User();
    user.setId(USER_ID);

    Card savedCard = getCard(CARD_ID, CARD_NUMBER, CARD_HOLDER, EXPIRATION_DATE);
    savedCard.setUser(user);

    CardDTO expectedCardDTO = getCardDTO(CARD_NUMBER, CARD_HOLDER, EXPIRATION_DATE);

    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(cardInfoRepository.save(any(Card.class))).thenReturn(savedCard);
    when(cardMapper.toCardDTO(any(Card.class))).thenReturn(expectedCardDTO);

    CardDTO result = cardService.createCard(USER_ID, request);

    assertNotNull(result);
    assertEquals(expectedCardDTO, result);
    assertEquals(CARD_NUMBER, result.getNumber());
    assertEquals(CARD_HOLDER, result.getHolder());

    verify(userRepository, times(1)).findById(USER_ID);
    verify(cardInfoRepository, times(1)).save(any(Card.class));
    verify(cardMapper, times(1)).toCardDTO(any(Card.class));
  }

  @Test
  void createCard_WhenUserNotFound_ShouldThrowException() {
    CreateCardRequest request = getCreateCardRequest(CARD_NUMBER, CARD_HOLDER, EXPIRATION_DATE);

    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> cardService.createCard(USER_ID, request));

    assertEquals("User not found with id: " + USER_ID, exception.getMessage());
    verify(userRepository, times(1)).findById(USER_ID);
    verify(cardInfoRepository, never()).save(any(Card.class));
    verify(cardMapper, never()).toCardDTO(any(Card.class));
  }

  @Test
  void getCardById_WithExistingId_ShouldReturnCardDTO() {
    Card card = getCard(CARD_ID, CARD_NUMBER, CARD_HOLDER, EXPIRATION_DATE);

    CardDTO expectedCardDTO = getCardDTO(CARD_NUMBER, CARD_HOLDER, EXPIRATION_DATE);

    when(cardInfoRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
    when(cardMapper.toCardDTO(card)).thenReturn(expectedCardDTO);

    CardDTO result = cardService.getCardById(CARD_ID);

    assertNotNull(result);
    assertEquals(expectedCardDTO, result);
    verify(cardInfoRepository, times(1)).findById(CARD_ID);
    verify(cardMapper, times(1)).toCardDTO(card);
  }

  @Test
  void getCardById_WithNonExistingId_ShouldThrowCardNotFoundException() {
    when(cardInfoRepository.findById(CARD_ID)).thenReturn(Optional.empty());

    CardNotFoundException exception = assertThrows(CardNotFoundException.class,
        () -> cardService.getCardById(CARD_ID));

    assertEquals("Card not found with id: " + CARD_ID, exception.getMessage());
    verify(cardInfoRepository, times(1)).findById(CARD_ID);
    verify(cardMapper, never()).toCardDTO(any(Card.class));
  }

  @Test
  void getCardsByIds_WithExistingIds_ShouldReturnCardDTOList() {
    List<Long> cardIds = Arrays.asList(1L, 2L, 3L);

    Card card1 = new Card();
    card1.setId(1L);
    Card card2 = new Card();
    card2.setId(2L);
    Card card3 = new Card();
    card3.setId(3L);

    List<Card> cards = Arrays.asList(card1, card2, card3);

    CardDTO cardDTO1 = getCardDTO("111111111", "Ivan", EXPIRATION_DATE);
    CardDTO cardDTO2 = new CardDTO("222222222", "Petr", EXPIRATION_DATE);
    CardDTO cardDTO3 = new CardDTO("3333333333", "Olga", EXPIRATION_DATE);
    List<CardDTO> expectedCardDTOs = Arrays.asList(cardDTO1, cardDTO2, cardDTO3);

    when(cardInfoRepository.findByIdIn(cardIds)).thenReturn(cards);
    when(cardMapper.toCardDTO(card1)).thenReturn(cardDTO1);
    when(cardMapper.toCardDTO(card2)).thenReturn(cardDTO2);
    when(cardMapper.toCardDTO(card3)).thenReturn(cardDTO3);

    List<CardDTO> result = cardService.getCardsByIds(cardIds);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals(expectedCardDTOs, result);
    verify(cardInfoRepository, times(1)).findByIdIn(cardIds);
    verify(cardMapper, times(3)).toCardDTO(any(Card.class));
  }

  @Test
  void getCardsByIds_WithNonExistingIds_ShouldThrowCardNotFoundException() {
    List<Long> cardIds = Arrays.asList(1L, 2L, 3L);
    when(cardInfoRepository.findByIdIn(cardIds)).thenReturn(List.of());

    CardNotFoundException exception = assertThrows(CardNotFoundException.class,
        () -> cardService.getCardsByIds(cardIds));

    assertEquals("Cards not found with ids: " + cardIds, exception.getMessage());
    verify(cardInfoRepository, times(1)).findByIdIn(cardIds);
    verify(cardMapper, never()).toCardDTO(any(Card.class));
  }

  @Test
  void getCardsByIds_WithEmptyIdList_ShouldThrowCardNotFoundException() {
    List<Long> emptyCardIds = List.of();
    when(cardInfoRepository.findByIdIn(emptyCardIds)).thenReturn(List.of());

    CardNotFoundException exception = assertThrows(CardNotFoundException.class,
        () -> cardService.getCardsByIds(emptyCardIds));

    assertEquals("Cards not found with ids: " + emptyCardIds, exception.getMessage());
    verify(cardInfoRepository, times(1)).findByIdIn(emptyCardIds);
    verify(cardMapper, never()).toCardDTO(any(Card.class));
  }

  @Test
  void updateCard_WithExistingId_ShouldReturnUpdatedCardDTO() {
    String updatedHolder = "Maria Tafl";
    UpdateCardRequest request = new UpdateCardRequest(updatedHolder);

    Card existingCard = getCard(CARD_ID, CARD_NUMBER, CARD_HOLDER, EXPIRATION_DATE);

    Card updatedCard = getCard(CARD_ID, CARD_NUMBER, updatedHolder, EXPIRATION_DATE);

    CardDTO expectedCardDTO = getCardDTO(CARD_NUMBER, updatedHolder, EXPIRATION_DATE);

    when(cardInfoRepository.findById(CARD_ID)).thenReturn(Optional.of(existingCard));
    when(cardInfoRepository.save(existingCard)).thenReturn(updatedCard);
    when(cardMapper.toCardDTO(updatedCard)).thenReturn(expectedCardDTO);

    CardDTO result = cardService.updateCard(CARD_ID, request);

    assertNotNull(result);
    assertEquals(expectedCardDTO, result);
    assertEquals(updatedHolder, result.getHolder());

    verify(cardInfoRepository, times(1)).findById(CARD_ID);
    verify(cardInfoRepository, times(1)).save(existingCard);
    verify(cardMapper, times(1)).toCardDTO(updatedCard);
  }

  @Test
  void updateCard_WithNonExistingId_ShouldThrowException() {
    UpdateCardRequest request = new UpdateCardRequest("New Holder");
    when(cardInfoRepository.findById(CARD_ID)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> cardService.updateCard(CARD_ID, request));

    assertEquals("Card not found with id: " + CARD_ID, exception.getMessage());
    verify(cardInfoRepository, times(1)).findById(CARD_ID);
    verify(cardInfoRepository, never()).save(any(Card.class));
    verify(cardMapper, never()).toCardDTO(any(Card.class));
  }

  @Test
  void deleteCard_WithExistingId_ShouldDeleteCard() {
    Card card = new Card();
    card.setId(CARD_ID);
    when(cardInfoRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
    doNothing().when(cardInfoRepository).delete(card);

    cardService.deleteCard(CARD_ID);

    verify(cardInfoRepository, times(1)).findById(CARD_ID);
    verify(cardInfoRepository, times(1)).delete(card);
  }

  @Test
  void deleteCard_WithNonExistingId_ShouldThrowException() {
    when(cardInfoRepository.findById(CARD_ID)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> cardService.deleteCard(CARD_ID));

    assertEquals("Card not found with id: " + CARD_ID, exception.getMessage());
    verify(cardInfoRepository, times(1)).findById(CARD_ID);
    verify(cardInfoRepository, never()).delete(any(Card.class));
  }

  @Test
  void createCard_ShouldSetCorrectExpirationDate() {
    CreateCardRequest request = getCreateCardRequest(CARD_NUMBER, CARD_HOLDER, EXPIRATION_DATE);
    User user = new User();
    user.setId(USER_ID);

    Card savedCard = getCard(CARD_ID, CARD_NUMBER, CARD_HOLDER, EXPIRATION_DATE);

    CardDTO expectedCardDTO = getCardDTO(CARD_NUMBER, CARD_HOLDER, EXPIRATION_DATE);

    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(cardInfoRepository.save(any(Card.class))).thenAnswer(invocation -> {
      Card cardArg = invocation.getArgument(0);

      assertEquals(LocalDate.now().plusYears(4), cardArg.getExpirationDate());
      return savedCard;
    });
    when(cardMapper.toCardDTO(any(Card.class))).thenReturn(expectedCardDTO);

    CardDTO result = cardService.createCard(USER_ID, request);

    assertNotNull(result);
    verify(cardInfoRepository, times(1)).save(any(Card.class));
  }
}