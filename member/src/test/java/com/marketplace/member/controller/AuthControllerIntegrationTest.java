package com.marketplace.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.member.command.GetMemberDetailCommand;
import com.marketplace.member.command.LoginMemberCommand;
import com.marketplace.member.command.RegisterMemberCommand;
import com.marketplace.member.exception.*;
import com.marketplace.member.model.ApiResponse;
import com.marketplace.member.model.LoginRequest;
import com.marketplace.member.model.RegisterRequest;
import com.marketplace.member.model.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private RegisterMemberCommand registerMemberCommand;

  @MockitoBean
  private LoginMemberCommand loginMemberCommand;

  @MockitoBean
  private GetMemberDetailCommand getMemberDetailCommand;

  @Test
  void testRegister_WithEmptyFields_ShouldReturnValidationError() throws Exception {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("");
    request.setEmail("");
    request.setPassword("");

    mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.message", is("Validation failed")))
        .andExpect(jsonPath("$.errors.username", is("Username must not be blank")))
        .andExpect(jsonPath("$.errors.email", is("Email must not be blank")))
        .andExpect(jsonPath("$.errors.password", is("Password must not be blank")));
  }

  @Test
  void testRegister_UsernameExists_ShouldReturnError() throws Exception {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("user");
    request.setEmail("user@mail.com");
    request.setPassword("password");

    doThrow(new UsernameAlreadyExistsException("Username already exists")).when(
        registerMemberCommand).execute(request);

    mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.message", is("Username already exists")));
  }

  @Test
  void testRegister_EmailExists_ShouldReturnError() throws Exception {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("user");
    request.setEmail("user@mail.com");
    request.setPassword("password");

    doThrow(new EmailAlreadyExistsException("Email already exists")).when(registerMemberCommand)
        .execute(request);

    mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.message", is("Email already exists")));
  }

  @Test
  void testRegister_Success() throws Exception {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("user");
    request.setEmail("user@mail.com");
    request.setPassword("password");

    mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success", is(true)))
        .andExpect(jsonPath("$.message", is("User registered successfully")));
  }

  @Test
  void testLogin_Success() throws Exception {
    LoginRequest request = new LoginRequest();
    request.setUsername("user");
    request.setPassword("password");

    UserResponse userResponse = new UserResponse();
    userResponse.setId(1L);
    userResponse.setUsername("user");

    ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
        .success(true)
        .message("Login successful")
        .data(userResponse)
        .build();

    when(loginMemberCommand.execute(any(LoginRequest.class))).thenReturn(apiResponse);

    // Act & Assert
    mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Login successful"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.username").value("user"));
  }

  @Test
  void testLogin_UserNotFound() throws Exception {
    LoginRequest request = new LoginRequest();
    request.setUsername("unknown");
    request.setPassword("password");

    when(loginMemberCommand.execute(any(LoginRequest.class))).thenThrow(new UserNotFoundException(
        "unknown"));

    mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("User not found with username: unknown"))
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  @Test
  void testLogin_InvalidPassword() throws Exception {
    LoginRequest request = new LoginRequest();
    request.setUsername("user");
    request.setPassword("wrongpassword");

    when(loginMemberCommand.execute(any(LoginRequest.class))).thenThrow(new InvalidPasswordException());

    mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid password"))
        .andExpect(jsonPath("$.data").doesNotExist());
  }
}