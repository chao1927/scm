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
@Table(name = "return_supply_delivery_order")
public class ReturnSupplyDeliveryOrder extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_no", nullable = false, length = 50, unique = true)
    private String deliveryNo; // 退供发货单单号

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo; // 关联退供单单号

    @Column(name = "supplier_id", nullable = false)
    private Integer supplierId;

    @Column(name = "warehouse_id", nullable = false)
    private Integer warehouseId; // 退供发货仓库ID

    @Column(name = "expected_delivery_time")
    private LocalDateTime expectedDeliveryTime;

    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime;

    @Column(name = "logistic_channel_id")
    private Integer logisticChannelId;

    @Column(name = "logistic_no", length = 50)
    private String logisticNo;

    @Column(name = "logistic_cost", precision = 10, scale = 2)
    private BigDecimal logisticCost;

    @Column(name = "status", nullable = false)
    private Integer status; // 如：1-待发货，2-运输中，3-已发货

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;


}
