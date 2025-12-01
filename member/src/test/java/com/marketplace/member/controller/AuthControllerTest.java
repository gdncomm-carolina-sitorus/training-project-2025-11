package com.marketplace.member.controller;

import com.marketplace.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  MockMvc mockMvc;

  @BeforeEach
  @AfterEach
  void cleanDatabase() {
    memberRepository.deleteAll();
  }

  @Test
  void registerMember_success() throws Exception {
    String requestJson = """
        {
          "username": "john_doe",
          "email": "john@example.com",
          "password": "securepassword"
        }
        """;
    mockMvc.perform(post("/api/members/register").contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("User created"));
  }

  @Test
  void registerMember_duplicateUsername() throws Exception {
    // First registration
    String requestJson = """
        {
          "username": "john_doe",
          "email": "john@example.com",
          "password": "securepassword"
        }
        """;
    mockMvc.perform(post("/api/members/register").contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("User created"));

    // Second registration with same username
    String requestJson2 = """
        {
          "username": "john_doe",
          "email": "john@example.com",
          "password": "securepassword"
        }
        """;
    mockMvc.perform(post("/api/members/register").contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson2))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Username already exists"));
  }

  @Test
  void registerMember_duplicateEmail() throws Exception {
    // First registration
    String requestJson = """
        {
          "username": "user1",
          "email": "user@mail.com",
          "password": "securepassword"
        }
        """;
    mockMvc.perform(post("/api/members/register").contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("User created"));

    // Second registration with same email
    String requestJson2 = """
        {
          "username": "user2",
          "email": "user@mail.com",
          "password": "securepassword"
        }
        """;
    mockMvc.perform(post("/api/members/register").contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestJson2))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Email already exists"));
  }

  @Test
  void registerMember_validationError() throws Exception {
    String requestJson = """
        {
          "username": "",
          "email": "invalid-email",
          "password": ""
        }
        """;
    mockMvc.perform(post("/api/members/register").contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errors.username").value("Username must not be blank"))
        .andExpect(jsonPath("$.errors.email").value("Email should be valid"))
        .andExpect(jsonPath("$.errors.password").value("Password must not be blank"));
  }
}