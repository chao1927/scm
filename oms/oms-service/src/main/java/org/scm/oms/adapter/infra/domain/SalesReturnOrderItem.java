package org.scm.oms.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scm.common.BaseBO;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sales_return_order_item")
public class SalesReturnOrderItem extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sales_return_no", nullable = false, length = 50)
    private String salesReturnNo; // 对应 sales_return_order.sales_return_no

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "unit_id", nullable = false)
    private Integer unitId;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "location_id")
    private Long locationId;

}
