package com.java.project.unit;

import static com.java.util.TestDataFactory.getCreateUserRequest;
import static com.java.util.TestDataFactory.getUser;
import static com.java.util.TestDataFactory.getUserDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.java.project.dto.user.CreateUserRequest;
import com.java.project.dto.user.UpdateUserRequest;
import com.java.project.dto.user.UserDTO;
import com.java.project.entity.User;
import com.java.project.exception.EmailAlreadyExistsException;
import com.java.project.exception.UserNotFoundException;
import com.java.project.repository.UserRepository;
import com.java.project.service.impl.UserServiceImpl;
import com.java.project.util.UserMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private UserMapper userMapper;
  @InjectMocks
  private UserServiceImpl userService;

  private final Long USER_ID = 1L;
  private final String EMAIL = "test@example.com";
  private final String NAME = "Mark";
  private final String SURNAME = "Staf";
  private final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);


  @Test
  void createUser_ShouldCreateUserSuccessfully() {
    CreateUserRequest request = getCreateUserRequest(NAME, SURNAME, BIRTH_DATE, EMAIL);

    User savedUser = getUser(USER_ID, NAME, SURNAME, BIRTH_DATE, EMAIL);
    UserDTO expectedDTO = getUserDTO(NAME, SURNAME, BIRTH_DATE, EMAIL);

    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(userMapper.toUserDTO(savedUser)).thenReturn(expectedDTO);

    UserDTO result = userService.createUser(request);

    assertNotNull(result);
    assertEquals(expectedDTO.email(), result.email());

    verify(userRepository).findByEmail(EMAIL);
    verify(userRepository).save(any(User.class));
    verify(userMapper).toUserDTO(savedUser);
  }

  @Test
  void createUser_ShouldThrowEmailAlreadyExistsException() {
    CreateUserRequest request = getCreateUserRequest(NAME, SURNAME, BIRTH_DATE, EMAIL);
    User existingUser = getUser(USER_ID, NAME, SURNAME, BIRTH_DATE, EMAIL);

    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));

    assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(request));
    verify(userRepository).findByEmail(EMAIL);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void getUserById_ShouldReturnUserDTO() {
    User user = getUser(USER_ID, NAME, SURNAME, BIRTH_DATE, EMAIL);
    UserDTO expectedDTO = getUserDTO(NAME, SURNAME, BIRTH_DATE, EMAIL);

    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(userMapper.toUserDTO(user)).thenReturn(expectedDTO);

    UserDTO result = userService.getUserById(USER_ID);

    assertNotNull(result);
    verify(userRepository).findById(USER_ID);
    verify(userMapper).toUserDTO(user);
  }

  @Test
  void getUserById_ShouldThrowUserNotFoundException() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.getUserById(USER_ID));
    verify(userRepository).findById(USER_ID);
    verify(userMapper, never()).toUserDTO(any());
  }

  @Test
  void getUsersByIds_ShouldReturnListOfUserDTOs() {
    List<Long> ids = List.of(1L, 2L, 3L);

    User user1 = getUser(1L, "Ivan", "Ivanov", LocalDate.of(1981, 3, 12), "ivan@example.com");
    User user2 = getUser(2L, "Petr", "Ilir", LocalDate.of(1996, 5, 10), "petr@example.com");

    UserDTO dto1 = getUserDTO("Ivan", "Ivanov", BIRTH_DATE, "ivan@example.com");
    UserDTO dto2 = getUserDTO("Petr", "Ilir", BIRTH_DATE, "petr@example.com");

    when(userRepository.findByIdIn(ids)).thenReturn(List.of(user1, user2));
    when(userMapper.toUserDTO(user1)).thenReturn(dto1);
    when(userMapper.toUserDTO(user2)).thenReturn(dto2);

    List<UserDTO> result = userService.getUsersByIds(ids);

    assertNotNull(result);
    assertEquals(2, result.size());
    verify(userRepository).findByIdIn(ids);
    verify(userMapper, times(2)).toUserDTO(any(User.class));
  }

  @Test
  void getUsersByIds_ShouldThrowUserNotFoundException_WhenNoUsersFound() {
    List<Long> ids = List.of(1L, 2L, 3L);
    when(userRepository.findByIdIn(ids)).thenReturn(List.of());

    assertThrows(UserNotFoundException.class, () -> userService.getUsersByIds(ids));
    verify(userRepository).findByIdIn(ids);
    verify(userMapper, never()).toUserDTO(any());
  }

  @Test
  void getUserByEmail_ShouldReturnUserDTO() {
    User user = getUser(1L, NAME, SURNAME, BIRTH_DATE, EMAIL);

    UserDTO expectedDTO = getUserDTO(NAME, SURNAME, BIRTH_DATE, EMAIL);
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    when(userMapper.toUserDTO(user)).thenReturn(expectedDTO);

    UserDTO result = userService.getUserByEmail(EMAIL);

    assertNotNull(result);
    assertEquals(EMAIL, result.email());
    verify(userRepository).findByEmail(EMAIL);
    verify(userMapper).toUserDTO(user);
  }

  @Test
  void getUserByEmail_ShouldThrowUserNotFoundException() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(EMAIL));
    verify(userRepository).findByEmail(EMAIL);
    verify(userMapper, never()).toUserDTO(any());
  }

  @Test
  void updateUser_ShouldUpdateUserSuccessfully() {
    UpdateUserRequest request = new UpdateUserRequest("NewName", "NewSurname", "new@example.com");

    User user = getUser(1L, NAME, SURNAME, BIRTH_DATE, EMAIL);

    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.<User>getArgument(0));

    when(userMapper.toUserDTO(any(User.class))).thenAnswer(invocation -> {
      User userToMap = invocation.getArgument(0);
      return new UserDTO(userToMap.getName(), userToMap.getSurname(),
          userToMap.getBirthDate(), userToMap.getEmail());
    });

    UserDTO result = userService.updateUser(USER_ID, request);

    assertNotNull(result);
    assertEquals("NewName", result.name());
    assertEquals("new@example.com", result.email());
    assertEquals("NewSurname", result.surname());

    assertEquals("NewName", user.getName());
    assertEquals("NewSurname", user.getSurname());
    assertEquals("new@example.com", user.getEmail());

    verify(userRepository).findById(USER_ID);
    verify(userRepository).save(user);
    verify(userMapper).toUserDTO(user);
  }

  @Test
  void updateUser_ShouldEvictEmailCache_WhenEmailChanged() {
    UpdateUserRequest request = new UpdateUserRequest(null, null, "new@example.com");

    User existingUser = getUser(USER_ID, NAME, SURNAME, BIRTH_DATE, EMAIL);
    UserDTO expectedDTO = getUserDTO("NewName", "NewSurname", BIRTH_DATE, "new@example.com");

    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(User.class))).thenReturn(existingUser);
    when(userMapper.toUserDTO(any(User.class))).thenReturn(expectedDTO);

    userService.updateUser(USER_ID, request);

    verify(userRepository).findById(USER_ID);
    verify(userRepository).save(existingUser);
  }

  @Test
  void updateUser_ShouldThrowUserNotFoundException() {
    UpdateUserRequest request = new UpdateUserRequest("NewName", null, null);

    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.updateUser(USER_ID, request));
    verify(userRepository).findById(USER_ID);
    verify(userRepository, never()).save(any());
  }

  @Test
  void deleteUser_ShouldDeleteUserSuccessfully() {
    User user = getUser(USER_ID, NAME, SURNAME, BIRTH_DATE, EMAIL);

    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    doNothing().when(userRepository).delete(user);

    userService.deleteUser(USER_ID);

    verify(userRepository).findById(USER_ID);
    verify(userRepository).delete(user);
  }

  @Test
  void deleteUser_ShouldThrowException_WhenUserNotFound() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> userService.deleteUser(USER_ID));
    verify(userRepository).findById(USER_ID);
    verify(userRepository, never()).delete(any());
  }
}