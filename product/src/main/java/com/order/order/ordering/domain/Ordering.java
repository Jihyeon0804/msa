package com.order.order.ordering.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.order.order.common.domain.BaseTimeEntity;
import com.order.order.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.engine.internal.Cascade;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder.Default
    @OneToMany(mappedBy = "ordering", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<OrderDetail> orderDetailList = new ArrayList<>();

    public void cancelStatus() {
        this.orderStatus = OrderStatus.CANCELED;
    }
}
