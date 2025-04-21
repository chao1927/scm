package org.scm.srm.wms.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.scm.common.BaseBO;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sales_outbound_order_item")
public class SalesOutboundOrderItem extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outbound_no", nullable = false, length = 50)
    private String outboundNo;  // 对应 sales_outbound_order.outbound_no

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "order_quantity", nullable = false)
    private Integer orderQuantity;

    @Column(name = "shipped_quantity")
    private Integer shippedQuantity;

    @Column(name = "unit_id", nullable = false)
    private Integer unitId;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

}
