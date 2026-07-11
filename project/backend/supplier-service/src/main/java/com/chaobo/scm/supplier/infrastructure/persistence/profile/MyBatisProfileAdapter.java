package com.chaobo.scm.supplier.infrastructure.persistence.profile;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.application.profile.*;
import com.chaobo.scm.supplier.domain.profile.*;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Optional;

@Repository
public class MyBatisProfileAdapter implements ProfileChangeRepository, ProfileReadModelPort {
    private final ProfilePersistenceMapper mapper;
    private final ObjectMapper json;
    public MyBatisProfileAdapter(ProfilePersistenceMapper mapper, ObjectMapper json) { this.mapper = mapper; this.json = json; }

    @Override public Optional<ProfileChangeAggregate> findById(long id) {
        var row = mapper.findChange(id); if (row == null) return Optional.empty();
        return Optional.of(ProfileChangeAggregate.rehydrate(row.changeId(), row.changeNo(), row.supplierId(),
                row.profileVersion(), row.changeReason(), fields(row.changedFieldsJson()),
                ProfileChangeStatus.fromCode(row.changeStatus()), row.withdrawReason(), row.version()));
    }
    @Override public boolean existsPending(long supplierId) { return mapper.existsPending(supplierId); }
    @Override public void save(ProfileChangeAggregate aggregate, long operatorId) {
        var row = row(aggregate);
        if (mapper.findChange(aggregate.changeId()) == null) mapper.insert(row, operatorId);
        else if (mapper.update(row, aggregate.version() - 1, operatorId) != 1)
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "资料变更已被更新");
    }
    @Override public Optional<ProfileViews.Profile> findProfile(long supplierId) {
        var row = mapper.findProfile(supplierId); if (row == null) return Optional.empty();
        return Optional.of(new ProfileViews.Profile(row.supplierId(), row.supplierCode(), row.supplierName(),
                row.lifecycleStatus(), row.riskLevel(), row.profileJson(), row.version(), row.updatedAt()));
    }
    @Override public PageResult<ProfileViews.Change> pageChanges(long supplierId, Integer status, int pageNo, int pageSize) {
        long total = mapper.countChanges(supplierId, status);
        var rows = mapper.pageChanges(supplierId, status, (pageNo - 1) * pageSize, pageSize).stream()
                .map(this::view).toList();
        return new PageResult<>(pageNo, pageSize, total, rows);
    }
    private ProfilePersistenceMapper.ChangeRow row(ProfileChangeAggregate a) {
        return new ProfilePersistenceMapper.ChangeRow(a.changeId(), a.changeNo(), a.supplierId(), a.profileVersion(),
                a.reason(), json.writeValueAsString(a.changes()), a.status().code(), a.withdrawReason(), a.version(), null);
    }
    private ProfileViews.Change view(ProfilePersistenceMapper.ChangeRow row) {
        var status = ProfileChangeStatus.fromCode(row.changeStatus());
        return new ProfileViews.Change(row.changeId(), row.changeNo(), row.supplierId(), status.code(), status.label(),
                row.changeReason(), row.withdrawReason(), row.profileVersion(), row.version(), row.createdAt(), fields(row.changedFieldsJson()));
    }
    private java.util.List<ProfileFieldChange> fields(String value) {
        try { return Arrays.asList(json.readValue(value, ProfileFieldChange[].class)); }
        catch (RuntimeException exception) { throw new IllegalStateException("资料变更字段解析失败", exception); }
    }
}
