package com.order.order.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SseMessageDTO {

    private String sender;
    private String receiver;
    private Long orderingId;
}
