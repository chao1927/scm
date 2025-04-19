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
@Table(name = "purchase_apply_item")
public class PurchaseApplyItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apply_no", nullable = false, length = 50)
    private String applyNo; // 对应 purchase_apply.apply_no

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_id", nullable = false)
    private Integer unitId;

    @Column(name = "purchased_quantity")
    private Integer purchasedQuantity;

    @Column(name = "estimated_unit_price", precision = 10, scale = 2)
    private BigDecimal estimatedUnitPrice;

    @Column(name = "estimated_total_price", precision = 10, scale = 2)
    private BigDecimal estimatedTotalPrice;

    @Column(name = "warehouse_id")
    private Long warehouseId;

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
