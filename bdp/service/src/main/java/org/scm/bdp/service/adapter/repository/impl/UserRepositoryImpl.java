package org.scm.bdp.service.adapter.repository.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.scm.bdp.service._share.enums.errorcode.UserErrorCode;
import org.scm.bdp.service.adapter.infra.domain.UserRole;
import org.scm.bdp.service.adapter.infra.jpa.UserJpaRepository;
import org.scm.bdp.service.adapter.infra.jpa.UserRoleJpaRepository;
import org.scm.bdp.service.domain.model.UserAgg;
import org.scm.bdp.service.domain.repository.UserRepository;
import org.scm.common.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private UserRoleJpaRepository userRoleJpaRepository;


    @Override
    public UserAgg findById(Long id) {
        return userJpaRepository.findById(id)
                .map(UserAgg::new)
                .orElseThrow(() -> new BizException(UserErrorCode.USER_NOT_FOUND));
    }

    @Override
    public void deleteById(Long id) {
        // 删除用户角色关联
        userRoleJpaRepository.deleteByUserId(id);

        // 删除用户
        userJpaRepository.deleteById(id);
    }

    @Override
    public UserAgg findByUsername(String username) {
        return userJpaRepository.findByUsername(username)
                .map(UserAgg::new)
                .orElseThrow(() -> new BizException(UserErrorCode.USER_NOT_FOUND));
    }

    @Override
    public void save(UserAgg user) {
        userJpaRepository.save(user.user());
    }

    @Override
    public void checkUsernameDuplicate(Long userId, String username) {
        userJpaRepository.findByIdNotAndUsername(userId, username)
                .ifPresent(user -> {
                    throw new BizException(UserErrorCode.USER_NAME_DUPLICATE);
                });
    }

    @Override
    public void checkPhoneDuplicate(Long userId, String phone) {
        userJpaRepository.findByIdNotAndPhone(userId, phone).ifPresent(user -> {
            throw new BizException(UserErrorCode.USER_PHONE_DUPLICATE);
        });
    }

    @Override
    public void checkUsernameExist(String username) {
        userJpaRepository.findByUsername(username)
            .ifPresent(user -> {
                throw new BizException(UserErrorCode.USER_NAME_EXIST);
            });
    }

    @Override
    public void checkPhoneExist(String phone) {
        userJpaRepository.findByPhone(phone)
            .ifPresent(user -> {
                throw new BizException(UserErrorCode.USER_PHONE_EXIST);
            });
    }

    @Override
    public void checkExistById(Long userId) {
        userJpaRepository.findById(userId)
            .orElseThrow(() -> new BizException(UserErrorCode.USER_NOT_FOUND));
    }

    @Override
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        userRoleJpaRepository.deleteByUserId(userId);
        if (CollectionUtils.isEmpty(roleIds)) return;

        List<UserRole> userRoles = roleIds.stream().map(roleId -> {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            return userRole;
        }).toList();
        userRoleJpaRepository.saveAll(userRoles);
    }
}
