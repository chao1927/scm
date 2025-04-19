package org.scm.srm.wms.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scm.common.BaseBO;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "return_inbound_order")
public class ReturnInboundOrder extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inbound_no", nullable = false, length = 50, unique = true)
    private String inboundNo;

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo; // 关联原订单号（销售出库单对应的订单号）

    @Column(name = "sales_return_no", nullable = false, length = 50)
    private String salesReturnNo; // 关联销售退货单号

    @Column(name = "operator_id", nullable = false)
    private Long operatorId; // 验收操作人

    @Column(name = "refund_reason", length = 200)
    private String refundReason;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "logistics_no", length = 50)
    private String logisticsNo;

    @Column(name = "inbound_time")
    private LocalDateTime inboundTime;

    @Column(name = "shelving_time")
    private LocalDateTime shelvingTime;

    @Column(name = "inbound_status", nullable = false)
    private Integer inboundStatus; // 1-等待物流送货,2-收货中,3-验收中,4-验收完成,5-上架中,6-入库完成

}
