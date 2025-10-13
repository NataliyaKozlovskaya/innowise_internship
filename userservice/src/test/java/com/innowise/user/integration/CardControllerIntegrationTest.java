package com.innowise.user.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.innowise.user.dto.card.CardDTO;
import com.innowise.user.dto.card.CreateCardRequest;
import com.innowise.user.dto.card.UpdateCardRequest;
import com.innowise.user.util.TestDataFactory;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cache.type=none"
})
class CardControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;
  private String baseUrl;


  @BeforeEach
  void setUp() {
    baseUrl = "/api/v1/cards";
    restTemplate.getRestTemplate().setRequestFactory(
        new HttpComponentsClientHttpRequestFactory()
    );
  }

  @Test
  @Sql(statements = {
      "INSERT INTO users (uuid, name, surname, email, birth_date) VALUES (1, 'Petr', 'Ivanov', 'petro@gmail.com', '1990-01-01')"
  }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(statements = "DELETE FROM card_info; DELETE FROM users", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void createCard_WithValidData_ShouldReturnCreatedCard() {
    CreateCardRequest request = TestDataFactory.getCreateCardRequest("1234567890123456",
        "Petr Ivanov",
        LocalDate.now().plusYears(4));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<CreateCardRequest> entity = new HttpEntity<>(request, headers);

    ResponseEntity<CardDTO> response = restTemplate.exchange(
        baseUrl + "?userId=1",
        HttpMethod.POST,
        entity,
        CardDTO.class
    );

    assertEquals(HttpStatus.CREATED, response.getStatusCode());

    CardDTO responseBody = response.getBody();
    assertNotNull(responseBody);
    assertEquals("1234567890123456", responseBody.number());
    assertEquals("Petr Ivanov", responseBody.holder());
  }

  @Test
  @Sql(statements = {
      "INSERT INTO users (uuid, name, surname, email, birth_date) VALUES " +
          "(1, 'Max', 'Nio', 'max@example.com', '1990-01-01')",
      "INSERT INTO card_info (id, number, holder, expiration_date, user_uuid) VALUES " +
          "(1, '1234567890123456', 'Max Nio', '2026-12-31', 1)"
  }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(statements = "DELETE FROM card_info; DELETE FROM users", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void getCardById_WithExistingId_ShouldReturnCard() {
    ResponseEntity<CardDTO> response = restTemplate.getForEntity(
        baseUrl + "/1",
        CardDTO.class
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("1234567890123456", response.getBody().number());
    assertEquals("Max Nio", response.getBody().holder());
  }

  @Test
  void getCardById_WithNonExistingId_ShouldReturnNotFound() {
    ResponseEntity<String> response = restTemplate.getForEntity(
        baseUrl + "/9999",
        String.class
    );

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  @Sql(statements = {
      "INSERT INTO users (uuid, name, surname, email, birth_date) VALUES " +
          "(1, 'Mark', 'Nio', 'mark@example.com', '1990-07-01')",
      "INSERT INTO card_info (id, number, holder, expiration_date, user_uuid) VALUES " +
          "(1, '1111222233334444', 'Mark DOE', '2026-12-31', 1), " +
          "(2, '5555666677778888', 'Mark SMITH', '2027-06-30', 1), " +
          "(3, '9999000011112222', 'Mark JOHNSON', '2028-01-31', 1)"
  }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(statements = "DELETE FROM card_info; DELETE FROM users", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void getCardsByIds_WithExistingIds_ShouldReturnCards() {

    ResponseEntity<CardDTO[]> response = restTemplate.getForEntity(
        baseUrl + "/batch?ids=1,2",
        CardDTO[].class
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().length);

    List<CardDTO> cards = Arrays.asList(response.getBody());
    assertTrue(cards.stream().anyMatch(c -> c.number().equals("1111222233334444")));
    assertTrue(cards.stream().anyMatch(c -> c.number().equals("5555666677778888")));
    assertTrue(cards.stream().noneMatch(c -> c.number().equals("9999000011112222")));
  }

  @Test
  @Sql(statements = {
      "INSERT INTO users (uuid, name, surname, email, birth_date) VALUES " +
          "(2, 'Mark', 'Nio', 'markPP@example.com', '1990-07-01')",
      "INSERT INTO card_info (id, number, holder, expiration_date, user_uuid) VALUES " +
          "(1, '1234567890123456', 'Mark Nio', '2026-12-31', 2)"
  }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(statements = "DELETE FROM card_info; DELETE FROM users", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void updateCard_WithValidData_ShouldReturnUpdatedCard() {
    UpdateCardRequest request = new UpdateCardRequest("MARK NIO");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<UpdateCardRequest> entity = new HttpEntity<>(request, headers);

    String url = baseUrl + "/" + "1";
    ResponseEntity<CardDTO> response = restTemplate.exchange(
        url,
        HttpMethod.PATCH,
        entity,
        CardDTO.class
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("MARK NIO", response.getBody().holder());
    assertEquals("1234567890123456",
        response.getBody().number());
  }

  @Test
  @Sql(statements = {
      "INSERT INTO users (uuid, name, surname, email, birth_date) VALUES " +
          "(41, 'Mark', 'Nio', 'mark@example.com', '1990-07-01')",
      "INSERT INTO card_info (id, number, holder, expiration_date, user_uuid) VALUES " +
          "(1, '1234567890123456', 'Mark Nio', '2026-12-31', 41)"
  }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(statements = "DELETE FROM card_info; DELETE FROM users", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void deleteCard_WithExistingId_ShouldReturnNoContent() {

    ResponseEntity<Void> response = restTemplate.exchange(
        baseUrl + "/1",
        HttpMethod.DELETE,
        null,
        Void.class
    );

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    ResponseEntity<String> getResponse = restTemplate.getForEntity(
        baseUrl + "/1",
        String.class
    );
    assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
  }

  @Test
  void createCard_WithInvalidUserId_ShouldReturnBadRequest() {
    CreateCardRequest request = TestDataFactory.getCreateCardRequest("1234560090123456",
        "Anna Ivanova",
        LocalDate.now().plusYears(2));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<CreateCardRequest> entity = new HttpEntity<>(request, headers);

    ResponseEntity<String> response = restTemplate.exchange(
        baseUrl + "?userId" + 999999F,
        HttpMethod.POST,
        entity,
        String.class
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @Sql(statements = {
      "INSERT INTO users (uuid, name, surname, email, birth_date) VALUES " +
          "(1, 'Mark', 'Nio', 'mark@example.com', '1990-07-01')"
  }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  void createCard_WithInvalidCardNumber_ShouldReturnBadRequest() {
    CreateCardRequest request = TestDataFactory.getCreateCardRequest(null, "Anna Ivanova",
        LocalDate.now().plusYears(2));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<CreateCardRequest> entity = new HttpEntity<>(request, headers);

    ResponseEntity<String> response = restTemplate.exchange(
        baseUrl + "?userId=" + 1,
        HttpMethod.POST,
        entity,
        String.class
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @Sql(statements = {
      "INSERT INTO users (uuid, name, surname, email, birth_date) VALUES " +
          "(41, 'Mark', 'Nio', 'mark@example.com', '1990-07-01')",
      "INSERT INTO card_info (id, number, holder, expiration_date, user_uuid) VALUES " +
          "(1, '1234567890123456', 'Mark Nio', '2026-12-31', 41)"
  }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(statements = "DELETE FROM card_info; DELETE FROM users", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void updateCard_WithNonExistingId_ShouldReturnNotFound() {

    UpdateCardRequest request = new UpdateCardRequest("NEW HOLDER");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<UpdateCardRequest> entity = new HttpEntity<>(request, headers);

    String url = baseUrl + "/" + "11111";
    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.PATCH,
        entity,
        String.class
    );

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getCardsByIds_WithEmptyList_ShouldReturnNotFound() {
    ResponseEntity<String> response = restTemplate.getForEntity(
        baseUrl + "/batch?ids=",
        String.class
    );

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }
}