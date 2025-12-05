package com.marketplace.member.command;

import com.marketplace.member.entity.Member;
import com.marketplace.member.model.RegisterRequest;
import com.marketplace.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterMemberCommandTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterMemberCommand registerMemberCommand;

    @Test
    void execute_shouldRegisterMember_whenRequestIsValid() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password");
        request.setEmail("test@example.com");

        when(memberRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = registerMemberCommand.execute(request);

        assertEquals("User registered successfully", result);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void execute_shouldThrowException_whenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");

        when(memberRepository.existsByUsername(request.getUsername())).thenReturn(true);

        com.marketplace.member.exception.UsernameAlreadyExistsException exception = assertThrows(
                com.marketplace.member.exception.UsernameAlreadyExistsException.class,
                () -> registerMemberCommand.execute(request));
        assertEquals("Username already exists", exception.getMessage());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void execute_shouldThrowException_whenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");

        when(memberRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(true);

        com.marketplace.member.exception.EmailAlreadyExistsException exception = assertThrows(
                com.marketplace.member.exception.EmailAlreadyExistsException.class,
                () -> registerMemberCommand.execute(request));
        assertEquals("Email already exists", exception.getMessage());
        verify(memberRepository, never()).save(any(Member.class));
    }
}