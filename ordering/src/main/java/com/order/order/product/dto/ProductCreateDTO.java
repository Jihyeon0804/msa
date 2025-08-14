package com.order.order.product.dto;

import com.order.order.member.domain.Member;
import com.order.order.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDTO {
    private String name;
    private String category;
    private int price;
    private int stockQuantity;
    private MultipartFile productImage;

    public Product toEntity(Member member) {
        return Product.builder()
                .name(this.name)
                .category(this.category)
                .price(this.price)
                .stockQuantity(this.stockQuantity)
                .member(member)
                .build();
    }
}
