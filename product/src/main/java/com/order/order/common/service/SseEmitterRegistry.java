package com.order.order.common.service;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {

    // SseEmitter 는 연결된 사용자 정보(ip, macAddress 등)를 의미
    // ConcurrentHashMap 은 Thread-Safe 한 map (동시성 이슈 발생X)
    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public void addSseEmitter(String email, SseEmitter sseEmitter) {
        emitterMap.put(email, sseEmitter);
    }

    public void removeEmitter(String email) {
        emitterMap.remove(email);
    }

    public SseEmitter getEmitter(String email) {
        return emitterMap.get(email);
    }
}
