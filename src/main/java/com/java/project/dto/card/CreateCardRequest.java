package com.java.project.dto.card;

import com.java.project.entity.User;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCardRequest {

  private String number;
  private String holder;
  private User user;
  private LocalDate expirationDate;
}
