package org.scm.oms.adapter.infra.domain;

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
@Table(name = "delivery_order_item")
public class DeliveryOrderItem extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_no", nullable = false, length = 50)
    private String deliveryNo;  // 对应 delivery_order.delivery_no

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

}
