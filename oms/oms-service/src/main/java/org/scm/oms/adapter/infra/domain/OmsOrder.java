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
@Table(name = "oms_order")
public class OmsOrder extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "oms_order_no", nullable = false, unique = true, length = 50)
    private String omsOrderNo;

    @Column(name = "platform", nullable = false, length = 20)
    private String platform;  // 如：淘宝、天猫、京东等

    @Column(name = "platform_order_no", nullable = false, length = 50)
    private String platformOrderNo;

    @Column(name = "customer_name", nullable = false, length = 50)
    private String customerName;

    @Column(name = "customer_nickname", length = 50)
    private String customerNickname;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "address_detail", length = 200)
    private String addressDetail;

    @Column(name = "order_total", precision = 10, scale = 2)
    private BigDecimal orderTotal;

    @Column(name = "freight", precision = 10, scale = 2)
    private BigDecimal freight;

    @Column(name = "actual_payment", precision = 10, scale = 2)
    private BigDecimal actualPayment;

    @Column(name = "receive_time")
    private LocalDateTime receiveTime;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "pay_time")
    private LocalDateTime payTime;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @Column(name = "payment_way", length = 20)
    private String paymentWay;

    @Column(name = "payment_trade_no", length = 50)
    private String paymentTradeNo;

    @Column(name = "platform_order_status", nullable = false)
    private Integer platformOrderStatus;

    @Column(name = "order_status", nullable = false)
    private Integer orderStatus;  // 如：1-待审核, 2-审核通过, 3-分仓, 4-已拆单, 5-待发货,6-已发货

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

}
