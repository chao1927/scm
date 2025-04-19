package org.scm.oms.adapter.infra.domain;

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
@Table(name = "return_apply_item")
public class ReturnApplyItem extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_apply_no", nullable = false, length = 50)
    private String returnApplyNo; // 对应 return_apply.return_apply_no

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "unit_id", nullable = false)
    private Integer unitId;

}
