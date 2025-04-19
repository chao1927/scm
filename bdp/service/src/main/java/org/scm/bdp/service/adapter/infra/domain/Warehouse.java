package org.scm.bdp.service.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.scm.common.BaseBO;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "warehouse")
public class Warehouse extends BaseBO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id; // 仓库ID

    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name; // 仓库名称

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 参考描述

    @Column(name = "address", columnDefinition = "TEXT")
    private String address; // 地址

    @Column(name = "type", nullable = false)
    private Integer type; // 仓库类型：1（国内仓）/2（海外仓）/3（冷藏仓）/4（保税仓）

    @Column(name = "area", precision = 10, scale = 2)
    private BigDecimal area; // 仓库面积

    @Column(name = "manager", length = 100)
    private String manager; // 仓库管理员

    @Column(name = "manager_phone", length = 20)
    private String managerPhone; // 仓库管理员联系电话

    @Column(name = "status", nullable = false)
    private Integer status;


}
