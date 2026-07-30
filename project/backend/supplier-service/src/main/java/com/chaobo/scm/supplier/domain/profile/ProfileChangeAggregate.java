package com.chaobo.scm.supplier.domain.profile;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.domain.shared.DomainEvent;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ProfileChangeAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class ProfileChangeAggregate {

    /**
     * changeId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long changeId;

    /**
     * changeNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String changeNo;

    /**
     * supplierId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long supplierId;

    /**
     * profileVersion（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private final int profileVersion;

    /**
     * reason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String reason;

    /**
     * changes（类型：{@code List<ProfileFieldChange>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<ProfileFieldChange> changes;

    /**
     * events（类型：{@code List<DomainEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * status（类型：{@code ProfileChangeStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private ProfileChangeStatus status;

    /**
     * withdrawReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String withdrawReason;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * 创建 ProfileChangeAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param changeId 业务或技术标识，类型为 {@code long}
     * @param changeNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param profileVersion 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param changes 业务处理参数或成员，类型为 {@code List<ProfileFieldChange>}
     * @param status 生命周期状态，类型为 {@code ProfileChangeStatus}
     * @param withdrawReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    private ProfileChangeAggregate(long changeId, String changeNo, long supplierId, int profileVersion, String reason, List<ProfileFieldChange> changes, ProfileChangeStatus status, String withdrawReason, int version) {
        this.changeId = changeId;
        this.changeNo = changeNo;
        this.supplierId = supplierId;
        this.profileVersion = profileVersion;
        this.reason = reason;
        this.changes = List.copyOf(changes);
        this.status = status;
        this.withdrawReason = withdrawReason;
        this.version = version;
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param profileVersion 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param changes 业务处理参数或成员，类型为 {@code List<ProfileFieldChange>}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param generator 业务处理参数或成员，类型为 {@code IdentifierGenerator}
     * @return 执行命令的结果，类型为 {@code ProfileChangeAggregate}
     */
    public static ProfileChangeAggregate submit(long supplierId, int profileVersion, String reason, List<ProfileFieldChange> changes, long operatorId, IdentifierGenerator generator) {
        if (supplierId <= 0 || profileVersion < 0) {
            throw rule("供应商和档案版本不合法");
        }
        if (reason == null || reason.isBlank()) {
            throw rule("资料变更原因不能为空");
        }
        if (changes == null || changes.isEmpty()) {
            throw rule("至少提交一个变更字段");
        }
        if (changes.stream().map(ProfileFieldChange::fieldCode).distinct().count() != changes.size()) {
            throw rule("同一字段不能重复提交");
        }
        long id = generator.nextId();
        var aggregate = new ProfileChangeAggregate(id, generator.nextBusinessNo("SPC"), supplierId, profileVersion, reason.trim(), changes, ProfileChangeStatus.PENDING, null, 0);
        aggregate.raise(generator, "SupplierProfileChangeSubmitted", "供应商资料变更已提交", operatorId, Map.of("supplierId", supplierId, "profileVersion", profileVersion, "changedFields", changes.stream().map(ProfileFieldChange::fieldCode).toList()));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code rehydrate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param profileVersion 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param changes 业务处理参数或成员，类型为 {@code List<ProfileFieldChange>}
     * @param status 生命周期状态，类型为 {@code ProfileChangeStatus}
     * @param withdrawReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ProfileChangeAggregate}
     */
    public static ProfileChangeAggregate rehydrate(long id, String no, long supplierId, int profileVersion, String reason, List<ProfileFieldChange> changes, ProfileChangeStatus status, String withdrawReason, int version) {
        return new ProfileChangeAggregate(id, no, supplierId, profileVersion, reason, changes, status, withdrawReason, version);
    }

    /**
     * 处理当前类型职责中的操作 {@code withdraw}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param generator 业务处理参数或成员，类型为 {@code IdentifierGenerator}
     */
    public void withdraw(String reason, long operatorId, IdentifierGenerator generator) {
        if (status != ProfileChangeStatus.PENDING) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "只有待审批资料变更可以撤回");
        }
        if (reason == null || reason.isBlank()) {
            throw rule("撤回原因不能为空");
        }
        status = ProfileChangeStatus.WITHDRAWN;
        withdrawReason = reason.trim();
        version++;
        raise(generator, "SupplierProfileChangeWithdrawn", "供应商资料变更已撤回", operatorId, Map.of("supplierId", supplierId, "withdrawReason", withdrawReason));
    }

    /**
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param generator 业务处理参数或成员，类型为 {@code IdentifierGenerator}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param payload 业务处理参数或成员，类型为 {@code Map<String,Object>}
     */
    private void raise(IdentifierGenerator generator, String type, String name, long operatorId, Map<String, Object> payload) {
        long eventId = generator.nextId();
        events.add(new DomainEvent(eventId, "SUP-" + eventId, type, name, "SUPPLIER_PROFILE_CHANGE", changeId, changeNo, version, operatorId, OffsetDateTime.now(), payload));
    }

    /**
     * 处理当前类型职责中的操作 {@code rule}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param message 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private static BusinessException rule(String message) {
        return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, message);
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<DomainEvent>}
     */
    public List<DomainEvent> pullEvents() {
        var copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    /**
     * 处理当前类型职责中的操作 {@code changeId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long changeId() {
        return changeId;
    }

    /**
     * 处理当前类型职责中的操作 {@code changeNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String changeNo() {
        return changeNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code supplierId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long supplierId() {
        return supplierId;
    }

    /**
     * 处理当前类型职责中的操作 {@code profileVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int profileVersion() {
        return profileVersion;
    }

    /**
     * 处理当前类型职责中的操作 {@code reason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String reason() {
        return reason;
    }

    /**
     * 处理当前类型职责中的操作 {@code changes}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<ProfileFieldChange>}
     */
    public List<ProfileFieldChange> changes() {
        return changes;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ProfileChangeStatus}
     */
    public ProfileChangeStatus status() {
        return status;
    }

    /**
     * 处理当前类型职责中的操作 {@code withdrawReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String withdrawReason() {
        return withdrawReason;
    }

    /**
     * 处理当前类型职责中的操作 {@code version}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int version() {
        return version;
    }
}
