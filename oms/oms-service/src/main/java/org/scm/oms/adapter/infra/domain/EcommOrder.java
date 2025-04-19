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
@Table(name = "ecomm_order")
public class EcommOrder extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform_order_no", nullable = false, unique = true, length = 50)
    private String platformOrderNo;

    @Column(name = "customer_name", nullable = false, length = 50)
    private String customerName;

    @Column(name = "customer_nickname", length = 50)
    private String customerNickname;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "address_detail", length = 200)
    private String addressDetail;

    @Column(name = "total_product_amount", precision = 10, scale = 2)
    private BigDecimal totalProductAmount;

    @Column(name = "freight", precision = 10, scale = 2)
    private BigDecimal freight;

    @Column(name = "actual_payment", precision = 10, scale = 2)
    private BigDecimal actualPayment;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "pay_time")
    private LocalDateTime payTime;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Column(name = "payment_trade_no", length = 50)
    private String paymentTradeNo;

    @Column(name = "order_status", nullable = false)
    private Integer orderStatus;   // 例如：1-待发货, 2-已发货, 3-完成, 4-关闭

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

}
