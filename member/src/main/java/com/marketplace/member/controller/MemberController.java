package com.marketplace.member.controller;

import com.marketplace.member.command.GetMemberDetailCommand;
import com.marketplace.member.command.LoginMemberCommand;
import com.marketplace.member.command.RegisterMemberCommand;
import com.marketplace.member.entity.Member;
import com.marketplace.member.model.ApiResponse;
import com.marketplace.member.model.LoginRequest;
import com.marketplace.member.model.RegisterRequest;
import com.marketplace.member.model.UserResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberController {

  @Autowired
  GetMemberDetailCommand getMemberDetailCommand;

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<Member>> getMember(@PathVariable Long id) {
    Member member = getMemberDetailCommand.execute(id);
    return ResponseEntity.ok(ApiResponse.<Member>builder()
        .success(true)
        .message("Product retrieved successfully")
        .data(member)
        .build());
  }
}
