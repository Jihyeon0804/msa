package com.order.order.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
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
}
