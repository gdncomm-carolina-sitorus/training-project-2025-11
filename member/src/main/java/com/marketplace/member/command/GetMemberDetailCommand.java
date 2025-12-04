package com.marketplace.member.command;

import com.marketplace.member.entity.Member;
import com.marketplace.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetMemberDetailCommand implements Command<Member, Long> {

  @Autowired
  private MemberRepository memberRepository;

  @Override
  public Member execute(Long id) {
    return memberRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Product not found"));
  }
}
