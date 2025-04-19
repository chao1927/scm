package org.scm.bdp.service.adapter.infra.jpa;

import org.scm.bdp.service.adapter.infra.domain.EmployeeJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeJobJpaRepository extends JpaRepository<EmployeeJob, Long> {
    EmployeeJob findByEmployeeId(Long employeeId);
}
