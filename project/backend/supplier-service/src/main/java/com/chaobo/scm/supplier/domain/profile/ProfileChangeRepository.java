package com.chaobo.scm.supplier.domain.profile;

import java.util.Optional;

public interface ProfileChangeRepository {
    Optional<ProfileChangeAggregate> findById(long changeId);
    boolean existsPending(long supplierId);
    void save(ProfileChangeAggregate aggregate, long operatorId);
}
