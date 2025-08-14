package com.order.order.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.order.common.dto.SseMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Component
@Slf4j
public class SseAlarmService implements MessageListener {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final RedisTemplate<String, String> redisTemplate;

    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry
            , @Qualifier("ssePubSub") RedisTemplate<String, String> redisTemplate) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.redisTemplate = redisTemplate;
    }

    // 특정 사용자에게 메세지 발송
    // orderingId 매개변수는 이후 디벨롭할 때 보낼 메세지가 들어오도록 바꾸기 (지금은 어떤 상품이 주문이 들어왔는지만 보내도록 설계)
    public void publishMessage(String receiver, String sender, Long orderingId) {
        SseMessageDTO sseMessageDTO = SseMessageDTO.builder()
                .sender(sender)
                .receiver(receiver)
                .orderingId(orderingId).build();

        // 보낼 데이터를 json 형식으로 조립
        ObjectMapper objectMapper = new ObjectMapper();
        String data;
        try {
            data = objectMapper.writeValueAsString(sseMessageDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        // emitter 객체를 통해 메세지 전송
        // admin 객체가 disconnect 되면 sseEmitter가 null이 되어 NullPointerException 발생 -> 실시간 메세지 전송 불가
        // 그렇기 때문에 try-catch 에서 에러 터뜨리지 말고 log 남겨주기
        SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(receiver);

        // disconnect 되는 동안은 오는 메세지들을 db에 저장하기
        // disconnect : 웹 브라우저에서 나가거나 로그아웃 했을 때
        // 사용자가 로그아웃이나 새로고침 후에 다시 화면에 들어왔을 대 알림 메세지가 남아있으려면 db에 추가적으로 저장 필요
        // emitter 객체가 현재 서버에 있으면, 직접 알림 발송. 그렇지 않으면 redis에 publish
        if (sseEmitter != null) {
            try {
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // convertAndSend(채널, 메세지);
            redisTemplate.convertAndSend("order-channel",  data);
        }
    }


    @Override
    public void onMessage(Message message, byte[] pattern) {
        // Message : 실질적인 메세지가 담겨있는 객체. 여기서 메세지를 꺼낼 것임(data 가 들어오는 것)
        // pattern : 채널명
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SseMessageDTO sseMessageDTO = objectMapper.readValue(message.getBody(), SseMessageDTO.class);
            // 여러 개의 채널을 구독하고 있을 경우, 채널명으로 분기 처리
            String channelName = new String(pattern);
            log.info("sseMessageDTO = {}", sseMessageDTO);
            log.info("pattern = {}", channelName);

            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(sseMessageDTO.getReceiver());

            // disconnect 되는 동안은 오는 메세지들을 db에 저장하기
            // disconnect : 웹 브라우저에서 나가거나 로그아웃 했을 때
            // 사용자가 로그아웃이나 새로고침 후에 다시 화면에 들어왔을 대 알림 메세지가 남아있으려면 db에 추가적으로 저장 필요
            // emitter 객체가 현재 서버에 있으면, 직접 알림 발송. 그렇지 않으면 redis에 publish
            if (sseEmitter != null) {
                try {
                    sseEmitter.send(SseEmitter.event().name("ordered").data(sseMessageDTO));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
