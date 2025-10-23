package com.innowise.user.service.impl;


import com.innowise.user.dto.card.CardDTO;
import com.innowise.user.dto.user.CreateUserRequest;
import com.innowise.user.dto.user.UpdateUserRequest;
import com.innowise.user.dto.user.UserCreateResponse;
import com.innowise.user.dto.user.UserDTO;
import com.innowise.user.dto.user.UserWithCardDTO;
import com.innowise.user.entity.Card;
import com.innowise.user.entity.User;
import com.innowise.user.exception.EmailAlreadyExistsException;
import com.innowise.user.exception.UserNotFoundException;
import com.innowise.user.mapper.CardMapper;
import com.innowise.user.mapper.UserMapper;
import com.innowise.user.repository.CardRepository;
import com.innowise.user.repository.UserRepository;
import com.innowise.user.service.UserService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "users")
public class UserServiceImpl implements UserService {

  private static final String USER_NOT_FOUND = "User not found with id: ";
  private final UserRepository userRepository;
  private final CardRepository cardRepository;
  private final UserMapper userMapper;
  private final CardMapper cardMapper;

  @Transactional
  @Override
  public UserCreateResponse createUser(CreateUserRequest request) {
    userRepository.findByEmail(request.email()).ifPresent(user -> {
      throw new EmailAlreadyExistsException("Email already exists: " + request.email());
    });

    User user = new User();

    user.setUuid(request.uuid());
    user.setName(request.name());
    user.setSurname(request.surname());
    user.setBirthDate(request.birthDate());
    user.setEmail(request.email());
    User savedUser = userRepository.save(user);

    return userMapper.toUserCreateResponse(savedUser);
  }

  @Transactional(readOnly = true)
//  @Cacheable(key = "#id", unless = "#result == null")
  @Override
  public UserCreateResponse getUserById(String id) {
    return userRepository.findById(id)
        .map(userMapper::toUserCreateResponse)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + id));
  }

  @Transactional(readOnly = true)
  @Override
  public User getUserEntityById(String id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + id));
  }

  @Transactional(readOnly = true)
  @Override
  public List<UserDTO> getUsersByIds(List<String> ids) {

    List<UserDTO> result = userRepository.findByIdsIn(ids).stream()
        .map(userMapper::toUserDTO)
        .toList();

    if (result.isEmpty()) {
      throw new UserNotFoundException("Users not found with ids: " + ids);
    }
    return result;
  }

  @Transactional(readOnly = true)
//  @Cacheable(key = "'email:' + #email", unless = "#result == null")
  @Override
  public UserCreateResponse getUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .map(userMapper::toUserCreateResponse)
        .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
  }

  @Transactional
  //@CachePut(key = "#id")
  @Override
  public UserDTO updateUser(String id, UpdateUserRequest request) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + id));

    if(userRepository.findByEmail(request.email()).isPresent()){
      log.error("User can`t be updated, email {} already exist ", request.email());
      throw new EmailAlreadyExistsException("User can`t be updated, email already exist");
    }

    if (request.email() != null && !request.email().equals(user.getEmail())) {
      evictEmailCache(user.getEmail());
    }

    Optional.ofNullable(request.name()).ifPresent(user::setName);
    Optional.ofNullable(request.surname()).ifPresent(user::setSurname);
    Optional.ofNullable(request.email()).ifPresent(user::setEmail);

    User updatedUser = userRepository.save(user);
    return userMapper.toUserDTO(updatedUser);
  }

  @Transactional
  @CacheEvict(key = "#id")
  @Override
  public UserWithCardDTO deleteUser(String id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + id));

    List<Card> cardList = cardRepository.findAllByUserUuid(id);
    List<CardDTO> cardDTOList = cardList.stream().map(cardMapper::toCardDTO).toList();

    evictEmailCache(user.getEmail());
    userRepository.delete(user);

    return new UserWithCardDTO(user.getName(), user.getSurname(), user.getBirthDate(), user.getEmail(), cardDTOList);
  }

  /**
   * Invalidate the old cache via email
   */
  @CacheEvict(value = "users", key = "'email:' + #email")
  public void evictEmailCache(String email) {
    log.debug("Invalidated cache for email: {}", email);
  }
}
