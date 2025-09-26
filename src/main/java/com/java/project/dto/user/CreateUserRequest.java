package com.java.project.dto.user;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
  private String name;
  private String surname;
  private String email;
  private LocalDate birthDate;
}
