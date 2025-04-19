package org.scm.srm.wms.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.scm.common.BaseBO;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "warehouse_inventory")
public class WarehouseInventory extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "in_transit_quantity")
    private Integer inTransitQuantity;

    @Column(name = "locked_quantity")
    private Integer lockedQuantity;

    @Column(name = "frozen_quantity")
    private Integer frozenQuantity;
}
