package com.chaobo.scm.supplier.application.profile;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.profile.*;
import com.chaobo.scm.supplier.domain.shared.DomainEvent;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProfileApplicationService {
    private final ProfileChangeRepository repository;
    private final ProfileReadModelPort readModel;
    private final OutboxRepository outbox;
    private final AuditLogRepository audit;
    private final IdentifierGenerator ids;
    private final TransactionalCommandExecutor executor;

    public ProfileApplicationService(ProfileChangeRepository repository, ProfileReadModelPort readModel,
                                     OutboxRepository outbox, AuditLogRepository audit,
                                     IdentifierGenerator ids, TransactionalCommandExecutor executor) {
        this.repository = repository; this.readModel = readModel; this.outbox = outbox;
        this.audit = audit; this.ids = ids; this.executor = executor;
    }

    @Transactional(readOnly = true)
    public ProfileViews.Profile profile(long supplierId, Long scopeId) {
        requireScope(supplierId, scopeId);
        return readModel.findProfile(supplierId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供应商档案不存在"));
    }

    @Transactional(readOnly = true)
    public PageResult<ProfileViews.Change> changes(long supplierId, Long scopeId, Integer status,
                                                   int pageNo, int pageSize) {
        requireScope(supplierId, scopeId);
        validatePage(pageNo, pageSize);
        return readModel.pageChanges(supplierId, status, pageNo, pageSize);
    }

    @Transactional
    public CommandResult submit(long supplierId, int profileVersion, String reason,
                                List<ProfileFieldChange> changes, CommandContext context) {
        context.requirePermission("supplier:supplier_profile:change"); context.requireSupplierScope(supplierId);
        record Request(long supplierId, int profileVersion, String reason, List<ProfileFieldChange> changes) {}
        var request = new Request(supplierId, profileVersion, reason, changes);
        return executor.execute("supplier:profile", context, request, () -> {
            var profile = readModel.findProfile(supplierId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供应商档案不存在"));
            if (profile.version() != profileVersion) throw new BusinessException(ErrorCode.VERSION_CONFLICT, "档案版本已变化");
            if (repository.existsPending(supplierId)) throw new BusinessException(ErrorCode.STATE_CONFLICT, "已有待审批资料变更");
            var aggregate = ProfileChangeAggregate.submit(supplierId, profileVersion, reason, changes,
                    context.operatorId(), ids);
            return persist(aggregate, context, "SUBMIT_PROFILE_CHANGE", null);
        });
    }

    @Transactional
    public CommandResult withdraw(long changeId, int version, String reason, CommandContext context) {
        context.requirePermission("supplier:supplier_profile:change");
        record Request(long changeId, int version, String reason) {}
        var request = new Request(changeId, version, reason);
        return executor.execute("supplier:profile", context, request, () -> {
            var aggregate = repository.findById(changeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "资料变更不存在"));
            context.requireSupplierScope(aggregate.supplierId());
            if (aggregate.version() != version) throw new BusinessException(ErrorCode.VERSION_CONFLICT, "资料变更版本已变化");
            String before = snapshot(aggregate);
            aggregate.withdraw(reason, context.operatorId(), ids);
            return persist(aggregate, context, "WITHDRAW_PROFILE_CHANGE", before);
        });
    }

    private CommandResult persist(ProfileChangeAggregate aggregate, CommandContext context,
                                  String operation, String before) {
        repository.save(aggregate, context.operatorId());
        List<DomainEvent> events = aggregate.pullEvents(); outbox.saveAll(events);
        audit.save(context, operation, "SUPPLIER_PROFILE_CHANGE", aggregate.changeId(), aggregate.changeNo(),
                before, snapshot(aggregate));
        String event = events.isEmpty() ? null : events.get(events.size() - 1).eventCode();
        return new CommandResult(aggregate.changeId(), aggregate.changeNo(), aggregate.status().code(),
                aggregate.status().label(), aggregate.version(), event, false);
    }

    private String snapshot(ProfileChangeAggregate aggregate) {
        return "{\"changeNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(
                aggregate.changeNo(), aggregate.status().code(), aggregate.version());
    }
    private void requireScope(long supplierId, Long scopeId) {
        if (scopeId != null && scopeId != supplierId) throw new BusinessException(ErrorCode.NOT_FOUND, "供应商档案不存在");
    }
    private void validatePage(int pageNo, int pageSize) {
        if (pageNo < 1 || pageSize < 1 || pageSize > 100)
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
    }
}
