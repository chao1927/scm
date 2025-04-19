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
@Table(name = "position")
public class Position extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "code", nullable = false, length = 20, unique = true)
    private String code;

    @Column(name = "department_id", nullable = false)
    private Long departmentId; // 关联 department.id

    @Column(name = "grade_type", nullable = false)
    private Integer gradeType; // 1-管理序列，2-技术序列，3-专业序列

    @Column(name = "grade_level", length = 10)
    private String gradeLevel; // 如 M2 或 P3

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false)
    private Integer status; // 1-启用,2-停用

}
