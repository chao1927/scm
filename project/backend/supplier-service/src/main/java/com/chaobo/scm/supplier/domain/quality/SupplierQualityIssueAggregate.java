package com.chaobo.scm.supplier.domain.quality;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.domain.shared.*;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * SupplierQualityIssueAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class SupplierQualityIssueAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * no（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String no;

    /**
     * supplierId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long supplierId;

    /**
     * sourceType、sourceNo、issueType、description（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String sourceType, sourceNo, issueType, description;

    /**
     * severity（类型：{@code int}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final int severity;

    /**
     * status（类型：{@code QualityIssueStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private QualityIssueStatus status;

    /**
     * deadline（类型：{@code OffsetDateTime}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private OffsetDateTime deadline;

    /**
     * plan、verification（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String plan, verification;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * events（类型：{@code List<DomainEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * 创建 SupplierQualityIssueAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param issueType 业务处理参数或成员，类型为 {@code String}
     * @param severity 业务处理参数或成员，类型为 {@code int}
     * @param description 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code QualityIssueStatus}
     * @param deadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param plan 业务处理参数或成员，类型为 {@code String}
     * @param verification 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    private SupplierQualityIssueAggregate(long id, String no, long supplierId, String sourceType, String sourceNo, String issueType, int severity, String description, QualityIssueStatus status, OffsetDateTime deadline, String plan, String verification, int version) {
        this.id = id;
        this.no = no;
        this.supplierId = supplierId;
        this.sourceType = sourceType;
        this.sourceNo = sourceNo;
        this.issueType = issueType;
        this.severity = severity;
        this.description = description;
        this.status = status;
        this.deadline = deadline;
        this.plan = plan;
        this.verification = verification;
        this.version = version;
        validate();
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param issueType 业务处理参数或成员，类型为 {@code String}
     * @param severity 业务处理参数或成员，类型为 {@code int}
     * @param description 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 执行命令的结果，类型为 {@code SupplierQualityIssueAggregate}
     */
    public static SupplierQualityIssueAggregate create(long supplierId, String sourceType, String sourceNo, String issueType, int severity, String description, long operator, IdentifierGenerator ids) {
        var result = new SupplierQualityIssueAggregate(ids.nextId(), ids.nextBusinessNo("QI"), supplierId, sourceType, sourceNo, issueType, severity, description, QualityIssueStatus.OPEN, null, null, null, 0);
        result.raise(ids, "SupplierQualityIssueCreated", "供应商质量问题已创建", operator);
        return result;
    }

    /**
     * 处理当前类型职责中的操作 {@code rehydrate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param issueType 业务处理参数或成员，类型为 {@code String}
     * @param severity 业务处理参数或成员，类型为 {@code int}
     * @param description 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param deadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param plan 业务处理参数或成员，类型为 {@code String}
     * @param verification 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQualityIssueAggregate}
     */
    public static SupplierQualityIssueAggregate rehydrate(long id, String no, long supplierId, String sourceType, String sourceNo, String issueType, int severity, String description, int status, OffsetDateTime deadline, String plan, String verification, int version) {
        return new SupplierQualityIssueAggregate(id, no, supplierId, sourceType, sourceNo, issueType, severity, description, QualityIssueStatus.fromCode(status), deadline, plan, verification, version);
    }

    /**
     * 处理当前类型职责中的操作 {@code requestRectification}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param deadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void requestRectification(OffsetDateTime deadline, long operator, IdentifierGenerator ids) {
        if (status != QualityIssueStatus.OPEN && status != QualityIssueStatus.OVERDUE) {
            throw state("当前状态不能发起整改");
        }
        if (deadline == null || !deadline.isAfter(OffsetDateTime.now())) {
            throw rule("整改截止时间必须晚于当前时间");
        }
        this.deadline = deadline;
        status = QualityIssueStatus.RECTIFYING;
        version++;
        raise(ids, "SupplierRectificationRequested", "供应商整改已发起", operator);
    }

    /**
     * 执行命令 {@code submitPlan}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param plan 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void submitPlan(String plan, long operator, IdentifierGenerator ids) {
        if (status != QualityIssueStatus.RECTIFYING && status != QualityIssueStatus.OVERDUE) {
            throw state("当前状态不能提交整改");
        }
        if (plan == null || plan.isBlank()) {
            throw rule("整改方案不能为空");
        }
        this.plan = plan.trim();
        status = QualityIssueStatus.PENDING_VERIFICATION;
        version++;
        raise(ids, "SupplierRectificationSubmitted", "供应商整改已提交", operator);
    }

    /**
     * 处理当前类型职责中的操作 {@code verify}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param passed 业务处理参数或成员，类型为 {@code boolean}
     * @param comment 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void verify(boolean passed, String comment, long operator, IdentifierGenerator ids) {
        if (status != QualityIssueStatus.PENDING_VERIFICATION) {
            throw state("当前状态不能验证整改");
        }
        if (comment == null || comment.isBlank()) {
            throw rule("验证意见不能为空");
        }
        verification = comment.trim();
        status = passed ? QualityIssueStatus.CLOSED : QualityIssueStatus.RECTIFYING;
        version++;
        raise(ids, passed ? "SupplierRectificationApproved" : "SupplierRectificationRejected", passed ? "供应商整改已通过" : "供应商整改已驳回", operator);
    }

    /**
     * 处理当前类型职责中的操作 {@code markOverdue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void markOverdue(long operator, IdentifierGenerator ids) {
        if (status != QualityIssueStatus.RECTIFYING || deadline == null || deadline.isAfter(OffsetDateTime.now())) {
            return;
        }
        status = QualityIssueStatus.OVERDUE;
        version++;
        raise(ids, "SupplierRectificationOverdue", "供应商整改已逾期", operator);
    }

    /**
     * 校验业务约束 {@code validate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void validate() {
        if (supplierId <= 0 || sourceType == null || sourceType.isBlank() || issueType == null || issueType.isBlank() || severity < 1 || severity > VALIDATE_VALUE_4 || description == null || description.isBlank()) {
            throw rule("质量问题信息不完整");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    private void raise(IdentifierGenerator ids, String type, String name, long operator) {
        long eventId = ids.nextId();
        events.add(new DomainEvent(eventId, "SUP-" + eventId, type, name, "SUPPLIER_QUALITY_ISSUE", id, no, version, operator, OffsetDateTime.now(), Map.of("supplierId", supplierId, "severity", severity, "issueType", issueType)));
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
     * 处理当前类型职责中的操作 {@code state}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param message 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private static BusinessException state(String message) {
        return new BusinessException(ErrorCode.STATE_CONFLICT, message);
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<DomainEvent>}
     */
    public List<DomainEvent> pullEvents() {
        var result = List.copyOf(events);
        events.clear();
        return result;
    }

    /**
     * 处理当前类型职责中的操作 {@code id}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long id() {
        return id;
    }

    /**
     * 处理当前类型职责中的操作 {@code no}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String no() {
        return no;
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
     * 处理当前类型职责中的操作 {@code sourceType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String sourceType() {
        return sourceType;
    }

    /**
     * 处理当前类型职责中的操作 {@code sourceNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String sourceNo() {
        return sourceNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code issueType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String issueType() {
        return issueType;
    }

    /**
     * 处理当前类型职责中的操作 {@code severity}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int severity() {
        return severity;
    }

    /**
     * 处理当前类型职责中的操作 {@code description}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String description() {
        return description;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code QualityIssueStatus}
     */
    public QualityIssueStatus status() {
        return status;
    }

    /**
     * 处理当前类型职责中的操作 {@code deadline}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OffsetDateTime}
     */
    public OffsetDateTime deadline() {
        return deadline;
    }

    /**
     * 处理当前类型职责中的操作 {@code plan}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String plan() {
        return plan;
    }

    /**
     * 处理当前类型职责中的操作 {@code verification}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String verification() {
        return verification;
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

    /**
     * 业务常量 {@code VALIDATE_VALUE_4}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int VALIDATE_VALUE_4 = 4;
}
