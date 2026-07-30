package com.chaobo.scm.purchase.domain.orderchange;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.domain.shared.DomainEvent;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PurchaseOrderChangeAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class PurchaseOrderChangeAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * changeNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String changeNo;

    /**
     * orderNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String orderNo;

    /**
     * changeType（类型：{@code int}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final int changeType;

    /**
     * beforeSnapshot（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String beforeSnapshot;

    /**
     * afterSnapshot（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String afterSnapshot;

    /**
     * changeReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String changeReason;

    /**
     * status（类型：{@code PurchaseOrderChangeStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private PurchaseOrderChangeStatus status;

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
     * 创建 PurchaseOrderChangeAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param changeNo 可追踪业务编码，类型为 {@code String}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param changeType 业务处理参数或成员，类型为 {@code int}
     * @param beforeSnapshot 业务处理参数或成员，类型为 {@code String}
     * @param afterSnapshot 业务处理参数或成员，类型为 {@code String}
     * @param changeReason 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code PurchaseOrderChangeStatus}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    public PurchaseOrderChangeAggregate(long id, String changeNo, String orderNo, int changeType, String beforeSnapshot, String afterSnapshot, String changeReason, PurchaseOrderChangeStatus status, int version) {
        if (orderNo == null || orderNo.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购订单号不能为空");
        }
        if (changeReason == null || changeReason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "变更原因不能为空");
        }
        this.id = id;
        this.changeNo = changeNo;
        this.orderNo = orderNo;
        this.changeType = changeType;
        this.beforeSnapshot = beforeSnapshot;
        this.afterSnapshot = afterSnapshot;
        this.changeReason = changeReason;
        this.status = status;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param changeType 业务处理参数或成员，类型为 {@code int}
     * @param beforeSnapshot 业务处理参数或成员，类型为 {@code String}
     * @param afterSnapshot 业务处理参数或成员，类型为 {@code String}
     * @param changeReason 业务处理参数或成员，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 执行命令的结果，类型为 {@code PurchaseOrderChangeAggregate}
     */
    public static PurchaseOrderChangeAggregate create(String orderNo, int changeType, String beforeSnapshot, String afterSnapshot, String changeReason, IdentifierGenerator ids) {
        var aggregate = new PurchaseOrderChangeAggregate(ids.nextId(), ids.nextCode("POC"), orderNo, changeType, beforeSnapshot, afterSnapshot, changeReason, PurchaseOrderChangeStatus.PENDING_APPROVAL, 0);
        aggregate.raise("PurchaseOrderChangeCreated", Map.of());
        return aggregate;
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param approved 业务处理参数或成员，类型为 {@code boolean}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void approve(boolean approved, IdentifierGenerator ids) {
        if (status != PurchaseOrderChangeStatus.PENDING_APPROVAL) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "当前变更单状态不能审批");
        }
        version++;
        status = approved ? PurchaseOrderChangeStatus.EFFECTIVE : PurchaseOrderChangeStatus.REJECTED;
        raise(approved ? "PurchaseOrderChangeEffective" : "PurchaseOrderChangeRejected", Map.of());
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
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param extra 业务处理参数或成员，类型为 {@code Map<String,Object>}
     */
    private void raise(String eventType, Map<String, Object> extra) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("changeId", id);
        payload.put("changeNo", changeNo);
        payload.put("orderNo", orderNo);
        payload.put("changeType", changeType);
        payload.put("status", status.code());
        payload.put("version", version);
        payload.putAll(extra);
        events.add(new DomainEvent(0, "PUR-" + eventType + "-" + id + "-" + version, eventType, "PURCHASE_ORDER_CHANGE", Long.toString(id), version, OffsetDateTime.now(), payload));
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
     * 处理当前类型职责中的操作 {@code changeNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String changeNo() {
        return changeNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code orderNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String orderNo() {
        return orderNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code changeType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int changeType() {
        return changeType;
    }

    /**
     * 处理当前类型职责中的操作 {@code beforeSnapshot}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String beforeSnapshot() {
        return beforeSnapshot;
    }

    /**
     * 处理当前类型职责中的操作 {@code afterSnapshot}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String afterSnapshot() {
        return afterSnapshot;
    }

    /**
     * 处理当前类型职责中的操作 {@code changeReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String changeReason() {
        return changeReason;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchaseOrderChangeStatus}
     */
    public PurchaseOrderChangeStatus status() {
        return status;
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
