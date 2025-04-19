package org.scm.bdp.service.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.common.BaseBO;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product extends BaseBO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id; // 商品ID

    @Column(name = "sku", nullable = false, length = 50, unique = true)
    private String sku; // SKU

    @Column(name = "spu", nullable = false, length = 50)
    private String spu; // SPU

    @Column(name = "name", nullable = false, length = 255, unique = true)
    private String name; // 商品名称

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 商品描述

    @Column(name = "category_id")
    private Long categoryId; // 分类ID

    @Column(name = "unit_id")
    private Long unitId; // 单位ID

    @Column(name = "key_attributes", columnDefinition = "JSON")
    private String keyAttributes; // 关键属性（JSON格式）

    @Column(name = "sales_attributes", columnDefinition = "JSON")
    private String salesAttributes; // 销售属性（JSON格式）

    @Column(name = "reference_purchase_price", precision = 10, scale = 2)
    private BigDecimal referencePurchasePrice; // 参考采购价

    @Column(name = "reference_sales_price", precision = 10, scale = 2)
    private BigDecimal referenceSalesPrice; // 参考销售价

    @Column(name = "status", nullable = false)
    private Integer status;

    public void enable() {
        this.status = SwitchStatus.ENABLED.getValue();
    }

    public void disable() {
        this.status = SwitchStatus.DISABLED.getValue();
    }

}
