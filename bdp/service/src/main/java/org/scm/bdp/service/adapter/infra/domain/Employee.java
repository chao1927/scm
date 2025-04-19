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
@Table(name = "employee")
public class Employee extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_no", nullable = false, unique = true, length = 20)
    private String employeeNo;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "gender")
    private Integer gender; // 0-未知,1-男,2-女

    @Column(name = "id_card", nullable = false, length = 20)
    private String idCard;

    @Column(name = "mobile", length = 20)
    private String mobile;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "status", nullable = false)
    private Integer status; // 1-在职,2-离职,3-停薪留职

}
