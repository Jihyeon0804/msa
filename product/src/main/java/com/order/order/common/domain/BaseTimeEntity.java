package com.order.order.common.domain;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// 기본적으로 Entity는 상속이 불가능. @MappedSuperclass 사용 시 상속 가능
@MappedSuperclass
@Getter
public class BaseTimeEntity {

    // 컬럼명에 캐멀 케이스 사용 시, db 에는 created_time 으로 컬럼 생성
    @CreationTimestamp
    private LocalDateTime createdTime;
    @UpdateTimestamp
    private LocalDateTime updatedTime;
}
