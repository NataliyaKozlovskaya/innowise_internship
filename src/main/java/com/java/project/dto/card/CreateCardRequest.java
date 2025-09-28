package com.java.project.dto.card;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCardRequest {

  @NotBlank(message = "Card number is required")
  private String number;

  @NotBlank(message = "Holder is required")
  private String holder;

  private LocalDate expirationDate;
}
