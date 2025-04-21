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
@Table(name = "sales_outbound_order")
public class SalesOutboundOrder extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outbound_no", nullable = false, length = 50, unique = true)
    private String outboundNo;

    @Column(name = "delivery_no", nullable = false, length = 50)
    private String deliveryNo;

    @Column(name = "wave_no", length = 50)
    private String waveNo;

    @Column(name = "sorting_no", length = 50)
    private String sortingNo;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "oms_order_no", nullable = false, length = 50)
    private String omsOrderNo;

    @Column(name = "expected_delivery_time")
    private LocalDateTime expectedDeliveryTime;

    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime;

    @Column(name = "logistic_channel_id")
    private Long logisticChannelId;

    @Column(name = "logistic_cost", precision = 10, scale = 2)
    private BigDecimal logisticCost;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "outbound_status", nullable = false)
    private Integer outboundStatus;  // 如1-待出库,2-出库完成

}
