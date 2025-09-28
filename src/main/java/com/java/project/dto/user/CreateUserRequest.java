package com.java.project.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Name is required")
  private String surname;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;

  @Past(message = "Birth date must be in the past")
  private LocalDate birthDate;
}
