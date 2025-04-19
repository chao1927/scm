package org.scm.srm.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scm.common.BaseBO;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "supplier_delivery_order_item")
public class SupplierDeliveryOrderItem extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_no", nullable = false, length = 50)
    private String deliveryNo;  // 对应 supplier_delivery_order.delivery_no

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "production_time")
    private LocalDateTime productionTime;

    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_id", nullable = false)
    private Integer unitId;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

}
