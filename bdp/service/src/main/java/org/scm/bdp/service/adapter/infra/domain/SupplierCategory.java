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
@Table(name = "supplier_category")
public class SupplierCategory extends BaseBO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id; // 分类ID

    @Column(name = "name", nullable = false, length = 20, unique = true)
    private String name; // 分类名称

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 描述

}
