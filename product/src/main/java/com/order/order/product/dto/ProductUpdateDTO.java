package com.order.order.product.dto;

import com.order.order.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateDTO {
    private String name;
    private String category;
    private int price;
    private int stockQuantity;
    private MultipartFile productImage;
}
