package com.order.order.ordering.dto;

import com.order.order.ordering.domain.OrderDetail;
import com.order.order.ordering.domain.OrderStatus;
import com.order.order.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListResDTO {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    private List<OrderDetailResDTO> orderDetailResDTOList;

    public static OrderListResDTO fromEntity(Ordering ordering){
        List<OrderDetailResDTO> orderDetailResDTOList = new ArrayList<>();
        for (OrderDetail orderDetail : ordering.getOrderDetailList()){
            orderDetailResDTOList.add(OrderDetailResDTO.fromEntity(orderDetail));
        }
        OrderListResDTO orderListResDTO = OrderListResDTO.builder()
                .id(ordering.getId())
                .memberEmail(ordering.getMemberEmail())
                .orderStatus(ordering.getOrderStatus())
                .orderDetailResDTOList(orderDetailResDTOList)
                .build();
        return orderListResDTO;
    }
}
