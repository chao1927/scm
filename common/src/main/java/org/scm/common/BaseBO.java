package org.scm.common;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseBO {

    @Column(name = "created_by", length = 50)
    private Long createdBy; // 创建用户

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 创建时间

    @Column(name = "updated_by", length = 50)
    private Long updatedBy; // 修改用户

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 修改时间

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted; // 是否删除：0（未删除）/1（已删除）
}
