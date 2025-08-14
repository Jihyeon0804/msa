package com.order.order.ordering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {
    private Long productId;
    private Integer productCount;
}
