package com.chaobo.scm.supplier.domain.contract;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.domain.shared.DomainEvent;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * SupplierContractAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class SupplierContractAggregate {

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
     * quoteId（类型：{@code Long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final Long quoteId;

    /**
     * agreement（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String agreement;

    /**
     * type（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String type;

    /**
     * from（类型：{@code LocalDate}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final LocalDate from;

    /**
     * to（类型：{@code LocalDate}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private LocalDate to;

    /**
     * status（类型：{@code ContractStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private ContractStatus status;

    /**
     * terms（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String terms;

    /**
     * attachment（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String attachment;

    /**
     * reason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String reason;

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
     * 创建 SupplierContractAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param quoteId 业务或技术标识，类型为 {@code Long}
     * @param agreement 业务处理参数或成员，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param status 生命周期状态，类型为 {@code ContractStatus}
     * @param terms 业务处理参数或成员，类型为 {@code String}
     * @param attachment 业务处理参数或成员，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    private SupplierContractAggregate(long id, String no, long supplierId, Long quoteId, String agreement, String type, LocalDate from, LocalDate to, ContractStatus status, String terms, String attachment, String reason, int version) {
        this.id = id;
        this.no = no;
        this.supplierId = supplierId;
        this.quoteId = quoteId;
        this.agreement = agreement;
        this.type = type;
        this.from = from;
        this.to = to;
        this.status = status;
        this.terms = terms;
        this.attachment = attachment;
        this.reason = reason;
        this.version = version;
        validate();
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param quoteId 业务或技术标识，类型为 {@code Long}
     * @param agreement 业务处理参数或成员，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param terms 业务处理参数或成员，类型为 {@code String}
     * @param attachment 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 执行命令的结果，类型为 {@code SupplierContractAggregate}
     */
    public static SupplierContractAggregate create(long supplierId, Long quoteId, String agreement, String type, LocalDate from, LocalDate to, String terms, String attachment, long operator, IdentifierGenerator ids) {
        var result = new SupplierContractAggregate(ids.nextId(), ids.nextBusinessNo("SC"), supplierId, quoteId, agreement, type, from, to, ContractStatus.DRAFT, terms, attachment, null, 0);
        result.raise(ids, "SupplierContractCreated", "供应商合同已创建", operator);
        return result;
    }

    /**
     * 处理当前类型职责中的操作 {@code rehydrate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param quoteId 业务或技术标识，类型为 {@code Long}
     * @param agreement 业务处理参数或成员，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param status 生命周期状态，类型为 {@code int}
     * @param terms 业务处理参数或成员，类型为 {@code String}
     * @param attachment 业务处理参数或成员，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierContractAggregate}
     */
    public static SupplierContractAggregate rehydrate(long id, String no, long supplierId, Long quoteId, String agreement, String type, LocalDate from, LocalDate to, int status, String terms, String attachment, String reason, int version) {
        return new SupplierContractAggregate(id, no, supplierId, quoteId, agreement, type, from, to, ContractStatus.from(status), terms, attachment, reason, version);
    }

    /**
     * 处理当前类型职责中的操作 {@code modifyDraft}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param until 业务处理参数或成员，类型为 {@code LocalDate}
     * @param updatedTerms 业务时间，类型为 {@code String}
     * @param updatedAttachment 业务时间，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void modifyDraft(LocalDate until, String updatedTerms, String updatedAttachment, long operator, IdentifierGenerator ids) {
        require(ContractStatus.DRAFT);
        if (until == null || until.isBefore(from) || updatedTerms == null || updatedTerms.isBlank() || updatedAttachment == null || updatedAttachment.isBlank()) {
            throw rule("合同有效期、条款或附件不合法");
        }
        to = until;
        terms = updatedTerms.trim();
        attachment = updatedAttachment.trim();
        reason = null;
        version++;
        raise(ids, "SupplierContractDraftModified", "供应商合同草稿已修改", operator);
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void submit(long operator, IdentifierGenerator ids) {
        require(ContractStatus.DRAFT);
        if (attachment == null || attachment.isBlank()) {
            throw rule("合同附件不能为空");
        }
        status = ContractStatus.APPROVING;
        reason = null;
        version++;
        raise(ids, "SupplierContractSubmitted", "供应商合同已提交审批", operator);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void approve(long operator, IdentifierGenerator ids) {
        require(ContractStatus.APPROVING);
        status = ContractStatus.ACTIVE;
        reason = null;
        version++;
        raise(ids, "SupplierContractActivated", "供应商合同已生效", operator);
    }

    /**
     * 执行命令 {@code rejectApproval}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param comment 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void rejectApproval(String comment, long operator, IdentifierGenerator ids) {
        require(ContractStatus.APPROVING);
        if (comment == null || comment.isBlank()) {
            throw rule("审批驳回意见不能为空");
        }
        status = ContractStatus.DRAFT;
        reason = comment.trim();
        version++;
        raise(ids, "SupplierContractApprovalRejected", "供应商合同审批已驳回", operator);
    }

    /**
     * 处理当前类型职责中的操作 {@code renew}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param until 业务处理参数或成员，类型为 {@code LocalDate}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void renew(LocalDate until, long operator, IdentifierGenerator ids) {
        require(ContractStatus.ACTIVE);
        if (until == null || !until.isAfter(to)) {
            throw rule("续签截止日期必须晚于当前有效期");
        }
        to = until;
        version++;
        raise(ids, "SupplierContractRenewed", "供应商合同已续签", operator);
    }

    /**
     * 处理当前类型职责中的操作 {@code terminate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param terminationReason 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void terminate(String terminationReason, long operator, IdentifierGenerator ids) {
        if (status != ContractStatus.ACTIVE && status != ContractStatus.APPROVING) {
            throw state("当前状态不能终止合同");
        }
        if (terminationReason == null || terminationReason.isBlank()) {
            throw rule("终止原因不能为空");
        }
        reason = terminationReason.trim();
        status = ContractStatus.TERMINATED;
        version++;
        raise(ids, "SupplierContractTerminated", "供应商合同已终止", operator);
    }

    /**
     * 处理当前类型职责中的操作 {@code expire}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void expire(long operator, IdentifierGenerator ids) {
        if (status != ContractStatus.ACTIVE || to.isAfter(LocalDate.now())) {
            return;
        }
        status = ContractStatus.EXPIRED;
        version++;
        raise(ids, "SupplierContractExpired", "供应商合同已到期", operator);
    }

    /**
     * 校验业务约束 {@code validate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void validate() {
        if (supplierId <= 0 || type == null || type.isBlank() || from == null || to == null || to.isBefore(from) || terms == null || terms.isBlank()) {
            throw rule("合同核心信息不完整");
        }
    }

    /**
     * 查询并返回 {@code require}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param expected 业务处理参数或成员，类型为 {@code ContractStatus}
     */
    private void require(ContractStatus expected) {
        if (status != expected) {
            throw state("合同状态不允许当前操作");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    private void raise(IdentifierGenerator ids, String eventType, String name, long operator) {
        long eventId = ids.nextId();
        events.add(new DomainEvent(eventId, "SUP-" + eventId, eventType, name, "SUPPLIER_CONTRACT", id, no, version, operator, OffsetDateTime.now(), Map.of("contractNo", no, "supplierId", supplierId, "quoteId", quoteId == null ? 0 : quoteId, "priceAgreementRef", agreement == null ? "" : agreement, "validTo", to.toString())));
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
     * 处理当前类型职责中的操作 {@code quoteId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    public Long quoteId() {
        return quoteId;
    }

    /**
     * 处理当前类型职责中的操作 {@code agreement}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String agreement() {
        return agreement;
    }

    /**
     * 处理当前类型职责中的操作 {@code type}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String type() {
        return type;
    }

    /**
     * 转换数据模型 {@code from}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 转换数据模型的结果，类型为 {@code LocalDate}
     */
    public LocalDate from() {
        return from;
    }

    /**
     * 转换数据模型 {@code to}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 转换数据模型的结果，类型为 {@code LocalDate}
     */
    public LocalDate to() {
        return to;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ContractStatus}
     */
    public ContractStatus status() {
        return status;
    }

    /**
     * 处理当前类型职责中的操作 {@code terms}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String terms() {
        return terms;
    }

    /**
     * 处理当前类型职责中的操作 {@code attachment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String attachment() {
        return attachment;
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
     * 处理当前类型职责中的操作 {@code version}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int version() {
        return version;
    }
}
