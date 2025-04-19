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
@Table(name = "address")
public class Address extends BaseBO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id; // 地址ID

    @Column(name = "province", length = 100)
    private String province; // 省份

    @Column(name = "city", length = 100)
    private String city; // 城市

    @Column(name = "district", length = 100)
    private String district; // 区县

    @Column(name = "detailed_address", columnDefinition = "TEXT")
    private String detailedAddress; // 详细地址

    @Column(name = "zip_code", length = 20)
    private String zipCode; // 邮编

    @Column(name = "longitude", precision = 10, scale = 8)
    private BigDecimal longitude; // 经度

    @Column(name = "latitude", precision = 11, scale = 8)
    private BigDecimal latitude; // 纬度


}
