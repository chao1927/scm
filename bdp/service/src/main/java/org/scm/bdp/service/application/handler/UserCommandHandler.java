package org.scm.bdp.service.application.handler;

import org.scm.bdp.service.application.command.rbac.*;
import org.scm.bdp.service.application.converter.UserConverter;
import org.scm.bdp.service.domain.model.UserAgg;
import org.scm.bdp.service.domain.repository.RoleRepository;
import org.scm.bdp.service.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserCommandHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public void handle(CreateUserCommand cmd) {
        // 判断用户名, 手机号是否重复
        userRepository.checkUsernameExist(cmd.username());
        userRepository.checkPhoneExist(cmd.phone());

        // 初始化用户信息
        UserAgg userAgg = UserConverter.cmdConvertAgg(cmd);
        userAgg.init();

        // 保存用户
        userRepository.save(userAgg);
    }


    public void handle(UpdateUserCommand cmd) {
        UserAgg userAgg = userRepository.findById(cmd.id());

        // 判断用户名, 手机号是否重复
        userRepository.checkUsernameDuplicate(cmd.id(), cmd.username());
        userRepository.checkPhoneDuplicate(cmd.id(), cmd.phone());

        // 更新用户信息
        userAgg.update(cmd.username(), cmd.nickname(), cmd.phone(), cmd.email());
        userRepository.save(userAgg);
    }

    public void handle(ChangePasswordCommand cmd) {
        UserAgg userAgg = userRepository.findById(cmd.id());

        // 修改密码
        userAgg.changePassword(cmd.oldPassword(), cmd.newPassword());

        // 保存用户
        userRepository.save(userAgg);
    }

    public void handle(AssignRolesCommand cmd) {
        userRepository.checkExistById(cmd.userId());
        roleRepository.checkExistByIds(cmd.roleIds());

        userRepository.assignRolesToUser(cmd.userId(), cmd.roleIds());
    }

    public void handle(LoginCommand cmd) {
        UserAgg userAgg = userRepository.findByUsername(cmd.username());
        userAgg.login(cmd.password());

        // 登录成功, 设置登录状态
        // TODO
    }
}
