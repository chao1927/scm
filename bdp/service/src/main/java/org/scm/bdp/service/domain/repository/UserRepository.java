package org.scm.bdp.service.domain.repository;

import org.scm.bdp.service.domain.model.UserAgg;
import org.scm.common.BaseRepository;

import java.util.List;

public interface UserRepository extends BaseRepository<UserAgg> {
    UserAgg findById(Long id);
    UserAgg findByUsername(String username);
    void save(UserAgg user);

    void checkUsernameDuplicate(Long userId, String username);

    void checkPhoneDuplicate(Long userId, String phone);

    void checkUsernameExist(String username);

    void checkPhoneExist(String phone);

    void checkExistById(Long userId);

    void assignRolesToUser(Long userId, List<Long> roleIds);
}
