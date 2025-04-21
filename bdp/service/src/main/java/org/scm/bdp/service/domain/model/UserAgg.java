package org.scm.bdp.service.domain.model;

import org.apache.commons.lang3.StringUtils;
import org.scm.bdp.service._share.enums.UserStatus;
import org.scm.bdp.service._share.enums.errorcode.UserErrorCode;
import org.scm.bdp.service.adapter.infra.domain.User;
import org.scm.common.exception.BizException;

public record UserAgg(User user) {
    public Long id() {
        return user.getId();
    }

    public void init() {
        user.setStatus(UserStatus.ENABLED.getCode());
        user.setPassword("123456");
    }

    public void update(String username, String nickname, String phone, String email) {
        user.setUsername(username);
        user.setNickname(nickname);
        user.setPhone(phone);
        user.setEmail(email);
    }

    public void changePassword(String oldPassword, String newPassword) {
        if (user.getPassword().equals(oldPassword)) {
            user.setPassword(newPassword);
        }
        throw new BizException(UserErrorCode.USER_PASSWORD_ERROR);
    }

    public void login(String password) {
        if (!StringUtils.equals(user.getPassword(), password)) {
            throw new BizException(UserErrorCode.USER_PASSWORD_ERROR);
        }
    }
}
