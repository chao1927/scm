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
@Table(name = "wave_order")
public class WaveOrder extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wave_no", nullable = false, length = 50, unique = true)
    private String waveNo;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "delivery_nos", columnDefinition = "JSON")
    private String deliveryNos; // JSON 格式的发货单号列表

    @Column(name = "total_sku_count")
    private Integer totalSkuCount;

    @Column(name = "total_product_quantity")
    private Integer totalProductQuantity;

    @Column(name = "wave_status", nullable = false)
    private Integer waveStatus; // 如 1-待分拣,2-拣货中,3-拣货完成,4-波次完成

    @Column(name = "wave_priority")
    private Integer wavePriority;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

}
