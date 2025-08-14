package com.order.order.member.controller;

import com.order.order.common.auth.JwtTokenProvider;
import com.order.order.common.dto.CommonDTO;
import com.order.order.member.domain.Member;
import com.order.order.member.dto.*;
import com.order.order.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody @Valid CreateMemberDTO createMemberDTO) {

        // 방금 가입된 회원의 id 반환
        Long id = memberService.save(createMemberDTO);
        return new ResponseEntity<>(CommonDTO.builder()
                .result(id)
                .status_code(HttpStatus.CREATED.value())
                .status_message("회원가입 완료")
                .build()
                , HttpStatus.CREATED);
    }
    
    
    // 로그인
    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody LoginReqDTO loginReqDTO) {
        Member member = memberService.doLogin(loginReqDTO);

        // at 토큰 생성
        String accessToken = jwtTokenProvider.createAtToken(member);
        
        // rt 토큰 생성과 동시에 db에 저장(redis)
        String refreshToken = jwtTokenProvider.createRtToken(member);
        
        LoginResDTO loginResDTO = LoginResDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return new ResponseEntity<>(CommonDTO.builder()
                .result(loginResDTO)
                .status_code(HttpStatus.OK.value())
                .status_message("로그인 성공").build(), HttpStatus.OK);

    }
    
    // rt를 통한 at 갱신 요청
    @PostMapping("/refresh-at")
    public ResponseEntity<?> generateNewAt(@RequestBody RefreshTokenDTO refreshTokenDTO) {
        // rt 검증 로직
        // refresh 검증 + db의 값과 비교
        Member member = jwtTokenProvider.validateRt(refreshTokenDTO.getRefreshToken());
        
        // at 신규 생성 로직
        // refresh 토큰 안에 있는 email로 member 객체 찾기
        String accessToken = jwtTokenProvider.createAtToken(member);

        LoginResDTO loginResDTO = LoginResDTO.builder()
                .accessToken(accessToken)
                .build();

        return new ResponseEntity<>(CommonDTO.builder()
                .result(loginResDTO)
                .status_code(HttpStatus.OK.value())
                .status_message("accessToken 갱신 성공").build(), HttpStatus.OK);
    }

    // 회원 목록 조회 - admin 권한
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAll() {
        List<MemberResDTO> memberResDTOList = memberService.findAll();
        return new ResponseEntity<>(CommonDTO.builder()
                .result(memberResDTOList)
                .status_code(HttpStatus.OK.value())
                .status_message("회원 목록 조회 성공").build(), HttpStatus.OK);
    }
    
    // 마이페이지
    @GetMapping("/myPage")
    public ResponseEntity<?> myPage() {
        return new ResponseEntity<>(CommonDTO.builder()
                .result(memberService.myInfo())
                .status_code(HttpStatus.OK.value())
                .status_message("마이페이지 조회 성공").build(), HttpStatus.OK);
    }

    // 회원 탈퇴
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete() {
        memberService.updateDelYn();
        return new ResponseEntity<>(CommonDTO.builder()
                .result("OK")
                .status_code(HttpStatus.OK.value())
                .status_message("회원 탈퇴 완료")
                .build(), HttpStatus.OK);
    }

    // 회원 상세 조회
    @GetMapping("/detail/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> detail(@PathVariable Long memberId) {
        MemberResDTO memberResDTO = memberService.detail(memberId);
        return new ResponseEntity<>(CommonDTO.builder()
                .result(memberResDTO)
                .status_code(HttpStatus.OK.value())
                .status_message("회원 상세 조회 완료")
                .build(), HttpStatus.OK);
    }
}
