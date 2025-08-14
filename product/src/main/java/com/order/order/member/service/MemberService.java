package com.order.order.member.service;

import com.order.order.member.domain.Member;
import com.order.order.member.dto.CreateMemberDTO;
import com.order.order.member.dto.LoginReqDTO;
import com.order.order.member.dto.MemberResDTO;
import com.order.order.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional      // 영속성 컨텍스트 반영 시 해당 어노테이션 사용
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public Long save(CreateMemberDTO createMemberDTO) {
        if (memberRepository.findByEmail(createMemberDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        if (createMemberDTO.getPassword().length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상으로 입력해주세요.");
        }

        Member member = memberRepository.save(createMemberDTO.toEntity(passwordEncoder.encode(createMemberDTO.getPassword())));
        return member.getId();
    }

    // 로그인
    public Member doLogin(LoginReqDTO loginReqDTO) {
        Optional<Member> optionalMember = memberRepository.findByEmail(loginReqDTO.getEmail());

        boolean check = true;
        if (optionalMember.isPresent()) {
            if (!passwordEncoder.matches(loginReqDTO.getPassword(), optionalMember.get().getPassword())) {
                // 비밀번호 불일치
                check = false;
            }
        } else {
            check = false;
        }

        if (!check) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        return optionalMember.get();
    }

    // 회원 목록 조회
    public List<MemberResDTO> findAll() {
        return memberRepository.findAll().stream().map(MemberResDTO::fromEntity).collect(Collectors.toList());
    }


    // 마이페이지
    public MemberResDTO myInfo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member is not found"));
        return MemberResDTO.fromEntity(member);
    }

    // 회원 탈퇴
    public void updateDelYn() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member is not found"));
        log.info("member = {}", member);
        member.updateDelYn("Y");
        log.info("member = {}", member);
    }
    
    // 회원 상세 조회
    public MemberResDTO detail(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("member is not found"));
        return MemberResDTO.fromEntity(member);
    }

}
