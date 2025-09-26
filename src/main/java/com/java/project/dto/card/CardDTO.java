package com.java.project.dto.card;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardDTO {

  private String number;
  private String holder;
  private LocalDate expirationDate;
}
