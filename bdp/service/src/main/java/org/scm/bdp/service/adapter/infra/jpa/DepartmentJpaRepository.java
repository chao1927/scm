package org.scm.bdp.service.adapter.infra.jpa;

import org.scm.bdp.service.adapter.infra.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentJpaRepository extends JpaRepository<Department, Long> {
    Department findByCode(String code);
}
