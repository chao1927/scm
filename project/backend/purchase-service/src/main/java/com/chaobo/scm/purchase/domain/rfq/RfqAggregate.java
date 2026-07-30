package com.chaobo.scm.purchase.domain.rfq;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.domain.shared.DomainEvent;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RfqAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class RfqAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * rfqNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String rfqNo;

    /**
     * rfqType（类型：{@code int}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private final int rfqType;

    /**
     * purchaseOrgId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long purchaseOrgId;

    /**
     * categoryCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String categoryCode;

    /**
     * sourceRequisitionNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String sourceRequisitionNo;

    /**
     * quoteDeadline（类型：{@code OffsetDateTime}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private OffsetDateTime quoteDeadline;

    /**
     * status（类型：{@code RfqStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private RfqStatus status;

    /**
     * publishedAt（类型：{@code OffsetDateTime}）。
     *
     * <p>保存当前对象所需的业务时间；其具体生命周期由所属对象统一管理。
     */
    private OffsetDateTime publishedAt;

    /**
     * closeReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String closeReason;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * lines（类型：{@code List<RfqLine>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<RfqLine> lines;

    /**
     * invitations（类型：{@code List<RfqInvitation>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<RfqInvitation> invitations;

    /**
     * events（类型：{@code List<DomainEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * 创建 RfqAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @param rfqType 数量值，类型为 {@code int}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param categoryCode 可追踪业务编码，类型为 {@code String}
     * @param sourceRequisitionNo 可追踪业务编码，类型为 {@code String}
     * @param quoteDeadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param status 生命周期状态，类型为 {@code RfqStatus}
     * @param publishedAt 业务时间，类型为 {@code OffsetDateTime}
     * @param closeReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param lines 业务处理参数或成员，类型为 {@code List<RfqLine>}
     * @param invitations 业务处理参数或成员，类型为 {@code List<RfqInvitation>}
     */
    public RfqAggregate(long id, String rfqNo, int rfqType, long purchaseOrgId, String categoryCode, String sourceRequisitionNo, OffsetDateTime quoteDeadline, RfqStatus status, OffsetDateTime publishedAt, String closeReason, int version, List<RfqLine> lines, List<RfqInvitation> invitations) {
        validateType(rfqType);
        validateDeadline(quoteDeadline);
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "询价单必须至少包含一行商品");
        }
        if (invitations == null || invitations.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "询价单必须邀请供应商");
        }
        this.id = id;
        this.rfqNo = rfqNo;
        this.rfqType = rfqType;
        this.purchaseOrgId = purchaseOrgId;
        this.categoryCode = categoryCode;
        this.sourceRequisitionNo = sourceRequisitionNo;
        this.quoteDeadline = quoteDeadline;
        this.status = status;
        this.publishedAt = publishedAt;
        this.closeReason = closeReason;
        this.version = version;
        this.lines = new ArrayList<>(lines);
        this.invitations = new ArrayList<>(invitations);
        assertNoDuplicateSku(this.lines);
        assertNoDuplicateSupplier(this.invitations);
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param rfqType 数量值，类型为 {@code int}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param categoryCode 可追踪业务编码，类型为 {@code String}
     * @param sourceRequisitionNo 可追踪业务编码，类型为 {@code String}
     * @param quoteDeadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param lines 业务处理参数或成员，类型为 {@code List<RfqLine>}
     * @param invitations 业务处理参数或成员，类型为 {@code List<RfqInvitation>}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 执行命令的结果，类型为 {@code RfqAggregate}
     */
    public static RfqAggregate create(int rfqType, long purchaseOrgId, String categoryCode, String sourceRequisitionNo, OffsetDateTime quoteDeadline, List<RfqLine> lines, List<RfqInvitation> invitations, IdentifierGenerator ids) {
        var aggregate = new RfqAggregate(ids.nextId(), ids.nextCode("RFQ"), rfqType, purchaseOrgId, categoryCode, sourceRequisitionNo, quoteDeadline, RfqStatus.DRAFT, null, null, 0, lines, invitations);
        aggregate.raise("RfqCreated", Map.of(), "");
        return aggregate;
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void publish(IdentifierGenerator ids) {
        ensureStatus(RfqStatus.DRAFT);
        if (!quoteDeadline.isAfter(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "报价截止时间必须晚于当前时间");
        }
        touch();
        this.status = RfqStatus.QUOTING;
        this.publishedAt = OffsetDateTime.now();
        for (RfqInvitation invitation : invitations) {
            raise("RfqPublished", Map.of("supplierId", invitation.supplierId()), "-" + invitation.supplierId());
        }
    }

    /**
     * 执行命令 {@code closeBidding}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void closeBidding(String reason, IdentifierGenerator ids) {
        ensureStatus(RfqStatus.PUBLISHED, RfqStatus.QUOTING);
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "截标原因不能为空");
        }
        touch();
        this.status = RfqStatus.BIDDING_CLOSED;
        this.closeReason = reason;
        for (RfqInvitation invitation : invitations) {
            invitation.closeTodo();
        }
        raise("RfqBiddingClosed", Map.of("closeReason", reason), "");
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<DomainEvent>}
     */
    public List<DomainEvent> pullEvents() {
        var pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    /**
     * 校验业务约束 {@code ensureStatus}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param allowed 业务处理参数或成员，类型为 {@code RfqStatus}
     */
    private void ensureStatus(RfqStatus... allowed) {
        for (RfqStatus candidate : allowed) {
            if (status == candidate) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.STATE_CONFLICT, "当前询价状态不允许执行该操作");
    }

    /**
     * 转换数据模型 {@code touch}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void touch() {
        this.version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param extra 业务处理参数或成员，类型为 {@code Map<String,Object>}
     * @param eventSuffix 业务处理参数或成员，类型为 {@code String}
     */
    private void raise(String eventType, Map<String, Object> extra, String eventSuffix) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("rfqId", id);
        payload.put("rfqNo", rfqNo);
        payload.put("rfqType", rfqType);
        payload.put("purchaseOrgId", purchaseOrgId);
        payload.put("categoryCode", Objects.requireNonNullElse(categoryCode, ""));
        payload.put("sourceRequisitionNo", Objects.requireNonNullElse(sourceRequisitionNo, ""));
        payload.put("quoteDeadline", quoteDeadline.toString());
        payload.put("status", status.code());
        payload.put("version", version);
        payload.put("supplierIds", invitations.stream().map(RfqInvitation::supplierId).toList().toString());
        payload.putAll(extra);
        events.add(new DomainEvent(0, "PUR-" + eventType + "-" + id + "-" + version + eventSuffix, eventType, "RFQ", Long.toString(id), version, OffsetDateTime.now(), payload));
    }

    /**
     * 校验业务约束 {@code validateType}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param rfqType 数量值，类型为 {@code int}
     */
    private static void validateType(int rfqType) {
        if (rfqType < 1 || rfqType > VALIDATE_TYPE_VALUE_3) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "询价类型不合法");
        }
    }

    /**
     * 校验业务约束 {@code validateDeadline}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param quoteDeadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     */
    private static void validateDeadline(OffsetDateTime quoteDeadline) {
        if (quoteDeadline == null || !quoteDeadline.isAfter(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "报价截止时间必须晚于当前时间");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code assertNoDuplicateSku}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param lines 业务处理参数或成员，类型为 {@code List<RfqLine>}
     */
    private static void assertNoDuplicateSku(List<RfqLine> lines) {
        var keys = new java.util.HashSet<String>();
        for (RfqLine line : lines) {
            if (!keys.add(line.skuCode())) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "询价行SKU不能重复");
            }
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code assertNoDuplicateSupplier}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param invitations 业务处理参数或成员，类型为 {@code List<RfqInvitation>}
     */
    private static void assertNoDuplicateSupplier(List<RfqInvitation> invitations) {
        var supplierIds = new java.util.HashSet<Long>();
        for (RfqInvitation invitation : invitations) {
            if (!supplierIds.add(invitation.supplierId())) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "邀请供应商不能重复");
            }
        }
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
     * 处理当前类型职责中的操作 {@code rfqNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String rfqNo() {
        return rfqNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code rfqType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int rfqType() {
        return rfqType;
    }

    /**
     * 处理当前类型职责中的操作 {@code purchaseOrgId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long purchaseOrgId() {
        return purchaseOrgId;
    }

    /**
     * 处理当前类型职责中的操作 {@code categoryCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String categoryCode() {
        return categoryCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code sourceRequisitionNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String sourceRequisitionNo() {
        return sourceRequisitionNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code quoteDeadline}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OffsetDateTime}
     */
    public OffsetDateTime quoteDeadline() {
        return quoteDeadline;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code RfqStatus}
     */
    public RfqStatus status() {
        return status;
    }

    /**
     * 执行命令 {@code publishedAt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code OffsetDateTime}
     */
    public OffsetDateTime publishedAt() {
        return publishedAt;
    }

    /**
     * 执行命令 {@code closeReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code String}
     */
    public String closeReason() {
        return closeReason;
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
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<RfqLine>}
     */
    public List<RfqLine> lines() {
        return List.copyOf(lines);
    }

    /**
     * 处理当前类型职责中的操作 {@code invitations}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<RfqInvitation>}
     */
    public List<RfqInvitation> invitations() {
        return List.copyOf(invitations);
    }

    /**
     * 业务常量 {@code VALIDATE_TYPE_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int VALIDATE_TYPE_VALUE_3 = 3;
}
