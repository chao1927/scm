package org.scm.bdp.service.application.handler;

import org.scm.bdp.service.application.command.rbac.AssignPermissionsToRoleCommand;
import org.scm.bdp.service.application.command.rbac.DeleteRoleCommand;
import org.scm.bdp.service.application.converter.RoleConverter;
import org.scm.bdp.service.application.command.rbac.CreateRoleCommand;
import org.scm.bdp.service.application.command.rbac.UpdateRoleCommand;
import org.scm.bdp.service.domain.model.RoleAgg;
import org.scm.bdp.service.domain.repository.PermissionRepository;
import org.scm.bdp.service.domain.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleCommandHandler {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    public void handle(CreateRoleCommand cmd) {
        // 验证角色名称是否重复
        roleRepository.checkNameExist(cmd.name());

        // 验证角色code是否重复
        roleRepository.checkCodeExist(cmd.code());

        // 转换命令为角色聚合根
        RoleAgg roleagg = RoleConverter.cmdConvertAgg(cmd);

        // 保存角色
        roleRepository.save(roleagg);
    }

    public void handle(UpdateRoleCommand cmd) {
        // 验证角色是否存在
        RoleAgg roleagg = roleRepository.findById(cmd.id());

        // 验证角色名称是否重复
        roleRepository.checkNameDuplicate(cmd.id(), cmd.name());

        // 验证角色code是否重复
        roleRepository.checkCodeDuplicate(cmd.id(), cmd.code());

        // 更新角色

        roleagg.update(cmd.name(), cmd.code(), cmd.remark());

        // 保存角色
        roleRepository.save(roleagg);
    }

    public void handle(DeleteRoleCommand cmd) {
        // 验证角色是否存在
        roleRepository.checkExistById(cmd.id());

        // 删除角色
        roleRepository.deleteById(cmd.id());
    }

    public void handle(AssignPermissionsToRoleCommand cmd) {
        // 验证角色是否存在
        roleRepository.checkExistById(cmd.roleId());

        // 验证权限是否存在
        permissionRepository.checkExistByIds(cmd.permissionIds());

        // 给角色分配权限
        roleRepository.assignPermissionsToRole(cmd.roleId(), cmd.permissionIds());
    }


}
