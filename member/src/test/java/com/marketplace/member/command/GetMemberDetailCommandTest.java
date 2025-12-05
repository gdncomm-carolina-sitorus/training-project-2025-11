package com.marketplace.member.command;

import com.marketplace.member.entity.Member;
import com.marketplace.member.exception.MemberNotFoundException;
import com.marketplace.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetMemberDetailCommandTest {

  @Mock
  private MemberRepository memberRepository;

  @InjectMocks
  private GetMemberDetailCommand getMemberDetailCommand;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testExecute_MemberFound() {
    // Arrange
    Member member = new Member();
    member.setId(1L);
    member.setUsername("user1");
    member.setEmail("user1@example.com");

    when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

    // Act
    Member result = getMemberDetailCommand.execute(1L);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("user1", result.getUsername());
    assertEquals("user1@example.com", result.getEmail());

    verify(memberRepository, times(1)).findById(1L);
  }

  @Test
  void testExecute_MemberNotFound() {
    // Arrange
    when(memberRepository.findById(2L)).thenReturn(Optional.empty());

    // Act & Assert
    MemberNotFoundException exception = assertThrows(MemberNotFoundException.class,
        () -> getMemberDetailCommand.execute(2L));
    assertEquals("Member not found with id: 2", exception.getMessage());

    verify(memberRepository, times(1)).findById(2L);
  }
}
