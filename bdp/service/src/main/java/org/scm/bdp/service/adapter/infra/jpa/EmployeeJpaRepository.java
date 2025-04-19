package org.scm.bdp.service.adapter.infra.jpa;

import org.scm.bdp.service.adapter.infra.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeJpaRepository extends JpaRepository<Employee, Long> {
    Employee findByEmployeeNo(String employeeNo);
}
