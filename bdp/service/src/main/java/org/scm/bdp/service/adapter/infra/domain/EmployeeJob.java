package org.scm.bdp.service.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.scm.common.BaseBO;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_job")
public class EmployeeJob extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;  // 关联 employee.id

    @Column(name = "position_id", nullable = false)
    private Long positionId;  // 关联 position.id

    @Column(name = "job_level", length = 20)
    private String jobLevel; // 如 "P6"、"M2"

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;

    @Column(name = "regular_date")
    private LocalDate regularDate;

}
