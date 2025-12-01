package com.marketplace.member.repository;

import com.marketplace.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}
