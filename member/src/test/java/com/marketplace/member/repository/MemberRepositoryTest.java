package com.marketplace.member.repository;

import com.marketplace.member.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private Member member1;

    @BeforeEach
    void setUp() {
        // Clear repository before each test
        memberRepository.deleteAll();

        // Prepare a sample member
        member1 = new Member();
        member1.setUsername("user1");
        member1.setEmail("user1@example.com");
        member1.setPassword("password123");

        memberRepository.save(member1);
    }

    @Test
    void testFindByUsername_Found() {
        Optional<Member> result = memberRepository.findByUsername("user1");

        assertTrue(result.isPresent());
        assertEquals(member1.getUsername(), result.get().getUsername());
        assertEquals(member1.getEmail(), result.get().getEmail());
    }

    @Test
    void testFindByUsername_NotFound() {
        Optional<Member> result = memberRepository.findByUsername("nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    void testExistsByUsername() {
        assertTrue(memberRepository.existsByUsername("user1"));
        assertFalse(memberRepository.existsByUsername("nonexistent"));
    }

    @Test
    void testExistsByEmail() {
        assertTrue(memberRepository.existsByEmail("user1@example.com"));
        assertFalse(memberRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void testSave_NewMember() {
        Member member2 = new Member();
        member2.setUsername("user2");
        member2.setEmail("user2@example.com");
        member2.setPassword("password456");

        Member saved = memberRepository.save(member2);

        assertNotNull(saved.getId());
        assertEquals("user2", saved.getUsername());
        assertEquals("user2@example.com", saved.getEmail());
    }
}
