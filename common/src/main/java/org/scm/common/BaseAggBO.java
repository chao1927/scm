package org.scm.common;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseAggBO {

    private Long createdBy; // 创建用户

    private LocalDateTime createdAt;

    private Long updatedBy;

    private LocalDateTime updatedAt;

    private Integer isDeleted;
}
