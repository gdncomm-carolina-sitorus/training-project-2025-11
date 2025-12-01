package com.marketplace.member.model;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class RegisterRequest {
  @NotBlank(message = "Username must not be blank")
  private String username;

  @NotBlank(message = "Password must not be blank")
  private String password;

  @NotBlank(message = "Email must not be blank")
  @Email(message = "Email should be valid")
  private String email;
}
