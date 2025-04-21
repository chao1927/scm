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
@Table(name = "location_inventory_flow")
public class LocationInventoryFlow extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "flow_type", nullable = false)
    private Integer flowType; // 1-采购入库,2-退货入库,3-销售出库,4-退供出库

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(name = "flow_time", nullable = false)
    private LocalDateTime flowTime;

    @Column(name = "related_order_id", length = 50)
    private String relatedOrderId;

}
