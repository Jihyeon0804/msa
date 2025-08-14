package com.order.order.member.domain;

import com.order.order.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// jpql을 제외하고 모든 조회 쿼리에 where del_yn = ""을 붙이는 효과
@Where(clause = "del_yn = 'N'")
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String name;

    @Column(length = 50)
    private String email;

    private String password;

    @Builder.Default
    private String delYn = "N";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    public void updateDelYn(String delYn) {
        this.delYn = delYn;
    }
}
