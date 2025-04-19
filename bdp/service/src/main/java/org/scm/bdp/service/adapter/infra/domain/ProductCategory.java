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
@Table(name = "product_category")
public class ProductCategory extends BaseBO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id; // 分类ID

    @Column(name = "name", nullable = false, length = 40, unique = true)
    private String name; // 分类名称

    @Column(name = "parent_id")
    private Long parentId; // 父分类ID

    @Column(name = "attributes", columnDefinition = "JSON")
    private String attributes; // 分类属性（JSON格式）

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "sort_order")
    private Integer sortOrder; // 排序序号

}
