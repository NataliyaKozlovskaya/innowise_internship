package com.innowise.user.util;

import com.innowise.user.dto.card.CardDTO;
import com.innowise.user.dto.card.CreateCardRequest;
import com.innowise.user.dto.user.CreateUserRequest;
import com.innowise.user.dto.user.UserCreateResponse;
import com.innowise.user.dto.user.UserDTO;
import com.innowise.user.entity.Card;
import com.innowise.user.entity.User;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Factory class for creating test data objects used in unit tests and integration tests
 */
public final class TestDataFactory {

  private TestDataFactory() {
  }

  public static CreateUserRequest getCreateUserRequest(String uuid, String name, String surname,
      LocalDate date,
      String email) {
    return new CreateUserRequest(uuid, name, surname, date, email);
  }

  public static UserDTO getUserDTO(String uuid, String name, String surname, LocalDate date,
      String email) {
    return new UserDTO(name, surname, date, email);
  }

  public static UserCreateResponse getUserCreateResponse(String uuid, String name, String surname,
      LocalDate date, String email) {
    return new UserCreateResponse(uuid, name, surname, date, email);
  }

  public static User getUser(UUID uuid, String name, String surname, LocalDate date, String email) {
    User user = new User();
    user.setUuid(uuid.toString());
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
    return new CardDTO(number, holder, expirationDate);
  }

  public static CreateCardRequest getCreateCardRequest(String number, String holder,
      LocalDate expirationDate) {

    return new CreateCardRequest(number, holder, expirationDate);
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