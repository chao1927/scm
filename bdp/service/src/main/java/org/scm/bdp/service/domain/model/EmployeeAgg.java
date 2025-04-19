package org.scm.bdp.service.domain.model;

import lombok.AllArgsConstructor;
import org.scm.bdp.service.adapter.infra.domain.Department;
import org.scm.bdp.service.adapter.infra.domain.Employee;
import org.scm.bdp.service.adapter.infra.domain.Position;

@AllArgsConstructor
public record EmployeeAgg(Employee employee, Department department, Position position) {


    public Long id() {
        return employee.getId();
    }


}
