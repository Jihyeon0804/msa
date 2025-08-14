package com.order.order.ordering.controller;

import com.order.order.common.dto.CommonDTO;
import com.order.order.ordering.domain.Ordering;
import com.order.order.ordering.dto.OrderCreateDTO;
import com.order.order.ordering.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordering")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 주문 등록
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody List<OrderCreateDTO> orderCreateDTOList) {
//        Long id = orderService.save(orderCreateDTOList);
        Long id = orderService.save(orderCreateDTOList);
        return new ResponseEntity<>(CommonDTO.builder()
                .result(id)
                .status_code(HttpStatus.CREATED.value())
                .status_message("주문 완료").build(), HttpStatus.CREATED);
    }
    
    // 주문 목록 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> findAll() {
        return new ResponseEntity<>(CommonDTO.builder()
                .result(orderService.findAll())
                .status_code(HttpStatus.OK.value())
                .status_message("주문 목록 조회 성공").build(), HttpStatus.OK);
    }
    
    // 나의 주문 목록 조회
    @GetMapping("/myOrders")
    public ResponseEntity<?> myOrders() {
        return new ResponseEntity<>(CommonDTO.builder()
                .result(orderService.findAllByMemberId())
                .status_code(HttpStatus.OK.value())
                .status_message("나의 주문 목록 조회 성공").build(), HttpStatus.OK);
    }


    // 주문 취소 (서비스 관리자가 사용자의 주문 취소하도록 설계)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<?> orderCancel(@PathVariable Long id) {
        Ordering ordering = orderService.cancel(id);
        return new ResponseEntity<>(CommonDTO.builder()
                .result(ordering.getId())
                .status_code(HttpStatus.OK.value())
                .status_message("주문 취소 성공").build(), HttpStatus.OK);
    }
}