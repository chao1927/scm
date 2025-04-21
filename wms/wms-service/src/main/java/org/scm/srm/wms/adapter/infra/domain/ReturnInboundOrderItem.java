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
@Table(name = "return_inbound_order_item")
public class ReturnInboundOrderItem extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inbound_no", nullable = false, length = 50)
    private String inboundNo; // 对应 return_inbound_order.inbound_no

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_id", nullable = false)
    private Integer unitId;

    @Column(name = "acceptance_result", nullable = false)
    private Integer acceptanceResult; // 1-通过,2-不通过

    @Column(name = "rejection_reason", length = 200)
    private String rejectionReason;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "location_id")
    private Long locationId;

}
