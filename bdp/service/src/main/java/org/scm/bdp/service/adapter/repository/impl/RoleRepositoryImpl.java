package org.scm.bdp.service.adapter.repository.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.scm.bdp.service._share.enums.errorcode.RoleErrorCode;
import org.scm.bdp.service.adapter.infra.domain.RolePermission;
import org.scm.bdp.service.adapter.infra.jpa.RoleJpaRepository;
import org.scm.bdp.service.adapter.infra.jpa.RolePermissionJpaRepository;
import org.scm.bdp.service.adapter.infra.jpa.UserRoleJpaRepository;
import org.scm.bdp.service.domain.model.RoleAgg;
import org.scm.bdp.service.domain.repository.RoleRepository;
import org.scm.common.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoleRepositoryImpl implements RoleRepository {

    @Autowired
    private RoleJpaRepository roleJpaRepository;

    @Autowired
    private RolePermissionJpaRepository rolePermissionJpaRepository;

    @Autowired
    private UserRoleJpaRepository userRoleJpaRepository;



    @Override
    public void save(RoleAgg roleAgg) {
        roleJpaRepository.save(roleAgg.role());
    }

    @Override
    public RoleAgg findById(Long id) {
        return roleJpaRepository.findById(id)
                .map(RoleAgg::new)
                .orElseThrow(() -> new BizException(RoleErrorCode.ROLE_NOT_FOUND));
    }

    @Override
    public void deleteById(Long id) {
        // 判断角色有没有被使用, 如果被使用，则不允许删除
        userRoleJpaRepository.findByUserId(id).ifPresent(userRoles -> {
            if (CollectionUtils.isNotEmpty(userRoles)) {
                throw new BizException(RoleErrorCode.ROLE_BEEN_USED);
            }
        });

        roleJpaRepository.deleteById(id);

        rolePermissionJpaRepository.deleteByRoleId(id);
    }

    @Override
    public void checkNameDuplicate(Long roleId, String name) {
        roleJpaRepository.findByIdNotAndName(roleId, name).ifPresent(role -> {
            throw new BizException(RoleErrorCode.ROLE_NAME_DUPLICATE);
        });
    }

    @Override
    public void checkCodeDuplicate(Long roleId, String code) {
        roleJpaRepository.findByIdNotAndCode(roleId, code).ifPresent(role -> {
            throw new BizException(RoleErrorCode.ROLE_CODE_DUPLICATE);
        });
    }

    @Override
    public void checkExistById(Long id) {
        roleJpaRepository.findById(id)
                .orElseThrow(() -> new BizException(RoleErrorCode.ROLE_NOT_FOUND));
    }

    @Override
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        // 删除旧的角色权限配置
        rolePermissionJpaRepository.deleteByRoleId(roleId);

        // 如果权限id集合为空，则直接返回, 表示清除用户权限
        if (CollectionUtils.isEmpty(permissionIds)) {
            return;
        }

        // 创建新的角色权限配置
        List<RolePermission> rolePermissions = permissionIds.stream().map(permissionId -> {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            return rolePermission;
        }).collect(Collectors.toList());
        rolePermissionJpaRepository.saveAll(rolePermissions);
    }

    @Override
    public void checkNameExist(String name) {
        roleJpaRepository.findByName(name).ifPresent(role -> {
            throw new BizException(RoleErrorCode.ROLE_NAME_EXIST);
        });
    }

    @Override
    public void checkCodeExist(String code) {
        roleJpaRepository.findByCode(code).ifPresent(role -> {
            throw new BizException(RoleErrorCode.ROLE_CODE_EXIST);
        });
    }

    @Override
    public void checkExistByIds(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }

        List<RoleAgg> roleAggs = roleJpaRepository.findAllById(roleIds).stream().map(RoleAgg::new).toList();
        if (roleAggs.size() != roleIds.size()) {
            throw new BizException(RoleErrorCode.ROLE_NOT_FOUND);
        }
    }


}
