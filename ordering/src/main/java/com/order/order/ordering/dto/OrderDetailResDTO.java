package com.order.order.ordering.dto;

import com.order.order.ordering.domain.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResDTO {
    private Long id;
    private String productName;
    private int productCount;

    public static OrderDetailResDTO fromEntity(OrderDetail orderDetail) {
        return OrderDetailResDTO.builder()
                .id(orderDetail.getId())
                .productName(orderDetail.getProductName())
                .productCount(orderDetail.getQuantity()).build();
    }
}
