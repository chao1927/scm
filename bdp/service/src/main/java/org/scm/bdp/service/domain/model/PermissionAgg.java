package org.scm.bdp.service.domain.model;

import org.scm.bdp.service.adapter.infra.domain.Permission;

public record PermissionAgg(Permission permission) {
    public Long id() {
        return permission.getId();
    }

    public void update(String name, String code, String path, String method, String description, Long parentId) {
        permission.setName(name);
        permission.setCode(code);
        permission.setPath(path);
        permission.setMethod(method);
        permission.setDescription(description);
        permission.setParentId(parentId);
    }
}
