package com.order.order.ordering.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.order.common.dto.CommonDTO;
import com.order.order.common.service.SseAlarmService;
import com.order.order.ordering.domain.OrderDetail;
import com.order.order.ordering.domain.Ordering;
import com.order.order.ordering.dto.OrderCreateDTO;
import com.order.order.ordering.dto.OrderListResDTO;
import com.order.order.ordering.dto.ProductDTO;
import com.order.order.ordering.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final SseAlarmService sseAlarmService;
    private final RestTemplate restTemplate;

    // 주문 생성 (redisTemplate)
    public Long save(List<OrderCreateDTO> orderCreateDTOList, String email) {
        Ordering ordering = Ordering.builder().memberEmail(email).build();

        for (OrderCreateDTO orderCreateDTO : orderCreateDTOList) {
            // 상품 조회
            // restTemplate.exchange(url, HttpMethod 요청 방식, 헤더부, 리턴클래스);
            // 리턴클래스 : 파싱하고 싶은 클래스명 명시
            // url을 http://localhost:8080/product-service/product/detail로 쓰게 되면
            // 요청이 api-gateway 밖으로 한 번 나갔다가 들어오게 되기 때문에 안됨
            String productDetailUrl = "http://product-service/product/detail/" + orderCreateDTO.getProductId();
            HttpHeaders httpHeaders = new HttpHeaders();        // httpHeaders의 기본값 세팅 (사용자 정의 header 세팅 없을 경우)
            // HttpEntity : HttpBody와 HttpHeader를 세팅하기 위한 객체
            HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
            // HttpMethod.PATCH 있지만 사용 시 error 발생
            ResponseEntity<CommonDTO> response = restTemplate.exchange(productDetailUrl, HttpMethod.GET
                    , httpEntity, CommonDTO.class);
            CommonDTO commonDTO = response.getBody();
            ObjectMapper objectMapper = new ObjectMapper();

            // readValue() : String -> 클래스 변환
            // convertValue() : Object 클래스 -> 원하는 클래스 변환
            ProductDTO product = objectMapper.convertValue(commonDTO.getResult(), ProductDTO.class);
            if (product.getStockQuantity() < orderCreateDTO.getProductCount()) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }

            // 주문 등록
            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(orderCreateDTO.getProductCount())
                    .ordering(ordering)
                    .build();

            ordering.getOrderDetailList().add(orderDetail);

            // kafka를 활용한 비동기적 재고 감소 요청

        }

        // 알림 발송
        sseAlarmService.publishMessage("admin@email.com", email, ordering.getId());
        // db 저장
        orderRepository.save(ordering);

        return ordering.getId();
    }

    // 주문 등록 (FeignClient + Kafka)
    public Long createFeignKafka(List<OrderCreateDTO> orderCreateDTOList, String email) {
        Ordering ordering = Ordering.builder().memberEmail(email).build();

        for (OrderCreateDTO orderCreateDTO : orderCreateDTOList) {

            // feign 클라이언트를 사용한 동기적 상품 조회
            if (product.getStockQuantity() < orderCreateDTO.getProductCount()) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }

            // 주문 등록
            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(orderCreateDTO.getProductCount())
                    .ordering(ordering)
                    .build();

            ordering.getOrderDetailList().add(orderDetail);

            // 동기적 재고 감소 요청
            String productUpdateStockUrl = "http://product-service/product/updateStock";
            HttpHeaders stockHeaders = new HttpHeaders();        // httpHeaders의 기본값 세팅 (사용자 정의 header 세팅 없을 경우)
            stockHeaders.setContentType(MediaType.APPLICATION_JSON);
            // HttpEntity : HttpBody와 HttpHeader를 세팅하기 위한 객체
            HttpEntity<OrderCreateDTO> updateStockEntity = new HttpEntity<>(orderCreateDTO, stockHeaders);
            // return 받을 게 없는 경우는 void 로 설정
            restTemplate.exchange(productUpdateStockUrl, HttpMethod.PUT
                    , updateStockEntity, Void.class);
        }

        // 알림 발송
        sseAlarmService.publishMessage("admin@email.com", email, ordering.getId());
        // db 저장
        orderRepository.save(ordering);

        return ordering.getId();
    }
    
    // 주문 목록 조회
    public List<OrderListResDTO> findAll() {
        return orderRepository.findAll().stream()
                .map(OrderListResDTO::fromEntity).collect(Collectors.toList());
    }
    
    // 나의 주문 목록 조회
    public List<OrderListResDTO> myOrders(String email) {
        return  orderRepository.findAllByMemberEmail(email).stream()
                .map(OrderListResDTO::fromEntity).collect(Collectors.toList());
    }
}
