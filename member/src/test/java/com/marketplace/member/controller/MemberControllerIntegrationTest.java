package com.marketplace.member.controller;

import com.marketplace.member.command.GetMemberDetailCommand;
import com.marketplace.member.entity.Member;
import com.marketplace.member.exception.MemberNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
class MemberControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private GetMemberDetailCommand getMemberDetailCommand;

  @Test
  @WithMockUser(username = "testuser")
  void testGetMember_Success() throws Exception {
    Member member = new Member();
    member.setId(1L);
    member.setUsername("user1");
    member.setEmail("user1@example.com");

    when(getMemberDetailCommand.execute(1L)).thenReturn(member);

    mockMvc.perform(get("/api/members/{id}", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Member retrieved successfully"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.username").value("user1"))
        .andExpect(jsonPath("$.data.email").value("user1@example.com"));
  }


  @Test
  @WithMockUser(username = "testuser")
  void testGetMember_NotFound() throws Exception {
    when(getMemberDetailCommand.execute(99L)).thenThrow(new MemberNotFoundException(99L));

    mockMvc.perform(get("/api/members/{id}", 99L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Member not found with id: 99"))
        .andExpect(jsonPath("$.data").doesNotExist());
  }
}