package com.chaobo.scm.purchase.domain.requisition;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.domain.shared.DomainEvent;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * PurchaseRequisitionAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class PurchaseRequisitionAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * requisitionNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String requisitionNo;

    /**
     * applicantId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long applicantId;

    /**
     * purchaseOrgId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long purchaseOrgId;

    /**
     * demandDepartmentId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long demandDepartmentId;

    /**
     * status（类型：{@code PurchaseRequisitionStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private PurchaseRequisitionStatus status;

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
     * lines（类型：{@code List<PurchaseRequisitionLine>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<PurchaseRequisitionLine> lines;

    /**
     * events（类型：{@code List<DomainEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * 创建 PurchaseRequisitionAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param requisitionNo 可追踪业务编码，类型为 {@code String}
     * @param applicantId 业务或技术标识，类型为 {@code long}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param demandDepartmentId 业务或技术标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code PurchaseRequisitionStatus}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param lines 业务处理参数或成员，类型为 {@code List<PurchaseRequisitionLine>}
     */
    public PurchaseRequisitionAggregate(long id, String requisitionNo, long applicantId, long purchaseOrgId, long demandDepartmentId, PurchaseRequisitionStatus status, String reason, int version, List<PurchaseRequisitionLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "请购单必须至少包含一行商品");
        }
        this.id = id;
        this.requisitionNo = requisitionNo;
        this.applicantId = applicantId;
        this.purchaseOrgId = purchaseOrgId;
        this.demandDepartmentId = demandDepartmentId;
        this.status = status;
        this.reason = reason;
        this.version = version;
        this.lines = new ArrayList<>(lines);
        assertNoDuplicateSkuAndDate(this.lines);
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param applicantId 业务或技术标识，类型为 {@code long}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param demandDepartmentId 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<PurchaseRequisitionLine>}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 执行命令的结果，类型为 {@code PurchaseRequisitionAggregate}
     */
    public static PurchaseRequisitionAggregate create(long applicantId, long purchaseOrgId, long demandDepartmentId, String reason, List<PurchaseRequisitionLine> lines, IdentifierGenerator ids) {
        var aggregate = new PurchaseRequisitionAggregate(ids.nextId(), ids.nextCode("PR"), applicantId, purchaseOrgId, demandDepartmentId, PurchaseRequisitionStatus.DRAFT, reason, 0, lines);
        aggregate.raise("PurchaseRequisitionCreated");
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code changeDraft}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param newLines 业务处理参数或成员，类型为 {@code List<PurchaseRequisitionLine>}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void changeDraft(String reason, List<PurchaseRequisitionLine> newLines, IdentifierGenerator ids) {
        ensureStatus(PurchaseRequisitionStatus.DRAFT, PurchaseRequisitionStatus.REJECTED);
        assertNoDuplicateSkuAndDate(newLines);
        this.reason = reason;
        this.lines.clear();
        this.lines.addAll(newLines);
        touch();
        raise("PurchaseRequisitionChanged");
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void submit(IdentifierGenerator ids) {
        ensureStatus(PurchaseRequisitionStatus.DRAFT, PurchaseRequisitionStatus.REJECTED);
        touch();
        this.status = PurchaseRequisitionStatus.SUBMITTED;
        raise("PurchaseRequisitionSubmitted");
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param approvedQuantities 业务处理参数或成员，类型为 {@code Map<Long,BigDecimal>}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void approve(Map<Long, BigDecimal> approvedQuantities, IdentifierGenerator ids) {
        ensureStatus(PurchaseRequisitionStatus.SUBMITTED);
        for (PurchaseRequisitionLine line : lines) {
            line.approve(approvedQuantities.getOrDefault(line.lineId(), line.requestedQty()));
        }
        if (lines.stream().allMatch(line -> line.approvedQty().signum() == 0)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "批准数量不能全部为0");
        }
        touch();
        this.status = PurchaseRequisitionStatus.APPROVED;
        raise("PurchaseRequisitionApproved");
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param rejectReason 业务处理参数或成员，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void reject(String rejectReason, IdentifierGenerator ids) {
        ensureStatus(PurchaseRequisitionStatus.SUBMITTED);
        if (rejectReason == null || rejectReason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "驳回原因不能为空");
        }
        this.reason = rejectReason;
        touch();
        this.status = PurchaseRequisitionStatus.REJECTED;
        raise("PurchaseRequisitionRejected");
    }

    /**
     * 处理当前类型职责中的操作 {@code convert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param quantities 业务处理参数或成员，类型为 {@code Map<Long,BigDecimal>}
     * @param targetType 业务处理参数或成员，类型为 {@code String}
     * @param targetNo 可追踪业务编码，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void convert(Map<Long, BigDecimal> quantities, String targetType, String targetNo, IdentifierGenerator ids) {
        ensureStatus(PurchaseRequisitionStatus.APPROVED, PurchaseRequisitionStatus.PARTIALLY_CONVERTED);
        if (quantities == null || quantities.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "转采购行不能为空");
        }
        for (Map.Entry<Long, BigDecimal> entry : quantities.entrySet()) {
            line(entry.getKey()).convert(entry.getValue());
        }
        touch();
        this.status = allConverted() ? PurchaseRequisitionStatus.CONVERTED : PurchaseRequisitionStatus.PARTIALLY_CONVERTED;
        raise("PurchaseRequisitionConverted", Map.of("targetType", Objects.requireNonNullElse(targetType, ""), "targetNo", Objects.requireNonNullElse(targetNo, "")));
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
     * @param allowed 业务处理参数或成员，类型为 {@code PurchaseRequisitionStatus}
     */
    private void ensureStatus(PurchaseRequisitionStatus... allowed) {
        for (PurchaseRequisitionStatus candidate : allowed) {
            if (status == candidate) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.STATE_CONFLICT, "当前请购状态不允许执行该操作");
    }

    /**
     * 处理当前类型职责中的操作 {@code line}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param lineId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchaseRequisitionLine}
     */
    private PurchaseRequisitionLine line(long lineId) {
        return lines.stream().filter(line -> line.lineId() == lineId).findFirst().orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "请购行不存在"));
    }

    /**
     * 处理当前类型职责中的操作 {@code allConverted}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private boolean allConverted() {
        return lines.stream().allMatch(line -> line.remainingApprovedQty().signum() == 0);
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
     */
    private void raise(String eventType) {
        raise(eventType, Map.of());
    }

    /**
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param extra 业务处理参数或成员，类型为 {@code Map<String,Object>}
     */
    private void raise(String eventType, Map<String, Object> extra) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("requisitionId", id);
        payload.put("requisitionNo", requisitionNo);
        payload.put("purchaseOrgId", purchaseOrgId);
        payload.put("demandDepartmentId", demandDepartmentId);
        payload.put("status", status.code());
        payload.put("version", version);
        payload.putAll(extra);
        events.add(new DomainEvent(0, "PUR-" + eventType + "-" + id + "-" + version, eventType, "PURCHASE_REQUISITION", Long.toString(id), version, OffsetDateTime.now(), payload));
    }

    /**
     * 处理当前类型职责中的操作 {@code assertNoDuplicateSkuAndDate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param lines 业务处理参数或成员，类型为 {@code List<PurchaseRequisitionLine>}
     */
    private static void assertNoDuplicateSkuAndDate(List<PurchaseRequisitionLine> lines) {
        var keys = new java.util.HashSet<String>();
        for (PurchaseRequisitionLine line : lines) {
            var key = line.skuCode() + "|" + line.requiredDate();
            if (!keys.add(key)) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "同一SKU和需求日期不能重复");
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
     * 处理当前类型职责中的操作 {@code requisitionNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String requisitionNo() {
        return requisitionNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code applicantId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long applicantId() {
        return applicantId;
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
     * 处理当前类型职责中的操作 {@code demandDepartmentId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long demandDepartmentId() {
        return demandDepartmentId;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchaseRequisitionStatus}
     */
    public PurchaseRequisitionStatus status() {
        return status;
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

    /**
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PurchaseRequisitionLine>}
     */
    public List<PurchaseRequisitionLine> lines() {
        return List.copyOf(lines);
    }
}
