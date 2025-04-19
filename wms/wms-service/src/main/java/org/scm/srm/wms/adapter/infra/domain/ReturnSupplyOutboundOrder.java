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
@Table(name = "return_supply_outbound_order")
public class ReturnSupplyOutboundOrder extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outbound_no", nullable = false, length = 50, unique = true)
    private String outboundNo; // 退供出库单单号

    @Column(name = "delivery_no", nullable = false, length = 50)
    private String deliveryNo; // 关联退供发货单单号

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo; // 关联退供单单号

    @Column(name = "warehouse_id", nullable = false)
    private Integer warehouseId;

    @Column(name = "total_item_types")
    private Integer totalItemTypes;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "operator_id", nullable = false)
    private Integer operatorId; // 仓库操作员ID

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "outbound_time")
    private LocalDateTime outboundTime;

    @Column(name = "logistic_channel_id")
    private Integer logisticChannelId;

    @Column(name = "logistic_cost", precision = 10, scale = 2)
    private BigDecimal logisticCost;

    @Column(name = "status", nullable = false)
    private Integer status; // 如1-待出库，2-出库中，3-已出库

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;


}
