package com.order.order.ordering.service;

import com.order.order.common.service.SseAlarmService;
import com.order.order.member.domain.Member;
import com.order.order.member.repository.MemberRepository;
import com.order.order.ordering.domain.OrderDetail;
import com.order.order.ordering.domain.Ordering;
import com.order.order.ordering.dto.OrderCreateDTO;
import com.order.order.ordering.dto.OrderDetailResDTO;
import com.order.order.ordering.dto.OrderListResDTO;
import com.order.order.ordering.repository.OrderRepository;
import com.order.order.product.domain.Product;
import com.order.order.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final SseAlarmService sseAlarmService;

    // 주문 생성
    public Long save(List<OrderCreateDTO> orderCreateDTOList) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("로그인 후 이용 가능"));

        Ordering ordering = Ordering.builder().member(member).build();

        for (OrderCreateDTO orderCreateDTO : orderCreateDTOList) {
            Product product = productRepository.findById(orderCreateDTO.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("product is not found"));

            if (product.getStockQuantity() < orderCreateDTO.getProductCount()) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }

            product.updateStockQuantity(orderCreateDTO.getProductCount());

            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .quantity(orderCreateDTO.getProductCount())
                    .ordering(ordering)
                    .build();

            ordering.getOrderDetailList().add(orderDetail);
        }

        // 주문 성공 시 admin 유저에게 알림 메세지 전송
        sseAlarmService.publishMessage("admin@email.com", email, ordering.getId());
        
        // db 저장
        orderRepository.save(ordering);

        return ordering.getId();
    }
    
    // 주문 목록 조회
    public List<OrderListResDTO> findAll() {
        List<Ordering> orderList = orderRepository.findAll();
        List<OrderListResDTO> orderListResDTOList = new ArrayList<>();
        for (Ordering ordering : orderList) {
            List<OrderDetailResDTO> orderDetailResDTOList = ordering.getOrderDetailList().stream()
                    .map(OrderDetailResDTO::fromEntity).collect(Collectors.toList());

            OrderListResDTO orderListResDTO = OrderListResDTO.builder()
                        .id(ordering.getId())
                        .orderStatus(ordering.getOrderStatus())
                        .orderDetailResDTOList(orderDetailResDTOList)
                        .memberEmail(ordering.getMember().getEmail()).build();

            orderListResDTOList.add(orderListResDTO);
        }
        return orderListResDTOList;
    }
    
    // 나의 주문 목록 조회
    public List<OrderListResDTO> findAllByMemberId() {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow();
        Long id = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member is not found")).getId();
        List<Ordering> orderList = orderRepository.findAllByMemberId(id);
        List<OrderListResDTO> orderListResDTOList = new ArrayList<>();
        for (Ordering ordering : orderList) {
            List<OrderDetailResDTO> orderDetailResDTOList = ordering.getOrderDetailList().stream()
                    .map(OrderDetailResDTO::fromEntity).collect(Collectors.toList());

            OrderListResDTO orderListResDTO = OrderListResDTO.builder()
                    .id(ordering.getId())
                    .orderStatus(ordering.getOrderStatus())
                    .orderDetailResDTOList(orderDetailResDTOList)
                    .memberEmail(ordering.getMember().getEmail()).build();

            orderListResDTOList.add(orderListResDTO);
        }
        return orderListResDTOList;
    }
    
    
    // 주문 취소
    public Ordering cancel(Long id) {
        // Ordering의 DB 상태값 변경 (ORDERED -> CANCELED)
        Ordering ordering = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("product is not found"));
        ordering.cancelStatus();
        

        for (OrderDetail orderDetail : ordering.getOrderDetailList()) {

            // rdb에 재고 업데이트
            orderDetail.getProduct().cancelOrder(orderDetail.getQuantity());
        }

        return ordering;
    }
}
