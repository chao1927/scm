package org.scm.pms.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "return_supply_order_item")
public class ReturnSupplyOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo; // 退供单单号

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "supplier_id", nullable = false)
    private Integer supplierId;

    @Column(name = "warehouse_id", nullable = false)
    private Integer warehouseId;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "purchase_order_no", length = 50)
    private String purchaseOrderNo;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "return_price", precision = 10, scale = 2)
    private BigDecimal returnPrice;

    @Column(name = "return_reason", length = 200)
    private String returnReason;

    @Column(name = "unit_id", nullable = false)
    private Integer unitId;

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
    private Integer isDeleted;
}
