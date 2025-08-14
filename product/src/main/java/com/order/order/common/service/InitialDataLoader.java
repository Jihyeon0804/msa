package com.order.order.common.service;

import com.order.order.member.domain.Member;
import com.order.order.member.domain.Role;
import com.order.order.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
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
