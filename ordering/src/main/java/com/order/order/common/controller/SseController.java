package com.order.order.common.controller;

import com.order.order.common.service.SseAlarmService;
import com.order.order.common.service.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController {

    private final SseEmitterRegistry sseEmitterRegistry;

    // SseEmitter 객체를 return 에서 연결이 끊기지 않고 계속 알림을 보낼 수 있도록 함
    @GetMapping("/connect")
    public SseEmitter subscribe() {
        // sseEmitter 에 연결 요청이 들어온 사용자의 정보가 자동으로 세팅되어 있음
        SseEmitter sseEmitter = new SseEmitter(14400 * 60 * 1000L);           // 10일 정도 emitter 유효 기간 설정
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        sseEmitterRegistry.addSseEmitter(email, sseEmitter);
        try {
            sseEmitter.send(SseEmitter.event().name("connect").data("연결 완료"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sseEmitter;
    }


    @GetMapping("/disconnect")
    public void unSubscribe() {
        // key 를 매개변수로 넘겨 삭제
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        sseEmitterRegistry.removeEmitter(email);
    }
}
