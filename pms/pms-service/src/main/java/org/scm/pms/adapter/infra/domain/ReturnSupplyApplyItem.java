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
@Table(name = "return_supply_apply_item")
public class ReturnSupplyApplyItem extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apply_no", nullable = false, length = 50)
    private String applyNo;  // 对应退供申请单号

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "supplier_id", nullable = false)
    private Integer supplierId;

    @Column(name = "warehouse_id", nullable = false)
    private Integer warehouseId; // 商品退自哪个仓库

    @Column(name = "batch_no", length = 50)
    private String batchNo; // 批次号

    @Column(name = "purchase_order_no", length = 50)
    private String purchaseOrderNo; // 采购单单号

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

}
