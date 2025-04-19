package org.scm.pms.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "return_supply_apply")
public class ReturnSupplyApply {

    // 主键单独声明
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apply_no", nullable = false, unique = true, length = 50)
    private String applyNo;

    @Column(name = "sales_emp_id", nullable = false)
    private Integer salesEmpId; // 销售人员ID

    @Column(name = "strategy_config", columnDefinition = "TEXT")
    private String strategyConfig; // 退供策略配置(JSON)

    @Column(name = "total_item_types")
    private Integer totalItemTypes;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "apply_time", nullable = false)
    private LocalDateTime applyTime;

    @Column(name = "status", nullable = false)
    private Integer status; // 例如：1-待审核，2-审核通过，3-取消

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    // 审计字段
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_by")
    private Integer updatedBy;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "is_deleted", nullable = false)
    private Byte isDeleted;


}
