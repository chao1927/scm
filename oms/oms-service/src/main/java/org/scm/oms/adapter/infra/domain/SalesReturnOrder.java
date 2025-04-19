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
@Table(name = "sales_return_order")
public class SalesReturnOrder extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sales_return_no", nullable = false, length = 50, unique = true)
    private String salesReturnNo;

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo; // 关联原订单号

    @Column(name = "return_apply_no", nullable = false, length = 50)
    private String returnApplyNo; // 对应 return_apply.return_apply_no

    @Column(name = "apply_type", nullable = false)
    private Integer applyType;

    @Column(name = "refund_requested_amount", precision = 10, scale = 2)
    private BigDecimal refundRequestedAmount;

    @Column(name = "actual_refund_amount", precision = 10, scale = 2)
    private BigDecimal actualRefundAmount;

    @Column(name = "refund_reason", length = 200)
    private String refundReason;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "return_type", nullable = false)
    private Integer returnType;

    @Column(name = "apply_time", nullable = false)
    private LocalDateTime applyTime;

    @Column(name = "logistics_no", length = 50)
    private String logisticsNo; // 客户提供的退货物流单号

    // 取件信息
    @Column(name = "pickup_contact", length = 50)
    private String pickupContact;

    @Column(name = "pickup_phone", length = 20)
    private String pickupPhone;

    @Column(name = "pickup_address", length = 200)
    private String pickupAddress;

    @Column(name = "pickup_postcode", length = 10)
    private String pickupPostcode;

    @Column(name = "pickup_longitude", precision = 10, scale = 6)
    private BigDecimal pickupLongitude;

    @Column(name = "pickup_latitude", precision = 10, scale = 6)
    private BigDecimal pickupLatitude;

    // 收货信息（仓库填写）
    @Column(name = "receipt_contact", length = 50)
    private String receiptContact;

    @Column(name = "receipt_phone", length = 20)
    private String receiptPhone;

    @Column(name = "receipt_address", length = 200)
    private String receiptAddress;

    @Column(name = "receipt_postcode", length = 10)
    private String receiptPostcode;

    @Column(name = "receipt_longitude", precision = 10, scale = 6)
    private BigDecimal receiptLongitude;

    @Column(name = "receipt_latitude", precision = 10, scale = 6)
    private BigDecimal receiptLatitude;

    @Column(name = "return_status", nullable = false)
    private Integer returnStatus; // 如：待审核、已审核、待收货、已收货、拒收等

}
