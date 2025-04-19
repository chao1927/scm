package org.scm.pms.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "purchase_order")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;

    @Column(name = "apply_no", nullable = false, length = 50)
    private String applyNo; // 对应 purchase_apply.apply_no

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;

    @Column(name = "purchaser_emp_id", nullable = false)
    private Long purchaserEmpId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "estimated_arrival_time")
    private LocalDateTime estimatedArrivalTime;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "status", nullable = false)
    private Integer status; // 如 1-待提交，2-待审核，3-待确认，4-待发货，5-待收货，6-待打款，7-采购完成

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Byte isDeleted;
}
