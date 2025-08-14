package com.order.order.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenDTO {
    // 하나의 필드 밖에 없는데 매개변수로 받지 않고 body로 받음 => 보안 상 이유
    // url 파라미터로 받으면 url에 남아있을 수 있기 때문에 body로 받아야 안전
    private String refreshToken;
}
