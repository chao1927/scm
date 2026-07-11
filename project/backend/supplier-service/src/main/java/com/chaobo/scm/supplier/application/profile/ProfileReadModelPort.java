package com.chaobo.scm.supplier.application.profile;

import com.chaobo.scm.common.api.PageResult;
import java.util.Optional;

public interface ProfileReadModelPort {
    Optional<ProfileViews.Profile> findProfile(long supplierId);
    PageResult<ProfileViews.Change> pageChanges(long supplierId, Integer status, int pageNo, int pageSize);
}
