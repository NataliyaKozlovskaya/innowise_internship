package com.java.project.dto.user;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
  private String name;
  private String surname;
  private LocalDate birthDate;
  private String email;
}
