package com.order.order.ordering.domain;

import com.order.order.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Ordering extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    private String memberEmail;

    @Builder.Default
    @OneToMany(mappedBy = "ordering", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<OrderDetail> orderDetailList = new ArrayList<>();

    public void cancelStatus() {
        this.orderStatus = OrderStatus.CANCELED;
    }
}
