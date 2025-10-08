package util;

import com.java.project.userservice.dto.card.CardDTO;
import com.java.project.userservice.dto.card.CreateCardRequest;
import com.java.project.userservice.dto.user.CreateUserRequest;
import com.java.project.userservice.dto.user.UserDTO;
import com.java.project.userservice.entity.Card;
import com.java.project.userservice.entity.User;
import java.time.LocalDate;

/**
 * Factory class for creating test data objects used in unit tests and integration tests
 */
public final class TestDataFactory {

  private TestDataFactory() {
  }

  public static CreateUserRequest getCreateUserRequest(String name, String surname, LocalDate date,
      String email) {
    return new CreateUserRequest(name, surname, email, date);
  }

  public static UserDTO getUserDTO(String name, String surname, LocalDate date, String email) {
    return new UserDTO(surname, name, date, email);
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