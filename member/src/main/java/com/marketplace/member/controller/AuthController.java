package com.marketplace.member.controller;

import com.marketplace.member.command.RegisterMemberCommand;
import com.marketplace.member.model.RegisterRequest;
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
@RequestMapping("/api/members")
public class AuthController {
  @Autowired
  private RegisterMemberCommand registerMemberCommand;

  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
    registerMemberCommand.execute(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of("success", true, "message", "User created"));
  }
}
