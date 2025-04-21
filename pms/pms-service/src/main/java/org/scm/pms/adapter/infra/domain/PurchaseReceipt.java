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
@Table(name = "purchase_receipt")
public class PurchaseReceipt extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_no", nullable = false, unique = true, length = 50)
    private String receiptNo;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "start_receipt_time")
    private LocalDateTime startReceiptTime;

    @Column(name = "finish_receipt_time")
    private LocalDateTime finishReceiptTime;

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo; // 对应 purchase_order.order_no

    @Column(name = "delivery_no", nullable = false, length = 50)
    private String deliveryNo;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "status", nullable = false)
    private Integer status; // 1-待收货，2-部分收货，3-全部收货完成，4-缺货收货完成

    @Column(name = "receiver_emp_id", nullable = false)
    private Long receiverEmpId;

}
