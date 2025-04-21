package org.scm.pms.adapter.infra.domain;

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
@Table(name = "purchase_apply_item")
public class PurchaseApplyItem extends BaseBO {

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

}
