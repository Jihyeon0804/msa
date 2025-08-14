package com.order.order.common.config;

import com.order.order.common.service.SseAlarmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

//    Qualifier : "같은 Bean 객체가 여러 개" 있을 경우 Bean 객체를 구분하기 위한 어노테이션
    @Bean
    @Qualifier("rtInventory")
    public RedisConnectionFactory redisConnectionFactory() {        // 연결 객체
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);
        return new LettuceConnectionFactory(configuration);
    }

    // 싱글톤 객체를 파라미터로 주입
    // Bean 들끼리 서로 의존성을 주입 받을 때 메서드 파라미터로도 주입 가능
    // 모든 템플릿 중에서 redisTemplate 이름의 메서드는 반드시 하나는 있어야 함
    @Bean
    @Qualifier("rtInventory")
    public RedisTemplate<String, String> redisTemplate(@Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory) {          // 템플릿 객체
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // String 으로 받으면 내가 직접 JSON 으로 변환 (값을 꺼낼 때도 동일)
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        
        // value가 객체인 경우 사용 (알아서 JSON 형식으로 변환)
//        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        // 0번 DB와 연결된 객체(@Qualifier("rtInventory")) 사용
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    // redis pub/sub을 위한 연결 객체 생성
    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory sseFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        
        // redis pub/sub 기능은 db에 값을 저장하는 기능이 아니므로, 특정 db에 의존적이지 않음
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("ssePubSub")
    public RedisTemplate<String, String> sseRedisTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory) {          // 템플릿 객체
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
    
    // redis 리스너 객체
    @Bean
    @Qualifier("ssePubSub")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory
            , MessageListenerAdapter messageListenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("order-channel"));
        // 만약 여러 채널을 구독해야 하는 경우에는 아래 코드처럼 여러 개의 PatterTopic을 add 하거나 별도의 bean 객체 생성
        // container.addMessageListener(messageListenerAdapter, new PatternTopic("comment-channel"));
        return container;
    }

    // redis 채널에서 수신된 메세지를 처리하는 빈 객체
    @Bean
    public MessageListenerAdapter messageListenerAdapter(SseAlarmService sseAlarmService) {

        // onMessage는 SseAlarmService 에서 implements 한 MessageListener 에서 상속 받은 메서드 명(상속 필수)
        // 채널로부터 수신되는 message 처리를 SseAlarmService의 onMessage 메서드로 설정
        // 즉, 메세지가 수신되면 onMessage 메서드가 호출됨
        return new MessageListenerAdapter(sseAlarmService, "onMessage");
    }
}
