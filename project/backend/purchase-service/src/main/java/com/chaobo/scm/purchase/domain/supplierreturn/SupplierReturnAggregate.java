package com.chaobo.scm.purchase.domain.supplierreturn;

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
 * SupplierReturnAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class SupplierReturnAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * returnNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String returnNo;

    /**
     * sourceOrderNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String sourceOrderNo;

    /**
     * supplierId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long supplierId;

    /**
     * purchaseOrgId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long purchaseOrgId;

    /**
     * warehouseCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String warehouseCode;

    /**
     * status（类型：{@code SupplierReturnStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private SupplierReturnStatus status;

    /**
     * rejectReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String rejectReason;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * lines（类型：{@code List<SupplierReturnLine>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<SupplierReturnLine> lines;

    /**
     * events（类型：{@code List<DomainEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * 创建 SupplierReturnAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param returnNo 可追踪业务编码，类型为 {@code String}
     * @param sourceOrderNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code SupplierReturnStatus}
     * @param rejectReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param lines 业务处理参数或成员，类型为 {@code List<SupplierReturnLine>}
     */
    public SupplierReturnAggregate(long id, String returnNo, String sourceOrderNo, long supplierId, long purchaseOrgId, String warehouseCode, SupplierReturnStatus status, String rejectReason, int version, List<SupplierReturnLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "退供申请必须至少包含一行");
        }
        this.id = id;
        this.returnNo = returnNo;
        this.sourceOrderNo = sourceOrderNo;
        this.supplierId = supplierId;
        this.purchaseOrgId = purchaseOrgId;
        this.warehouseCode = warehouseCode;
        this.status = status;
        this.rejectReason = rejectReason;
        this.version = version;
        this.lines = new ArrayList<>(lines);
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceOrderNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<SupplierReturnLine>}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 执行命令的结果，类型为 {@code SupplierReturnAggregate}
     */
    public static SupplierReturnAggregate create(String sourceOrderNo, long supplierId, long purchaseOrgId, String warehouseCode, List<SupplierReturnLine> lines, IdentifierGenerator ids) {
        var aggregate = new SupplierReturnAggregate(ids.nextId(), ids.nextCode("SRET"), sourceOrderNo, supplierId, purchaseOrgId, warehouseCode, SupplierReturnStatus.CREATED, null, 0, lines);
        aggregate.raise("SupplierReturnCreated", Map.of());
        return aggregate;
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void submit(IdentifierGenerator ids) {
        ensureStatus(SupplierReturnStatus.CREATED, SupplierReturnStatus.REJECTED);
        version++;
        status = SupplierReturnStatus.SUBMITTED;
        raise("SupplierReturnSubmitted", Map.of());
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param approved 业务处理参数或成员，类型为 {@code boolean}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void approve(boolean approved, String reason, IdentifierGenerator ids) {
        ensureStatus(SupplierReturnStatus.SUBMITTED);
        version++;
        if (approved) {
            status = SupplierReturnStatus.APPROVED;
            raise("SupplierReturnApproved", Map.of());
        } else {
            status = SupplierReturnStatus.REJECTED;
            rejectReason = reason;
            raise("SupplierReturnRejected", Map.of("reason", Objects.requireNonNullElse(reason, "")));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code notifyExecution}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param notifyMode 业务处理参数或成员，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void notifyExecution(String notifyMode, IdentifierGenerator ids) {
        ensureStatus(SupplierReturnStatus.APPROVED);
        version++;
        status = SupplierReturnStatus.EXECUTION_NOTIFIED;
        raise("SupplierReturnExecutionNotified", Map.of("notifyMode", Objects.requireNonNullElse(notifyMode, "EVENT")));
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
     * @param allowed 业务处理参数或成员，类型为 {@code SupplierReturnStatus}
     */
    private void ensureStatus(SupplierReturnStatus... allowed) {
        for (SupplierReturnStatus candidate : allowed) {
            if (status == candidate) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.STATE_CONFLICT, "当前退供状态不允许执行该操作");
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
        payload.put("returnId", id);
        payload.put("returnNo", returnNo);
        payload.put("sourceOrderNo", sourceOrderNo);
        payload.put("supplierId", supplierId);
        payload.put("purchaseOrgId", purchaseOrgId);
        payload.put("warehouseCode", Objects.requireNonNullElse(warehouseCode, ""));
        payload.put("status", status.code());
        payload.put("version", version);
        payload.putAll(extra);
        events.add(new DomainEvent(0, "PUR-" + eventType + "-" + id + "-" + version, eventType, "SUPPLIER_RETURN", Long.toString(id), version, OffsetDateTime.now(), payload));
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
     * 处理当前类型职责中的操作 {@code returnNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String returnNo() {
        return returnNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code sourceOrderNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String sourceOrderNo() {
        return sourceOrderNo;
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
     * 处理当前类型职责中的操作 {@code purchaseOrgId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long purchaseOrgId() {
        return purchaseOrgId;
    }

    /**
     * 处理当前类型职责中的操作 {@code warehouseCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String warehouseCode() {
        return warehouseCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierReturnStatus}
     */
    public SupplierReturnStatus status() {
        return status;
    }

    /**
     * 执行命令 {@code rejectReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code String}
     */
    public String rejectReason() {
        return rejectReason;
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
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<SupplierReturnLine>}
     */
    public List<SupplierReturnLine> lines() {
        return List.copyOf(lines);
    }
}
