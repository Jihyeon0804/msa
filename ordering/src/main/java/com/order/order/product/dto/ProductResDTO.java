package com.order.order.product.dto;

import com.order.order.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResDTO {
    private Long id;
    private String name;
    private String category;
    private int price;
    private int stockQuantity;
    private String imagePath;

    public static ProductResDTO fromEntity(Product product) {
        return ProductResDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imagePath(product.getImagePath()).build();
    }
}
