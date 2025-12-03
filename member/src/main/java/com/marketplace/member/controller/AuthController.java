package com.marketplace.member.controller;

import com.marketplace.member.command.LoginMemberCommand;
import com.marketplace.member.command.RegisterMemberCommand;
import com.marketplace.member.model.ApiResponse;
import com.marketplace.member.model.LoginRequest;
import com.marketplace.member.model.RegisterRequest;
import com.marketplace.member.model.UserResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {
  @Autowired
  private RegisterMemberCommand registerMemberCommand;

  @Autowired
  LoginMemberCommand loginMemberCommand;

  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
    registerMemberCommand.execute(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of("success", true, "message", "User created"));
  }

  @PostMapping("/login")
  public ApiResponse<UserResponse> login(@RequestBody LoginRequest request) {
    return loginMemberCommand.execute(request);
  }
}
