package com.order.order.member.dto;

import com.order.order.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResDTO {
    private Long id;
    private String name;
    private String email;

    public static MemberResDTO fromEntity(Member member) {
        return MemberResDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }
}
