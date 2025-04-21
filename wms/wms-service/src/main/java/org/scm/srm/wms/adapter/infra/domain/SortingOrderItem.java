package org.scm.srm.wms.adapter.infra.domain;

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
@Table(name = "sorting_order_item")
public class SortingOrderItem extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sorting_no", nullable = false, length = 50)
    private String sortingNo; // 对应 sorting_order.sorting_no

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "order_quantity", nullable = false)
    private Integer orderQuantity;

    @Column(name = "picked_quantity")
    private Integer pickedQuantity;

    @Column(name = "pending_quantity")
    private Integer pendingQuantity;

    @Column(name = "unit_id", nullable = false)
    private Integer unitId;

    @Column(name = "batch_number", length = 50)
    private String batchNumber; // 批次号（注意：表中字段为 batch_number，可保持原名或统一改为 batch_no，此处示例使用 batch_number）

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "picking_time")
    private LocalDateTime pickingTime;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

}
