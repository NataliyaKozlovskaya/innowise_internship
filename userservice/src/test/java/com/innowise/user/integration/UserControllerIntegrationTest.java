package com.innowise.user.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.innowise.user.dto.user.CreateUserRequest;
import com.innowise.user.dto.user.UpdateUserRequest;
import com.innowise.user.dto.user.UserCreateResponse;
import com.innowise.user.dto.user.UserDTO;
import com.innowise.user.entity.User;
import com.innowise.user.mapper.UserMapper;
import com.innowise.user.util.TestDataFactory;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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
class UserControllerIntegrationTest extends AbstractIntegrationTest {

  private final UUID uuid = UUID.randomUUID();
  private final String EMAIL = "test@example.com";
  private final String NAME = "Mark";
  private final String SURNAME = "Staf";
  private final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
  private String baseUrl;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private UserMapper userMapper;

  @BeforeEach
  void setUp() {
    baseUrl = "/api/v1/users";
    restTemplate.getRestTemplate().setRequestFactory(
        new HttpComponentsClientHttpRequestFactory()
    );
  }

  @Test
  @Sql(statements = "DELETE FROM users", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  void createUser_WithValidData_ShouldReturnCreatedUser() {
    CreateUserRequest request = new CreateUserRequest(
        UUID.randomUUID().toString(),
        "Mark",
        "Staf",
        LocalDate.of(1990, 1, 1),
        "test@example.com"
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request, headers);

    ResponseEntity<UserCreateResponse> response = restTemplate.exchange(
        baseUrl + "/register",
        HttpMethod.POST,
        entity,
        UserCreateResponse.class
    );

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("test@example.com", response.getBody().email());
    assertEquals("Mark", response.getBody().name());
  }

  @Test
  @Sql(statements = {
      "INSERT INTO users (uuid, name, surname, email, birth_date) VALUES ('121', 'Anna', 'Holl', 'annaaa@example.com', '1990-01-01')"
  }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(statements = "DELETE FROM users", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void getUserById_WithExistingId_ShouldReturnUser() {

    ResponseEntity<UserCreateResponse> response = restTemplate.getForEntity(
        baseUrl + "/121",
        UserCreateResponse.class
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("annaaa@example.com", response.getBody().email());
    assertEquals("Anna", response.getBody().name());
  }

  @Test
  void testMapperDirectly() {
    User user = new User();
    user.setUuid(UUID.randomUUID().toString());
    user.setName("Anna");
    user.setSurname("Holl");
    user.setEmail("annaaa@example.com");
    user.setBirthDate(LocalDate.of(1990, 1, 1));

    UserDTO dto = userMapper.toUserDTO(user);

    assertNotNull(dto);
    assertEquals("annaaa@example.com", dto.email());
    assertEquals("Anna", dto.name());
  }

  @Test
  void getUserById_WithNonExistingId_ShouldReturnNotFound() {
    ResponseEntity<String> response = restTemplate.getForEntity(
        baseUrl + "/999",
        String.class
    );
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  @Sql(statements = {
      "INSERT INTO users (uuid, name, surname, email, birth_date) VALUES " +
          "('user-1', 'Anna', 'Holl', 'anna@example.com', '1990-01-01'), " +
          "('user-2', 'Olga', 'Smyth', 'olga@example.com', '1991-02-02'), " +
          "('user-3', 'Alex', 'Vasin', 'alex@example.com', '1992-03-03')"
  }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(statements = "DELETE FROM users", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void getUsersByIds_WithExistingIds_ShouldReturnUsers() {
    String uuid1 = "user-1";
    String uuid2 = "user-2";

    ResponseEntity<UserDTO[]> response = restTemplate.getForEntity(
        baseUrl + "/batch?ids=" + uuid1 + "," + uuid2,
        UserDTO[].class
    );

    System.out.println("Status: " + response.getStatusCode());
    System.out.println("Body: " + Arrays.toString(response.getBody()));

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().length);

    List<UserDTO> users = Arrays.asList(response.getBody());
    for (UserDTO user : users) {
      assertNotNull(user, "User should not be null");
    }

    List<String> emails = users.stream()
        .map(UserDTO::email)
        .collect(Collectors.toList());

    assertTrue(emails.contains("anna@example.com"));
    assertTrue(emails.contains("olga@example.com"));
  }

  @Test
  @Sql(statements = {
      "INSERT INTO public.users (uuid, name, surname, email, birth_date) VALUES ('1', 'Anna', 'Holl', 'anna@example.com', '1990-01-01')"
  }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(statements = "DELETE FROM public.users", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void getUserByEmail_WithExistingEmail_ShouldReturnUser() {

    ResponseEntity<UserDTO> response = restTemplate.getForEntity(
        baseUrl + "/email?email=anna@example.com",

        UserDTO.class
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("anna@example.com", response.getBody().email());
    assertEquals("Anna", response.getBody().name());
  }

  @Test
  @Sql(statements = {
      "INSERT INTO users (uuid, name, surname, email, birth_date) VALUES (1, 'Anna', 'Holl', 'anna@example.com', '1990-01-01')"
  }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(statements = "DELETE FROM users", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void updateUser_WithValidData_ShouldReturnUpdatedUser() {
    UpdateUserRequest request = new UpdateUserRequest("AnnaUp", "HollUp",
        "annaUp@example.com");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(request, headers);

    ResponseEntity<UserDTO> response = restTemplate.exchange(
        baseUrl + "/1",
        HttpMethod.PATCH,
        entity,
        UserDTO.class
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());

    assertEquals("AnnaUp", response.getBody().name());
    assertEquals("HollUp", response.getBody().surname());
    assertEquals("annaUp@example.com", response.getBody().email());
  }

  @Test
  @Sql(statements = {
      "INSERT INTO users (uuid, name, surname, email, birth_date) VALUES (141, 'Anna', 'Holl', 'anna@example.com','1990-01-01')"
  }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(statements = "DELETE FROM users", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void deleteUser_WithExistingId_ShouldReturnNoContent() {
    ResponseEntity<Void> response = restTemplate.exchange(
        baseUrl + "/141",
        HttpMethod.DELETE,
        null,
        Void.class
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());

    ResponseEntity<String> getResponse = restTemplate.getForEntity(
        baseUrl + "/141",
        String.class
    );
    assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
  }

  @Test
  void createUser_WithInvalidData_ShouldReturnBadRequest() {
    CreateUserRequest request = TestDataFactory.getCreateUserRequest(uuid.toString(), NAME, SURNAME,
        LocalDate.now().plusDays(1), EMAIL);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request, headers);

    ResponseEntity<String> response = restTemplate.exchange(
        baseUrl + "/register",
        HttpMethod.POST,
        entity,
        String.class
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @Sql(statements = {
      "INSERT INTO users (uuid, name, surname, email, birth_date) VALUES (11, 'Anna', 'Holl', 'annaTT@gmail.com', '1990-01-01')"
  }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(statements = "DELETE FROM users", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  void createUser_WithDuplicateEmail_ShouldReturnConflict() {
    CreateUserRequest request = TestDataFactory.getCreateUserRequest(uuid.toString(), NAME, SURNAME,
        BIRTH_DATE, "annaTT@gmail.com");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request, headers);

    ResponseEntity<UserCreateResponse> response = restTemplate.exchange(
        baseUrl + "/register",
        HttpMethod.POST,
        entity,
        UserCreateResponse.class
    );

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
  }
}