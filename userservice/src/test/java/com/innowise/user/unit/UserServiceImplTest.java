//package com.innowise.user.unit;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.innowise.user.dto.user.CreateUserRequest;
//import com.innowise.user.dto.user.UpdateUserRequest;
//import com.innowise.user.dto.user.UserCreateResponse;
//import com.innowise.user.entity.Card;
//import com.innowise.user.entity.User;
//import com.innowise.user.exception.EmailAlreadyExistsException;
//import com.innowise.user.exception.UserNotFoundException;
//import com.innowise.user.mapper.CardMapper;
//import com.innowise.user.mapper.UserMapper;
//import com.innowise.user.repository.CardRepository;
//import com.innowise.user.repository.UserRepository;
//import com.innowise.user.service.impl.UserServiceImpl;
//import com.innowise.user.util.TestDataFactory;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//class UserServiceImplTest {
//
//  private final UUID USER_ID = UUID.randomUUID();
//  private final String EMAIL = "test@example.com";
//  private final String NAME = "Mark";
//  private final String SURNAME = "Staf";
//  private final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
//
//  @Mock
//  private UserRepository userRepository;
//  @Mock
//  private CardRepository cardRepository;
//  @Mock
//  private UserMapper userMapper;
//  @Mock
//  private CardMapper cardMapper;
//  @InjectMocks
//  private UserServiceImpl userService;
//
//  @Test
//  void createUser_ShouldCreateUserSuccessfully() {
//    CreateUserRequest request = TestDataFactory.getCreateUserRequest(USER_ID.toString(), NAME,
//        SURNAME, BIRTH_DATE, EMAIL);
//    User savedUser = TestDataFactory.getUser(USER_ID, NAME, SURNAME, BIRTH_DATE, EMAIL);
//    UserCreateResponse expectedDTO = TestDataFactory.getUserDTO(USER_ID.toString(),NAME, SURNAME, BIRTH_DATE, EMAIL);
//
//    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
//    when(userRepository.save(any(User.class))).thenReturn(savedUser);
//    when(userMapper.toUserCreateResponse(savedUser)).thenReturn(expectedDTO);
//
//    UserCreateResponse result = userService.createUser(request);
//
//    assertNotNull(result);
//    assertEquals(expectedDTO.email(), result.email());
//    verify(userRepository).findByEmail(EMAIL);
//    verify(userRepository).save(any(User.class));
//    verify(userMapper).toUserCreateResponse(savedUser);
//  }
//
//  @Test
//  void createUser_ShouldThrowEmailAlreadyExistsException() {
//    CreateUserRequest request = TestDataFactory.getCreateUserRequest(USER_ID.toString(), NAME,
//        SURNAME, BIRTH_DATE, EMAIL);
//    User existingUser = TestDataFactory.getUser(USER_ID, NAME, SURNAME, BIRTH_DATE, EMAIL);
//
//    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
//
//    assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(request));
//    verify(userRepository).findByEmail(EMAIL);
//    verify(userRepository, never()).save(any(User.class));
//  }
//
//  @Test
//  void getUserById_ShouldReturnUserDTO() {
//    User user = TestDataFactory.getUser(USER_ID, NAME, SURNAME, BIRTH_DATE, EMAIL);
//    UserCreateResponse expectedDTO = TestDataFactory.getUserDTO(USER_ID.toString(),NAME, SURNAME, BIRTH_DATE, EMAIL);
//
//    when(userRepository.findById("1")).thenReturn(Optional.of(user));
//    when(userMapper.toUserCreateResponse(user)).thenReturn(expectedDTO);
//
//    UserCreateResponse result = userService.getUserById("1");
//
//    assertNotNull(result);
//    verify(userRepository).findById("1");
//    verify(userMapper).toUserCreateResponse(user);
//  }
//
//  @Test
//  void getUserById_ShouldThrowUserNotFoundException() {
//    when(userRepository.findById("1")).thenReturn(Optional.empty());
//
//    assertThrows(UserNotFoundException.class, () -> userService.getUserById("1"));
//    verify(userRepository).findById("1");
//    verify(userMapper, never()).toUserCreateResponse(any());
//  }
//
//  @Test
//  void getUsersByIds_ShouldReturnListOfUserDTOs() {
//    List<String> ids = List.of("1", "2", "3");
//    User user1 = TestDataFactory.getUser(USER_ID, "Ivan", "Ivanov", LocalDate.of(1981, 3, 12),
//        "ivan@example.com");
//    User user2 = TestDataFactory.getUser(USER_ID, "Petr", "Ilir", LocalDate.of(1996, 5, 10),
//        "petr@example.com");
//
//    UserCreateResponse dto1 = TestDataFactory.getUserDTO(USER_ID.toString(),"Ivan", "Ivanov", BIRTH_DATE, "ivan@example.com");
//    UserCreateResponse dto2 = TestDataFactory.getUserDTO(USER_ID.toString(),"Petr", "Ilir", BIRTH_DATE, "petr@example.com");
//
//    when(userRepository.findByIdsIn(ids)).thenReturn(List.of(user1, user2));
//    when(userMapper.toUserCreateResponse(user1)).thenReturn(dto1);
//    when(userMapper.toUserCreateResponse(user2)).thenReturn(dto2);
//
//    List<UserCreateResponse> result = userService.getUsersByIds(ids);
//
//    assertNotNull(result);
//    assertEquals(2, result.size());
//    verify(userRepository).findByIdsIn((ids));
//    verify(userMapper, times(2)).toUserCreateResponse(any(User.class));
//  }
//
//  @Test
//  void getUsersByIds_ShouldThrowUserNotFoundException_WhenNoUsersFound() {
//    List<String> ids = List.of("1", "2", "3");
//
//    when(userRepository.findByIdsIn(ids)).thenReturn(List.of());
//
//    assertThrows(UserNotFoundException.class, () -> userService.getUsersByIds(ids));
//    verify(userRepository).findByIdsIn(ids);
//    verify(userMapper, never()).toUserCreateResponse(any());
//  }
//
//  @Test
//  void getUserByEmail_ShouldReturnUserDTO() {
//    User user = TestDataFactory.getUser(USER_ID, NAME, SURNAME, BIRTH_DATE, EMAIL);
//
//    UserCreateResponse expectedDTO = TestDataFactory.getUserDTO(USER_ID.toString(), NAME, SURNAME, BIRTH_DATE, EMAIL);
//    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
//    when(userMapper.toUserCreateResponse(user)).thenReturn(expectedDTO);
//
//    UserCreateResponse result = userService.getUserByEmail(EMAIL);
//
//    assertNotNull(result);
//    assertEquals(EMAIL, result.email());
//    verify(userRepository).findByEmail(EMAIL);
//    verify(userMapper).toUserCreateResponse(user);
//  }
//
//  @Test
//  void getUserByEmail_ShouldThrowUserNotFoundException() {
//    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
//
//    assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(EMAIL));
//    verify(userRepository).findByEmail(EMAIL);
//    verify(userMapper, never()).toUserCreateResponse(any());
//  }
//
//  @Test
//  void updateUser_ShouldUpdateUserSuccessfully() {
//    UpdateUserRequest request = new UpdateUserRequest("NewName", "NewSurname", "new@example.com");
//
//    User user = TestDataFactory.getUser(USER_ID, NAME, SURNAME, BIRTH_DATE, EMAIL);
//
//    when(userRepository.findById("1")).thenReturn(Optional.of(user));
//
//    when(userRepository.save(any(User.class))).thenAnswer(
//        invocation -> invocation.<User>getArgument(0));
//
//    when(userMapper.toUserCreateResponse(any(User.class))).thenAnswer(invocation -> {
//      User userToMap = invocation.getArgument(0);
//      return new UserCreateResponse(userToMap.getUuid(), userToMap.getName(), userToMap.getSurname(),
//          userToMap.getBirthDate(), userToMap.getEmail());
//    });
//
//    UserCreateResponse result = userService.updateUser("1", request);
//
//    assertNotNull(result);
//    assertEquals("NewName", result.name());
//    assertEquals("new@example.com", result.email());
//    assertEquals("NewSurname", result.surname());
//    verify(userRepository).findById("1");
//    verify(userRepository).save(user);
//    verify(userMapper).toUserCreateResponse(user);
//  }
//
//  @Test
//  void updateUser_ShouldEvictEmailCache_WhenEmailChanged() {
//    UpdateUserRequest request = new UpdateUserRequest(null, null, "new@example.com");
//    User existingUser = TestDataFactory.getUser(USER_ID, NAME, SURNAME, BIRTH_DATE, EMAIL);
//    UserCreateResponse expectedDTO = TestDataFactory.getUserDTO(USER_ID.toString(),"NewName", "NewSurname", BIRTH_DATE,
//        "new@example.com");
//
//    when(userRepository.findById("1")).thenReturn(Optional.of(existingUser));
//    when(userRepository.save(any(User.class))).thenReturn(existingUser);
//    when(userMapper.toUserCreateResponse(any(User.class))).thenReturn(expectedDTO);
//
//    userService.updateUser("1", request);
//
//    verify(userRepository).findById("1");
//    verify(userRepository).save(existingUser);
//  }
//
//  @Test
//  void updateUser_ShouldThrowUserNotFoundException() {
//    UpdateUserRequest request = new UpdateUserRequest("NewName", null, null);
//
//    when(userRepository.findById("1")).thenReturn(Optional.empty());
//
//    assertThrows(UserNotFoundException.class, () -> userService.updateUser("1", request));
//    verify(userRepository).findById("1");
//    verify(userRepository, never()).save(any());
//  }
//
//  @Test
//  void deleteUser_ShouldDeleteUserSuccessfully() {
//    User user = TestDataFactory.getUser(USER_ID, NAME, SURNAME, BIRTH_DATE, EMAIL);
//    List<Card> cardList = new ArrayList<>();
//    when(userRepository.findById("1")).thenReturn(Optional.of(user));
//    when(cardRepository.findAllByUserUuid("1")).thenReturn(cardList);
////    when(cardMapper.toCardDTO(new Card())).thenReturn(new CardDTO("1", "holder", LocalDate.now()));
//    doNothing().when(userRepository).delete(user);
//
//    userService.deleteUser("1");
//
//    verify(userRepository).findById("1");
//    verify(userRepository).delete(user);
//  }
//
//  @Test
//  void deleteUser_ShouldThrowException_WhenUserNotFound() {
//    when(userRepository.findById("1")).thenReturn(Optional.empty());
//
//    assertThrows(RuntimeException.class, () -> userService.deleteUser("1"));
//    verify(userRepository).findById("1");
//    verify(userRepository, never()).delete(any());
//  }
//}