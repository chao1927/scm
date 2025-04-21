package org.scm.bdp.service.application.handler;

import org.scm.bdp.service._share.enums.PermissionMethod;
import org.scm.bdp.service.application.command.rbac.CreatePermissionCommand;
import org.scm.bdp.service.application.command.rbac.DeletePermissionCommand;
import org.scm.bdp.service.application.command.rbac.UpdatePermissionCommand;
import org.scm.bdp.service.application.converter.PermissionConverter;
import org.scm.bdp.service.domain.model.PermissionAgg;
import org.scm.bdp.service.domain.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PermissionCommandHandler {

    @Autowired
    private PermissionRepository permissionRepository;


    /**
     * 创建权限
     * @param command 创建权限命令
     */
    public void handle(CreatePermissionCommand command) {
        // 校验权限方法 和 权限名称和权限code是否重复
        PermissionMethod.checkMethod(command.method());
        permissionRepository.checkNameExist(command.name());
        permissionRepository.checkCodeExist(command.code());

        // 转换命令为聚合根
        PermissionAgg permissionAgg = PermissionConverter.cmdConvertAgg(command);

        // 保存权限
        permissionRepository.save(permissionAgg);
    }

    public void handle(UpdatePermissionCommand command) {
        // 根据权限id校验并查询权限
        PermissionAgg permissionAgg = permissionRepository.findById(command.id());
        // 根据父权限id校验父权限是否存在
        permissionRepository.checkExistById(command.parentId());

        // 校验权限方法和 权限名称和权限code是否重复
        PermissionMethod.checkMethod(command.method());
        permissionRepository.checkNameDuplicate(command.id(), command.name());
        permissionRepository.checkCodeDuplicate(command.id(), command.code());

        // 更新权限
        permissionAgg.update(command.name(), command.code(), command.path(), command.method(), command.description(), command.parentId());
        permissionRepository.save(permissionAgg);
    }

    public void handle(DeletePermissionCommand command) {
        // 根据权限id校验权限是否存在
        permissionRepository.checkExistById(command.id());
        // 删除权限
        permissionRepository.deleteById(command.id());
    }



}
