package com.marketplace.member.command;

import com.marketplace.member.entity.Member;
import com.marketplace.member.exception.UserNotFoundException;
import com.marketplace.member.model.ApiResponse;
import com.marketplace.member.model.LoginRequest;
import com.marketplace.member.model.UserResponse;
import com.marketplace.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginMemberCommandTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private LoginMemberCommand loginMemberCommand;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testExecute_LoginSuccess() {
    LoginRequest request = new LoginRequest();
    request.setUsername("user1");
    request.setPassword("password123");

    Member member = new Member();
    member.setId(1L);
    member.setUsername("user1");
    member.setPassword("encodedPassword");

    when(memberRepository.findByUsername("user1")).thenReturn(Optional.of(member));
    when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

    ApiResponse<UserResponse> response = loginMemberCommand.execute(request);

    assertTrue(response.isSuccess());
    assertEquals("Login success", response.getMessage());
    assertNotNull(response.getData());
    assertEquals(1L, response.getData().getId());
    assertEquals("user1", response.getData().getUsername());

    verify(memberRepository, times(1)).findByUsername("user1");
    verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
  }

  @Test
  void testExecute_UserNotFound() {
    LoginRequest request = new LoginRequest();
    request.setUsername("nonexistent");
    request.setPassword("password123");

    when(memberRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
    UserNotFoundException exception =
        assertThrows(UserNotFoundException.class, () -> loginMemberCommand.execute(request));

    assertEquals("User not found with username: nonexistent", exception.getMessage());

    verify(memberRepository, times(1)).findByUsername("nonexistent");
    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }


  @Test
  void testExecute_InvalidPassword() {
    LoginRequest request = new LoginRequest();
    request.setUsername("user1");
    request.setPassword("wrongPassword");

    Member member = new Member();
    member.setId(1L);
    member.setUsername("user1");
    member.setPassword("encodedPassword");

    when(memberRepository.findByUsername("user1")).thenReturn(Optional.of(member));
    when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> loginMemberCommand.execute(request));
    assertEquals("Invalid password", exception.getMessage());

    verify(memberRepository, times(1)).findByUsername("user1");
    verify(passwordEncoder, times(1)).matches("wrongPassword", "encodedPassword");
  }
}
