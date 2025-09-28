package com.java.project.dto.card;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardDTO {
  @NotBlank
  private String number;

  @NotBlank
  private String holder;

  private LocalDate expirationDate;
}
