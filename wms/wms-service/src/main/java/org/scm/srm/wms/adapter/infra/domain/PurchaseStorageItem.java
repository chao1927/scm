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
@Table(name = "purchase_storage_item")
public class PurchaseStorageItem extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "storage_no", nullable = false, length = 50)
    private String storageNo; // 对应 purchase_storage.storage_no

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "unit_id", nullable = false)
    private Integer unitId;

    @Column(name = "production_time")
    private LocalDateTime productionTime;

    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "storage_quantity")
    private Integer storageQuantity;

    @Column(name = "inspection_quantity")
    private Integer inspectionQuantity;

    @Column(name = "shelving_quantity")
    private Integer shelvingQuantity;

    @Column(name = "good_quantity")
    private Integer goodQuantity;

    @Column(name = "defective_quantity")
    private Integer defectiveQuantity;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

}
