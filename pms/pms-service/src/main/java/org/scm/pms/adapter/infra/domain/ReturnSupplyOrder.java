package org.scm.pms.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.scm.common.BaseBO;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "return_supply_order")
public class ReturnSupplyOrder extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, length = 50, unique = true)
    private String orderNo; // 退供单单号

    @Column(name = "apply_no", nullable = false, length = 50)
    private String applyNo; // 退供申请单号

    @Column(name = "supplier_id", nullable = false)
    private Integer supplierId;

    @Column(name = "total_item_types")
    private Integer totalItemTypes;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "initiate_time", nullable = false)
    private LocalDateTime initiateTime;

    @Column(name = "status", nullable = false)
    private Integer status; // 如：1-待确认，2-已确认，3-已取消

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;




}
