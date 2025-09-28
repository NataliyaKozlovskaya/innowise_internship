package com.java.util;

import com.java.project.dto.card.CardDTO;
import com.java.project.dto.card.CreateCardRequest;
import com.java.project.dto.user.CreateUserRequest;
import com.java.project.dto.user.UserDTO;
import com.java.project.entity.Card;
import com.java.project.entity.User;
import java.time.LocalDate;

/**
 * Factory class for creating test data objects used in unit tests and integration tests
 */
public final class TestDataFactory {

  private TestDataFactory() {
  }

  public static CreateUserRequest getCreateUserRequest(String name, String surname, LocalDate date,
      String email) {
    CreateUserRequest request = new CreateUserRequest();
    request.setName(name);
    request.setSurname(surname);
    request.setBirthDate(date);
    request.setEmail(email);

    return request;
  }

  public static UserDTO getUserDTO(String name, String surname, LocalDate date, String email) {
    UserDTO expectedDTO = new UserDTO();
    expectedDTO.setSurname(surname);
    expectedDTO.setName(name);
    expectedDTO.setBirthDate(date);
    expectedDTO.setEmail(email);

    return expectedDTO;
  }

  public static User getUser(Long id, String name, String surname, LocalDate date, String email) {
    User user = new User();
    user.setId(id);
    user.setSurname(surname);
    user.setName(name);
    user.setBirthDate(date);
    user.setEmail(email);

    return user;
  }

  public static Card getCard(Long id, String number, String holder, LocalDate expirationDate) {
    Card card = new Card();
    card.setId(id);
    card.setNumber(number);
    card.setHolder(holder);
    card.setExpirationDate(expirationDate);

    return card;
  }

  public static CardDTO getCardDTO(String number, String holder, LocalDate expirationDate) {
    CardDTO cardDTO = new CardDTO();
    cardDTO.setNumber(number);
    cardDTO.setHolder(holder);
    cardDTO.setExpirationDate(expirationDate);

    return cardDTO;
  }

  public static CreateCardRequest getCreateCardRequest(String number, String holder,
      LocalDate expirationDate) {

    CreateCardRequest request = new CreateCardRequest();
    request.setNumber(number);
    request.setHolder(holder);
    request.setExpirationDate(expirationDate);
    return request;
  }

  public static Card getCard(String number, String holder, LocalDate expirationDate, User user) {
    Card card = new Card();
    card.setNumber(number);
    card.setHolder(holder);
    card.setExpirationDate(expirationDate);
    card.setUser(user);
    return card;
  }
}