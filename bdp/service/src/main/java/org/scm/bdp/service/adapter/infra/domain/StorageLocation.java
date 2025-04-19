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
@Table(name = "storage_location")
public class StorageLocation extends BaseBO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id; // 库位ID

    @Column(name = "warehouse_id")
    private Long warehouseId; // 仓库ID

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code; // 库位编码

    @Column(name = "max_volume", precision = 10, scale = 2)
    private BigDecimal maxVolume; // 最大容积（立方米）

    @Column(name = "max_weight", precision = 10, scale = 2)
    private BigDecimal maxWeight; // 最大承重（千克）

    @Column(name = "mixing_strategy", nullable = false)
    private Integer mixingStrategy; // 混放策略：1（允许批次混放）/2（允许SKU混放）/3（禁止混放）

    @Column(name = "status", nullable = false)
    private Integer status;

    public void enable() {
        this.status = SwitchStatus.ENABLED.getValue();
    }

    public void disable() {
        this.status = SwitchStatus.DISABLED.getValue();
    }


}
