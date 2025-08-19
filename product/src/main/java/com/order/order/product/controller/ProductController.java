package com.order.order.product.controller;

import com.order.order.common.dto.CommonDTO;
import com.order.order.product.dto.*;
import com.order.order.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    
    // 상품 등록
    @PostMapping("/create")
    public ResponseEntity<?> create(@ModelAttribute ProductCreateDTO productCreateDTO
                                    , @RequestHeader("X-User-Email") String email) {
        Long id = productService.save(productCreateDTO, email);
        return new ResponseEntity<>(CommonDTO.builder()
                .result(id)
                .status_code(HttpStatus.CREATED.value())
                .status_message("상품 등록 성공").build(), HttpStatus.CREATED);
    }

    // 상품 목록 조회 + 페이징 처리 + 검색
    @GetMapping("/list")
    public ResponseEntity<?> findAll(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
            , ProductSearchDTO productSearchDTO) {
        Page<ProductResDTO> productSearchDTOPage = productService.findAll(pageable, productSearchDTO);
        return new ResponseEntity<>(CommonDTO.builder()
                .result(productSearchDTOPage)
                .status_code(HttpStatus.OK.value())
                .status_message("상품 목록 조회 성공").build(), HttpStatus.OK);
    }

    // 상품 상세 정보 조회
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) throws InterruptedException {
        Thread.sleep(3000L);        // circuit 테스트를 위한 지연

        return new ResponseEntity<>(CommonDTO.builder()
                .result(productService.findById(id))
                .status_code(HttpStatus.OK.value())
                .status_message("상품 상세 조회 성공").build(), HttpStatus.OK);
    }
    
    // 상품 수정
    // 기존 이미지 삭제 후 새로운 이미지 등록 및 url 변경
    // s3에서 이미지 삭제 방법 : url로 삭제하기는 어렵고 파일명 삭제
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @ModelAttribute ProductUpdateDTO productUpdateDTO) {
        Long productId = productService.update(id, productUpdateDTO);
        return new ResponseEntity<>(CommonDTO.builder()
                .result(productId)
                .status_code(HttpStatus.OK.value())
                .status_message("상품 수정 성공").build(), HttpStatus.OK);
    }

    @PutMapping("/updateStock")
    public ResponseEntity<?> updateStock(@RequestBody ProductUpdateStockDTO productUpdateStockDTO) {
        Long productId = productService.updateStock(productUpdateStockDTO);
        return new ResponseEntity<>(CommonDTO.builder()
                .result(productId)
                .status_code(HttpStatus.OK.value())
                .status_message("재고 수량 변경 완료").build(), HttpStatus.OK);
    }
}