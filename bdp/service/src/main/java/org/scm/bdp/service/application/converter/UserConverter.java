package org.scm.bdp.service.application.converter;

import org.scm.bdp.service.adapter.infra.domain.User;
import org.scm.bdp.service.application.command.rbac.CreateUserCommand;
import org.scm.bdp.service.domain.model.UserAgg;

public class UserConverter {
    public static UserAgg cmdConvertAgg(CreateUserCommand cmd) {
        User user = new User();
        user.setUsername(cmd.username());
        user.setNickname(cmd.nickname());
        user.setEmail(cmd.email());
        user.setPhone(cmd.phone());
        return new UserAgg(user);
    }
}
