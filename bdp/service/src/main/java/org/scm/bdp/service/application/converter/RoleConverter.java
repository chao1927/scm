package org.scm.bdp.service.application.converter;

import org.scm.bdp.service.adapter.infra.domain.Role;
import org.scm.bdp.service.application.command.rbac.CreateRoleCommand;
import org.scm.bdp.service.domain.model.RoleAgg;

public class RoleConverter {
    public static RoleAgg cmdConvertAgg(CreateRoleCommand cmd) {
        Role role = new Role();
        role.setName(cmd.name());
        role.setCode(cmd.code());
        role.setRemark(cmd.remark());
        return new RoleAgg(role);
    }
}
