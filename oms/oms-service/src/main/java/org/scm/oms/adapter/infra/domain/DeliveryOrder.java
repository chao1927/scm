package org.scm.oms.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scm.common.BaseBO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "delivery_order")
public class DeliveryOrder extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_no", nullable = false, unique = true, length = 50)
    private String deliveryNo;

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

    @Column(name = "logistic_no", length = 50)
    private String logisticNo;

    @Column(name = "logistic_cost", precision = 10, scale = 2)
    private BigDecimal logisticCost;

    @Column(name = "delivery_status", nullable = false)
    private Integer deliveryStatus; // 1-待分波,2-待分拣,3-待复核,4-待打包,5-待称重,6-待分配物流,7-待出库,8-待揽收,9-发货成功

    // 新增目的地地址字段：
    @Column(name = "dest_name", length = 50)
    private String destName;

    @Column(name = "dest_phone", length = 20)
    private String destPhone;

    @Column(name = "dest_address", length = 200)
    private String destAddress;

    @Column(name = "dest_postcode", length = 10)
    private String destPostcode;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

}
