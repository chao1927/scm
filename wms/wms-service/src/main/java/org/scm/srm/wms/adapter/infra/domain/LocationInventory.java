package org.scm.srm.wms.adapter.infra.domain;

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
@Table(name = "location_inventory")
public class LocationInventory extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "unit_id", nullable = false)
    private Integer unitId;

    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "production_time")
    private LocalDateTime productionTime;

    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "locked_quantity")
    private Integer lockedQuantity;

    @Column(name = "storage_time")
    private LocalDateTime storageTime;

    @Column(name = "shelving_time")
    private LocalDateTime shelvingTime;

    public LocationInventory(Object o, Object o1, Long locationId, String sku, String batchNo, Object o2, Integer quantity, int i) {

    }
}
