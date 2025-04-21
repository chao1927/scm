package org.scm.pms.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.scm.common.BaseBO;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "return_supply_delivery_order_item")
public class ReturnSupplyDeliveryOrderItem extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_no", nullable = false, length = 50)
    private String deliveryNo; // 关联退供发货单单号

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

    @Column(name = "unit_id", nullable = false)
    private Integer unitId;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

}
