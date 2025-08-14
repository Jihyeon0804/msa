package com.order.order.ordering.domain;

import com.order.order.ordering.dto.OrderCreateDTO;
import com.order.order.product.domain.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Function;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class OrderDetail{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_id")
    private Ordering ordering;
}
