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
@Table(name = "supplier")
public class Supplier extends BaseBO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id; // 供应商ID

    @Column(name = "name", nullable = false, length = 255, unique = true)
    private String name; // 供应商名称

    @Column(name = "category_id")
    private Long categoryId; // 供应商分类ID

    @Column(name = "contact_person", length = 100)
    private String contactPerson; // 联系人

    @Column(name = "contact_phone", length = 20)
    private String contactPhone; // 联系电话

    @Column(name = "address", columnDefinition = "TEXT")
    private String address; // 地址

    @Column(name = "business_license_number", length = 50)
    private String businessLicenseNumber; // 营业执照编号

    @Column(name = "business_license_photo", length = 255)
    private String businessLicensePhoto; // 营业执照照片

    @Column(name = "organization_code", length = 50)
    private String organizationCode; // 统一组织代码

    @Column(name = "performance_score", precision = 3, scale = 2)
    private BigDecimal performanceScore; // 绩效评分

    @Column(name = "status", nullable = false)
    private Integer status;


}
