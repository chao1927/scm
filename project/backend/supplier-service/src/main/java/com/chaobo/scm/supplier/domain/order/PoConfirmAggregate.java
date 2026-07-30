package com.chaobo.scm.supplier.domain.order;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.domain.shared.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/**
 * PoConfirmAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class PoConfirmAggregate {

    /**
     * confirmId、purchaseOrderId、supplierId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long confirmId, purchaseOrderId, supplierId;

    /**
     * confirmNo、purchaseOrderNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String confirmNo, purchaseOrderNo;

    /**
     * deadline（类型：{@code OffsetDateTime}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private OffsetDateTime deadline;

    /**
     * lines（类型：{@code List<PoConfirmLine>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<PoConfirmLine> lines;

    /**
     * status（类型：{@code PoConfirmStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private PoConfirmStatus status;

    /**
     * confirmedAt（类型：{@code OffsetDateTime}）。
     *
     * <p>保存当前对象所需的业务时间；其具体生命周期由所属对象统一管理。
     */
    private OffsetDateTime confirmedAt;

    /**
     * diffType、reasonCode（类型：{@code Integer}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private Integer diffType, reasonCode;

    /**
     * remark（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String remark;

    /**
     * sourceVersion、version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int sourceVersion, version;

    /**
     * events（类型：{@code List<DomainEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * 创建 PoConfirmAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param poId 业务或技术标识，类型为 {@code long}
     * @param poNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param deadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param lines 业务处理参数或成员，类型为 {@code List<PoConfirmLine>}
     * @param status 生命周期状态，类型为 {@code PoConfirmStatus}
     * @param confirmedAt 业务时间，类型为 {@code OffsetDateTime}
     * @param diffType 业务处理参数或成员，类型为 {@code Integer}
     * @param reasonCode 可追踪业务编码，类型为 {@code Integer}
     * @param remark 业务处理参数或成员，类型为 {@code String}
     * @param sourceVersion 乐观锁或契约版本，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    private PoConfirmAggregate(long id, String no, long poId, String poNo, long supplierId, OffsetDateTime deadline, List<PoConfirmLine> lines, PoConfirmStatus status, OffsetDateTime confirmedAt, Integer diffType, Integer reasonCode, String remark, int sourceVersion, int version) {
        this.confirmId = id;
        this.confirmNo = no;
        this.purchaseOrderId = poId;
        this.purchaseOrderNo = poNo;
        this.supplierId = supplierId;
        this.deadline = deadline;
        this.lines = new ArrayList<>(lines);
        this.status = status;
        this.confirmedAt = confirmedAt;
        this.diffType = diffType;
        this.reasonCode = reasonCode;
        this.remark = remark;
        this.sourceVersion = sourceVersion;
        this.version = version;
    }

    /**
     * 处理当前类型职责中的操作 {@code receive}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param poId 业务或技术标识，类型为 {@code long}
     * @param poNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param deadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param source 业务处理参数或成员，类型为 {@code List<NewLine>}
     * @param sourceVersion 乐观锁或契约版本，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PoConfirmAggregate}
     */
    public static PoConfirmAggregate receive(long poId, String poNo, long supplierId, OffsetDateTime deadline, List<NewLine> source, int sourceVersion, long operator, IdentifierGenerator ids) {
        validateSource(poId, poNo, supplierId, source, sourceVersion);
        long id = ids.nextId();
        var aggregate = new PoConfirmAggregate(id, ids.nextBusinessNo("POC"), poId, poNo, supplierId, deadline, newLines(source, ids), PoConfirmStatus.PENDING, null, null, null, null, sourceVersion, 0);
        aggregate.raise(ids, "PurchaseOrderConfirmTodoCreated", "采购订单确认待办已创建", operator, Map.of("purchaseOrderId", poId, "supplierId", supplierId, "sourceVersion", sourceVersion));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code receive}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param poId 业务或技术标识，类型为 {@code long}
     * @param poNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param deadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param source 业务处理参数或成员，类型为 {@code List<NewLine>}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PoConfirmAggregate}
     */
    public static PoConfirmAggregate receive(long poId, String poNo, long supplierId, OffsetDateTime deadline, List<NewLine> source, long operator, IdentifierGenerator ids) {
        return receive(poId, poNo, supplierId, deadline, source, 1, operator, ids);
    }

    /**
     * 处理当前类型职责中的操作 {@code rehydrate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param poId 业务或技术标识，类型为 {@code long}
     * @param poNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param deadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param lines 业务处理参数或成员，类型为 {@code List<PoConfirmLine>}
     * @param status 生命周期状态，类型为 {@code PoConfirmStatus}
     * @param confirmedAt 业务时间，类型为 {@code OffsetDateTime}
     * @param diffType 业务处理参数或成员，类型为 {@code Integer}
     * @param reasonCode 可追踪业务编码，类型为 {@code Integer}
     * @param remark 业务处理参数或成员，类型为 {@code String}
     * @param sourceVersion 乐观锁或契约版本，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PoConfirmAggregate}
     */
    public static PoConfirmAggregate rehydrate(long id, String no, long poId, String poNo, long supplierId, OffsetDateTime deadline, List<PoConfirmLine> lines, PoConfirmStatus status, OffsetDateTime confirmedAt, Integer diffType, Integer reasonCode, String remark, int sourceVersion, int version) {
        return new PoConfirmAggregate(id, no, poId, poNo, supplierId, deadline, lines, status, confirmedAt, diffType, reasonCode, remark, sourceVersion, version);
    }

    /**
     * 执行命令 {@code applyOrderChange}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param newDeadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param source 业务处理参数或成员，类型为 {@code List<NewLine>}
     * @param newSourceVersion 乐观锁或契约版本，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void applyOrderChange(OffsetDateTime newDeadline, List<NewLine> source, int newSourceVersion, long operator, IdentifierGenerator ids) {
        ensureNewVersion(newSourceVersion);
        if (status == PoConfirmStatus.CLOSED || status == PoConfirmStatus.CANCELLED) {
            throw state("终态订单不能变更");
        }
        if (source == null || source.isEmpty()) {
            throw rule("变更后订单行不能为空");
        }
        lines.clear();
        lines.addAll(newLines(source, ids));
        deadline = newDeadline;
        sourceVersion = newSourceVersion;
        status = PoConfirmStatus.PENDING;
        confirmedAt = null;
        diffType = null;
        reasonCode = null;
        remark = "采购订单已变更，需重新确认";
        version++;
        raise(ids, "PurchaseOrderChangeReceived", "采购订单变更已接收", operator, Map.of("purchaseOrderId", purchaseOrderId, "sourceVersion", sourceVersion));
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param newSourceVersion 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void cancel(int newSourceVersion, String reason, long operator, IdentifierGenerator ids) {
        ensureNewVersion(newSourceVersion);
        if (status == PoConfirmStatus.CLOSED) {
            throw state("已关闭订单不能取消");
        }
        if (reason == null || reason.isBlank()) {
            throw rule("取消原因不能为空");
        }
        sourceVersion = newSourceVersion;
        status = PoConfirmStatus.CANCELLED;
        remark = reason.trim();
        version++;
        raise(ids, "PurchaseOrderCancellationReceived", "采购订单取消已接收", operator, Map.of("purchaseOrderId", purchaseOrderId, "reason", remark, "sourceVersion", sourceVersion));
    }

    /**
     * 执行命令 {@code close}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param newSourceVersion 乐观锁或契约版本，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void close(int newSourceVersion, long operator, IdentifierGenerator ids) {
        ensureNewVersion(newSourceVersion);
        if (status == PoConfirmStatus.CANCELLED) {
            throw state("已取消订单不能关闭");
        }
        sourceVersion = newSourceVersion;
        status = PoConfirmStatus.CLOSED;
        version++;
        raise(ids, "PurchaseOrderClosureReceived", "采购订单关闭已接收", operator, Map.of("purchaseOrderId", purchaseOrderId, "sourceVersion", sourceVersion));
    }

    /**
     * 执行命令 {@code confirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param decisions 业务处理参数或成员，类型为 {@code List<LineDecision>}
     * @param remark 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void confirm(List<LineDecision> decisions, String remark, long operator, IdentifierGenerator ids) {
        pending();
        if (decisions == null || decisions.size() != lines.size()) {
            throw rule("确认必须覆盖全部采购订单行");
        }
        Map<Long, LineDecision> map = new HashMap<>(decisions.size());
        decisions.forEach(value -> map.put(value.lineId(), value));
        for (var line : lines) {
            var decision = map.get(line.lineId());
            if (decision == null) {
                throw rule("存在未确认订单行");
            }
            line.confirm(decision.quantity(), decision.deliveryDate());
        }
        status = PoConfirmStatus.CONFIRMED;
        confirmedAt = OffsetDateTime.now();
        this.remark = remark;
        version++;
        raise(ids, "PurchaseOrderConfirmedBySupplier", "供应商订单已确认", operator, Map.of("purchaseOrderId", purchaseOrderId, "supplierId", supplierId, "sourceVersion", sourceVersion));
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code int}
     * @param remark 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void reject(int reason, String remark, long operator, IdentifierGenerator ids) {
        pending();
        if (reason <= 0) {
            throw rule("拒绝原因不能为空");
        }
        status = PoConfirmStatus.REJECTED;
        reasonCode = reason;
        this.remark = remark;
        version++;
        raise(ids, "PurchaseOrderRejectedBySupplier", "供应商订单已拒绝", operator, Map.of("purchaseOrderId", purchaseOrderId, "reasonCode", reason));
    }

    /**
     * 处理当前类型职责中的操作 {@code feedbackDifference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param type 业务处理参数或成员，类型为 {@code int}
     * @param differences 业务处理参数或成员，类型为 {@code List<LineDifference>}
     * @param remark 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void feedbackDifference(int type, List<LineDifference> differences, String remark, long operator, IdentifierGenerator ids) {
        pending();
        if (type < 1 || type > FEEDBACK_DIFFERENCE_VALUE_4 || differences == null || differences.isEmpty()) {
            throw rule("差异类型和差异行不能为空");
        }
        for (var difference : differences) {
            line(difference.lineId()).difference(difference.quantity(), difference.deliveryDate(), difference.reason());
        }
        status = PoConfirmStatus.DIFFERENCE_PENDING;
        diffType = type;
        this.remark = remark;
        version++;
        raise(ids, "PurchaseOrderDifferenceReportedBySupplier", "供应商订单差异已反馈", operator, Map.of("purchaseOrderId", purchaseOrderId, "diffType", type));
    }

    /**
     * 处理当前类型职责中的操作 {@code changeDelivery}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param lineId 业务或技术标识，类型为 {@code long}
     * @param date 业务时间，类型为 {@code LocalDate}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void changeDelivery(long lineId, LocalDate date, String reason, long operator, IdentifierGenerator ids) {
        if (status != PoConfirmStatus.CONFIRMED) {
            throw state("只有已确认订单可以修改承诺交期");
        }
        if (reason == null || reason.isBlank()) {
            throw rule("修改交期原因不能为空");
        }
        line(lineId).changeDelivery(date);
        version++;
        raise(ids, "SupplierPromisedDeliveryDateChanged", "供应商承诺交期已变更", operator, Map.of("purchaseOrderId", purchaseOrderId, "orderLineId", lineId, "newDeliveryDate", date.toString(), "reason", reason));
    }

    /**
     * 处理当前类型职责中的操作 {@code newLines}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param source 业务处理参数或成员，类型为 {@code List<NewLine>}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PoConfirmLine>}
     */
    private static List<PoConfirmLine> newLines(List<NewLine> source, IdentifierGenerator ids) {
        var skuSet = new HashSet<String>();
        return source.stream().map(value -> {
            if (!skuSet.add(value.skuCode())) {
                throw rule("采购订单SKU重复: " + value.skuCode());
            }
            return new PoConfirmLine(ids.nextId(), value.skuCode(), value.orderQty(), value.requestedDate(), null, null, 1, null);
        }).toList();
    }

    /**
     * 校验业务约束 {@code validateSource}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param poId 业务或技术标识，类型为 {@code long}
     * @param poNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param source 业务处理参数或成员，类型为 {@code List<NewLine>}
     * @param sourceVersion 乐观锁或契约版本，类型为 {@code int}
     */
    private static void validateSource(long poId, String poNo, long supplierId, List<NewLine> source, int sourceVersion) {
        if (poId <= 0 || supplierId <= 0 || poNo == null || poNo.isBlank() || source == null || source.isEmpty() || sourceVersion <= 0) {
            throw rule("采购订单确认数据不完整");
        }
    }

    /**
     * 校验业务约束 {@code ensureNewVersion}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param incoming 业务处理参数或成员，类型为 {@code int}
     */
    private void ensureNewVersion(int incoming) {
        if (incoming <= sourceVersion) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "采购订单事件版本已处理");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code pending}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void pending() {
        if (status != PoConfirmStatus.PENDING) {
            throw state("当前采购订单协同状态不允许该操作");
        }
        if (deadline != null && OffsetDateTime.now().isAfter(deadline)) {
            throw rule("采购订单确认已超过截止时间");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code line}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PoConfirmLine}
     */
    private PoConfirmLine line(long id) {
        return lines.stream().filter(value -> value.lineId() == id).findFirst().orElseThrow(() -> rule("采购订单行不存在"));
    }

    /**
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param payload 业务处理参数或成员，类型为 {@code Map<String,Object>}
     */
    private void raise(IdentifierGenerator ids, String type, String name, long operator, Map<String, Object> payload) {
        long eventId = ids.nextId();
        events.add(new DomainEvent(eventId, "SUP-" + eventId, type, name, "PO_CONFIRM", confirmId, confirmNo, version, operator, OffsetDateTime.now(), payload));
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
        var copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    /**
     * 执行命令 {@code confirmId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code long}
     */
    public long confirmId() {
        return confirmId;
    }

    /**
     * 执行命令 {@code confirmNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code String}
     */
    public String confirmNo() {
        return confirmNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code purchaseOrderId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long purchaseOrderId() {
        return purchaseOrderId;
    }

    /**
     * 处理当前类型职责中的操作 {@code purchaseOrderNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String purchaseOrderNo() {
        return purchaseOrderNo;
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
     * 处理当前类型职责中的操作 {@code deadline}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OffsetDateTime}
     */
    public OffsetDateTime deadline() {
        return deadline;
    }

    /**
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PoConfirmLine>}
     */
    public List<PoConfirmLine> lines() {
        return List.copyOf(lines);
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PoConfirmStatus}
     */
    public PoConfirmStatus status() {
        return status;
    }

    /**
     * 执行命令 {@code confirmedAt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code OffsetDateTime}
     */
    public OffsetDateTime confirmedAt() {
        return confirmedAt;
    }

    /**
     * 处理当前类型职责中的操作 {@code diffType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Integer}
     */
    public Integer diffType() {
        return diffType;
    }

    /**
     * 处理当前类型职责中的操作 {@code reasonCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Integer}
     */
    public Integer reasonCode() {
        return reasonCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code remark}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String remark() {
        return remark;
    }

    /**
     * 处理当前类型职责中的操作 {@code sourceVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int sourceVersion() {
        return sourceVersion;
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
     * NewLine。
     *
     * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record NewLine(String skuCode, BigDecimal orderQty, LocalDate requestedDate) {
    }

    /**
     * LineDecision。
     *
     * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record LineDecision(long lineId, BigDecimal quantity, LocalDate deliveryDate) {
    }

    /**
     * LineDifference。
     *
     * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record LineDifference(long lineId, BigDecimal quantity, LocalDate deliveryDate, String reason) {
    }

    /**
     * 业务常量 {@code FEEDBACK_DIFFERENCE_VALUE_4}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int FEEDBACK_DIFFERENCE_VALUE_4 = 4;
}
