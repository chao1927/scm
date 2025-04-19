package org.scm.bdp.service.adapter.infra.domain;

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
@Table(name = "logistics_channel")
public class LogisticsChannel extends BaseBO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id; // 渠道ID

    @Column(name = "name", nullable = false, length = 40, unique = true)
    private String name; // 渠道名称

    @Column(name = "service_type", nullable = false)
    private Integer serviceType; // 服务类型：1（快递）/2（快运）/3（整车）

    @Column(name = "coverage_area", columnDefinition = "JSON")
    private String coverageArea; // 覆盖区域（JSON格式）

    @Column(name = "freight_calculation_rules", columnDefinition = "JSON")
    private String freightCalculationRules; // 运费计算规则（JSON格式）

    @Column(name = "status", nullable = false)
    private Integer status;

}
