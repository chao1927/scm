package org.scm.bdp.service.application.converter;

import org.scm.bdp.service.adapter.infra.domain.Permission;
import org.scm.bdp.service.application.command.rbac.CreatePermissionCommand;
import org.scm.bdp.service.domain.model.PermissionAgg;

public class PermissionConverter {

    public static PermissionAgg cmdConvertAgg(CreatePermissionCommand cmd) {
        Permission permission = new Permission();
        permission.setName(cmd.name());
        permission.setCode(cmd.code());
        permission.setPath(cmd.path());
        permission.setMethod(cmd.method());
        permission.setDescription(cmd.description());
        permission.setParentId(cmd.parentId());

        return new PermissionAgg(permission);
    }


}
