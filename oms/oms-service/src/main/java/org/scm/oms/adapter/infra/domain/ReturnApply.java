package org.scm.oms.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.scm.common.BaseBO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "return_apply")
public class ReturnApply extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo;  // 关联订单号（电商平台或 OMS 订单号）

    @Column(name = "return_apply_no", nullable = false, length = 50, unique = true)
    private String returnApplyNo;

    @Column(name = "apply_type", nullable = false)
    private Integer applyType;  // 1-全部退货, 2-部分退货, 3-仅退款, 4-仅部分退款

    @Column(name = "refund_total_amount", precision = 10, scale = 2)
    private BigDecimal refundTotalAmount;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_reason", length = 200)
    private String refundReason;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "return_type", nullable = false)
    private Integer returnType; // 1-上门取件, 2-自行邮寄

    @Column(name = "apply_time", nullable = false)
    private LocalDateTime applyTime;

    @Column(name = "audit_emp_id")
    private Long auditEmpId; // 审核人（员工ID）

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

}
