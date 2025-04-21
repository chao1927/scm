package org.scm.bdp.service.domain.model;

import org.scm.bdp.service.adapter.infra.domain.Role;

public record RoleAgg(Role role) {
    public Long id() {
        return role.getId();
    }

    public void update(String name, String code, String remark) {
        role.setName(name);
        role.setCode(code);
        role.setRemark(remark);
    }
}
