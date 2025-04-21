package org.scm.bdp.service.adapter.repository.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.scm.bdp.service._share.enums.errorcode.PermissionErrorCode;
import org.scm.bdp.service.adapter.infra.domain.Permission;
import org.scm.bdp.service.adapter.infra.jpa.PermissionJpaRepository;
import org.scm.bdp.service.adapter.infra.jpa.RolePermissionJpaRepository;
import org.scm.bdp.service.domain.model.PermissionAgg;
import org.scm.bdp.service.domain.repository.PermissionRepository;
import org.scm.common.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PermissionRepositoryImpl implements PermissionRepository {


    @Autowired
    private PermissionJpaRepository permissionJpaRepository;

    @Autowired
    private RolePermissionJpaRepository rolePermissionJpaRepository;

    @Override
    public void checkNameDuplicate(Long permissionId, String name) {
        // 1.检查权限名称是否重复
        permissionJpaRepository.findByIdNotAndName(permissionId, name)
                .ifPresent(user -> {
                    throw new BizException(PermissionErrorCode.PERMISSION_NAME_DUPLICATE);
                });
    }

    @Override
    public void checkCodeDuplicate(Long permissionId, String code) {
        // 1.检查权限code是否重复
        permissionJpaRepository.findByIdNotAndCode(permissionId, code)
                .ifPresent(user -> {
                    throw new BizException(PermissionErrorCode.PERMISSION_CODE_DUPLICATE);
                });
    }

    @Override
    public void checkExistById(Long id) {
        permissionJpaRepository.findById(id).orElseThrow(() -> new BizException(PermissionErrorCode.PERMISSION_NOT_FOUND));
    }

    @Override
    public void checkExistByIds(List<Long> permissionIds) {
        // 如果角色权限id集合为空，则直接返回
        if (CollectionUtils.isEmpty(permissionIds)) {
            return;
        }

        List<Permission> permissions = permissionJpaRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new BizException(PermissionErrorCode.PERMISSION_NOT_FOUND);
        }
    }

    @Override
    public void checkNameExist(String name) {
        permissionJpaRepository.findByName(name)
                .ifPresent(user -> {
                    throw new BizException(PermissionErrorCode.PERMISSION_NAME_EXIST);
                });
    }

    @Override
    public void checkCodeExist(String code) {
        permissionJpaRepository.findByCode(code)
                .ifPresent(user -> {
                    throw new BizException(PermissionErrorCode.PERMISSION_CODE_EXIST);
                });
    }

    @Override
    public void save(PermissionAgg permissionAgg) {
        permissionJpaRepository.save(permissionAgg.permission());
    }

    @Override
    public PermissionAgg findById(Long id) {
        return permissionJpaRepository.findById(id)
                .map(PermissionAgg::new)
                .orElseThrow(() -> new BizException(PermissionErrorCode.PERMISSION_NOT_FOUND));
    }

    @Override
    public void deleteById(Long id) {
        // 校验权限是否被使用, 如果被使用，则不允许删除
        rolePermissionJpaRepository.findByPermissionId(id)
                .orElseThrow(() -> new BizException(PermissionErrorCode.PERMISSION_BEEN_USED));

        permissionJpaRepository.deleteById(id);
    }
}
