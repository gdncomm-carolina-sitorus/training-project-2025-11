package com.marketplace.member.controller;

import com.marketplace.member.command.GetMemberDetailCommand;
import com.marketplace.member.command.LoginMemberCommand;
import com.marketplace.member.command.RegisterMemberCommand;
import com.marketplace.member.model.ApiResponse;
import com.marketplace.member.model.LoginRequest;
import com.marketplace.member.model.RegisterRequest;
import com.marketplace.member.model.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

  @Mock
  private RegisterMemberCommand registerMemberCommand;

  @Mock
  private LoginMemberCommand loginMemberCommand;

  @Mock
  private GetMemberDetailCommand getMemberDetailCommand;

  @InjectMocks
  private AuthController authController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testRegister_Success() {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("user1");
    request.setEmail("user1@example.com");
    request.setPassword("password123");

    when(registerMemberCommand.execute(request)).thenReturn("User registered successfully");
    ResponseEntity<Map<String, Object>> response = authController.register(request);

    assertNotNull(response);
    assertEquals(201, response.getStatusCodeValue());
    assertTrue((Boolean) response.getBody().get("success"));
    assertEquals("User registered successfully", response.getBody().get("message"));

    verify(registerMemberCommand, times(1)).execute(request);
  }


  @Test
  void testLogin_Success() {
    LoginRequest request = new LoginRequest();
    request.setUsername("user1");
    request.setPassword("password123");

    UserResponse userResponse = new UserResponse(1L, "user1");
    ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
        .success(true)
        .message("Login success")
        .data(userResponse)
        .build();

    when(loginMemberCommand.execute(request)).thenReturn(apiResponse);

    ApiResponse<UserResponse> response = authController.login(request);

    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertEquals("Login success", response.getMessage());
    assertEquals(1L, response.getData().getId());
    assertEquals("user1", response.getData().getUsername());

    verify(loginMemberCommand, times(1)).execute(request);
  }

  @Test
  void testLogin_Failure() {
    LoginRequest request = new LoginRequest();
    request.setUsername("user1");
    request.setPassword("wrongPassword");

    when(loginMemberCommand.execute(request)).thenThrow(new RuntimeException("Invalid password"));
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> authController.login(request));
    assertEquals("Invalid password", exception.getMessage());

    verify(loginMemberCommand, times(1)).execute(request);
  }
}
