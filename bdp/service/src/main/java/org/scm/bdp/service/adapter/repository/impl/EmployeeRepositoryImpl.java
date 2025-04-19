package org.scm.bdp.service.adapter.repository.impl;

import org.scm.bdp.service.adapter.infra.domain.Employee;
import org.scm.bdp.service.adapter.infra.jpa.EmployeeJpaRepository;
import org.scm.bdp.service.domain.model.EmployeeAgg;
import org.scm.bdp.service.domain.repository.EmployeeRepository;
import org.scm.common.exception.BizException;
import org.scm.common.exception.EmployeeErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeRepositoryImpl implements EmployeeRepository {
    @Autowired
    private EmployeeJpaRepository employeeJpaRepository;
    @Override
    public void save(EmployeeAgg employeeAgg) {
        // TODO: 实现保存逻辑
    }
    @Override 
    public EmployeeAgg findById(Long id) {
        Employee employee = employeeJpaRepository.findById(id)
                .orElseThrow(() -> new BizException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        return new EmployeeAgg(employee, null, null);
    }

    @Override
    public void deleteById(Long id) {

    }
}