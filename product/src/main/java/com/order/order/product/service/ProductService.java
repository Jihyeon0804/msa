package com.order.order.product.service;

import com.order.order.product.domain.Product;
import com.order.order.product.dto.*;
import com.order.order.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final ProductRepository productRepository;
    private final S3Client s3Client;

    // 상품 등록
    public Long save(ProductCreateDTO productCreateDTO, String email) {
        Product product = productRepository.save(productCreateDTO.toEntity(email));

        if (productCreateDTO.getProductImage() != null) {
            // 이미지 파일명 설정
            String fileName = "product-" + product.getId() + "-profileImage-" + productCreateDTO.getProductImage().getOriginalFilename();

            // 저장 객체 구성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(productCreateDTO.getProductImage().getContentType())         // jpeg, mp4, ...
                    .build();

            // 이미지 업로드 (byte 형태로 업로드)
            try {
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(productCreateDTO.getProductImage().getBytes()));
            } catch (Exception e) {
                // checked 를 unchecked로 바꿔 전체 rollback 되도록 예외 처리
                throw new IllegalArgumentException("이미지 업로드 실패");
            }

            // 이미지 삭제
//            s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));

            // S3에서 이미지 url 추출
            String imgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();
            product.updateImageUrl(imgUrl);
        }

        return product.getId();
    }

    // 상품 목록
    public List<ProductResDTO> findAll() {
        return productRepository.findAll().stream().map(ProductResDTO::fromEntity).collect(Collectors.toList());
    }

    public Page<ProductResDTO> findAll(Pageable pageable, ProductSearchDTO productSearchDTO) {
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                // name input값 존재 여부 확인
                if (productSearchDTO.getProductName() != null) {
                    //  and title like '%postSearchDTO.getTitle%'
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%" + productSearchDTO.getProductName() + "%"));
                }
                // category input값 존재 여부 확인
                if (productSearchDTO.getCategory() != null) {
                    //  and category = "postSearchDTO.getCategory";
                    predicateList.add(criteriaBuilder.equal(root.get("category"), productSearchDTO.getCategory()));
                }
                Predicate[] predicateArr = new Predicate[predicateList.size()];
                for (int i = 0; i < predicateList.size(); i++) {
                    predicateArr[i] = predicateList.get(i);
                }

                // 위의 검색 조건을 하나(한 줄)의 Predicate 객체로 만들어서 return
                Predicate predicate = criteriaBuilder.and(predicateArr);

                return predicate;
            }
        };

//        Page<Product> productPage = productRepository.findAll(specification, pageable);
//        return productPage.map(ProductResDTO::fromEntity);
        return productRepository.findAll(specification, pageable).map(ProductResDTO::fromEntity);

    }

    // 상품 상세 조회
    public ProductResDTO findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다."));
        return ProductResDTO.fromEntity(product);
    }
    
    // 상품 정보 수정
    public Long update(Long id, ProductUpdateDTO productUpdateDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다."));
        product.updateDTO(productUpdateDTO);

        // 이미지 업데이트 전에 일단 비워두고 시작
        // 기존 이미지 삭제
        if (product.getImagePath() != null) {
            String imgUrl = product.getImagePath();
            String fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);
            s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));
        }

        // 이미지 업데이트
        if (productUpdateDTO.getProductImage() != null && !productUpdateDTO.getProductImage().isEmpty()) {
            String newFileName = "product-" + product.getId() + "-profileImage-" + productUpdateDTO.getProductImage().getOriginalFilename();

            // 신규 이미지 저장 객체 구성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(newFileName)
                    .contentType(productUpdateDTO.getProductImage().getContentType())         // jpeg, mp4, ...
                    .build();


            // 이미지 업로드 (byte 형태로 업로드)
            try {
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(productUpdateDTO.getProductImage().getBytes()));
            } catch (Exception e) {
                // checked 를 unchecked로 바꿔 전체 rollback 되도록 예외 처리
                throw new IllegalArgumentException("이미지 업로드 실패");
            }

            // S3에서 이미지 url 추출
            String newImgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(newFileName)).toExternalForm();
            product.updateImageUrl(newImgUrl);
        } else {
            product.updateImageUrl(null);
        }

        return product.getId();
    }

    public Long updateStock(ProductUpdateStockDTO productUpdateStockDTO) {
        Product product = productRepository.findById(productUpdateStockDTO.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("product is not found"));

        if (product.getStockQuantity() < productUpdateStockDTO.getProductCount()) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        log.error("######### 수량 : {}", productUpdateStockDTO.getProductCount());
        product.updateStockQuantity(productUpdateStockDTO.getProductCount());

        return product.getId();
    }
}
