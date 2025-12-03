package com.marketplace.member.command;

import com.marketplace.member.entity.Member;
import com.marketplace.member.model.ApiResponse;
import com.marketplace.member.model.LoginRequest;
import com.marketplace.member.model.UserResponse;
import com.marketplace.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class LoginMemberCommand implements Command<ApiResponse<UserResponse>, LoginRequest> {

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Override
  public ApiResponse<UserResponse> execute(LoginRequest request) {
    Member member = memberRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new RuntimeException("Invalid password");
    }

    UserResponse user = new UserResponse(member.getId(), member.getUsername());

    return ApiResponse.<UserResponse>builder()
        .success(true)
        .message("Login success")
        .data(user)
        .build();
  }
}
