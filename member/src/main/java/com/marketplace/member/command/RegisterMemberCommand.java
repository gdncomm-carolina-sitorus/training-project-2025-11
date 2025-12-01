package com.marketplace.member.command;

import com.marketplace.member.entity.Member;
import com.marketplace.member.model.RegisterRequest;
import com.marketplace.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RegisterMemberCommand implements Command<String, RegisterRequest> {
  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  public String execute(RegisterRequest request) {
    if (memberRepository.existsByUsername(request.getUsername())) {
      throw new RuntimeException("Username already exists");
    }
    if (memberRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("Email already exists");
    }
    Member member = new Member();
    member.setUsername(request.getUsername());
    member.setPassword(passwordEncoder.encode(request.getPassword()));
    member.setEmail(request.getEmail());

    memberRepository.save(member);
    return "User registered successfully";
  }
}
