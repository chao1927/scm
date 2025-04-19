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
@Table(name = "supplier_delivery_order")
public class SupplierDeliveryOrder extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_no", nullable = false, length = 50, unique = true)
    private String deliveryNo;

    @Column(name = "delivery_time", nullable = false)
    private LocalDateTime deliveryTime;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo;  // 对应 purchase_order.order_no

    @Column(name = "apply_no", nullable = false, length = 50)
    private String applyNo;  // 对应 purchase_apply.apply_no

    @Column(name = "purchaser_emp_id", nullable = false)
    private Long purchaserEmpId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "logistics_number", length = 50)
    private String logisticsNumber;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "status", nullable = false)
    private Integer status;  // 1-待发货, 2-运输中, 3-已完成

}
