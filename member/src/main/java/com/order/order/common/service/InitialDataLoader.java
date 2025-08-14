package com.order.order.common.service;

import com.order.order.member.domain.Member;
import com.order.order.member.domain.Role;
import com.order.order.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (memberRepository.findByEmail("admin@email.com").isPresent()) {
            return;
        }

        Member member = Member.builder()
                .email("admin@email.com")
                .password(passwordEncoder.encode("112233445566"))
                .role(Role.ADMIN)
                .build();

        memberRepository.save(member);
    }
}
