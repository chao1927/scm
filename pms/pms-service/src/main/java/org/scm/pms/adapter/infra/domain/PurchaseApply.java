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
@Table(name = "purchase_apply")
public class PurchaseApply extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apply_no", nullable = false, unique = true, length = 50)
    private String applyNo;

    @Column(name = "apply_time", nullable = false)
    private LocalDateTime applyTime;

    @Column(name = "apply_emp_id", nullable = false)
    private Long applyEmpId;

    @Column(name = "purchaser_emp_id", nullable = false)
    private Long purchaserEmpId;

    @Column(name = "estimated_total_price", precision = 10, scale = 2)
    private BigDecimal estimatedTotalPrice;

    @Column(name = "status", nullable = false)
    private Integer status; // 1-待提交，2-待审核，3-审核通过，4-审核拒绝

}
