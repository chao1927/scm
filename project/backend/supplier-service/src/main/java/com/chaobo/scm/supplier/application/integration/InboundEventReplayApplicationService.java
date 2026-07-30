package com.chaobo.scm.supplier.application.integration;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.asn.event.*;
import com.chaobo.scm.supplier.application.contract.*;
import com.chaobo.scm.supplier.application.finance.*;
import com.chaobo.scm.supplier.application.masterdata.*;
import com.chaobo.scm.supplier.application.operations.*;
import com.chaobo.scm.supplier.application.order.*;
import com.chaobo.scm.supplier.application.quality.*;
import com.chaobo.scm.supplier.application.returning.*;
import com.chaobo.scm.supplier.application.rfq.*;
import com.chaobo.scm.supplier.application.score.*;
import com.chaobo.scm.supplier.application.shared.*;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * InboundEventReplayApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class InboundEventReplayApplicationService {

    /**
     * inbox（类型：{@code MasterDataEventConsumeLogPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataEventConsumeLogPort inbox;

    /**
     * json（类型：{@code ObjectMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ObjectMapper json;

    /**
     * wmsAsn（类型：{@code WmsAsnEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final WmsAsnEventConsumerApplicationService wmsAsn;

    /**
     * tmsAsn（类型：{@code TmsAsnEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final TmsAsnEventConsumerApplicationService tmsAsn;

    /**
     * rfq（类型：{@code RfqEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final RfqEventConsumerApplicationService rfq;

    /**
     * purchaseOrders（类型：{@code PurchaseOrderEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseOrderEventConsumerApplicationService purchaseOrders;

    /**
     * quality（类型：{@code QualitySourceEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final QualitySourceEventConsumerApplicationService quality;

    /**
     * returns（类型：{@code SupplierReturnEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierReturnEventConsumerApplicationService returns;

    /**
     * finance（类型：{@code BmsFinanceEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final BmsFinanceEventConsumerApplicationService finance;

    /**
     * performance（类型：{@code PerformanceFactEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PerformanceFactEventConsumerApplicationService performance;

    /**
     * operations（类型：{@code OperationsEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final OperationsEventConsumerApplicationService operations;

    /**
     * approvals（类型：{@code ContractApprovalEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ContractApprovalEventConsumerApplicationService approvals;

    /**
     * masterData（类型：{@code MasterDataEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataEventConsumerApplicationService masterData;

    /**
     * audit（类型：{@code AuditLogRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AuditLogRepository audit;

    /**
     * 创建 InboundEventReplayApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param inbox 业务处理参数或成员，类型为 {@code MasterDataEventConsumeLogPort}
     * @param json 业务处理参数或成员，类型为 {@code ObjectMapper}
     * @param wmsAsn 业务处理参数或成员，类型为 {@code WmsAsnEventConsumerApplicationService}
     * @param tmsAsn 业务处理参数或成员，类型为 {@code TmsAsnEventConsumerApplicationService}
     * @param rfq 业务处理参数或成员，类型为 {@code RfqEventConsumerApplicationService}
     * @param purchaseOrders 业务处理参数或成员，类型为 {@code PurchaseOrderEventConsumerApplicationService}
     * @param quality 业务处理参数或成员，类型为 {@code QualitySourceEventConsumerApplicationService}
     * @param returns 业务处理参数或成员，类型为 {@code SupplierReturnEventConsumerApplicationService}
     * @param finance 业务处理参数或成员，类型为 {@code BmsFinanceEventConsumerApplicationService}
     * @param performance 业务处理参数或成员，类型为 {@code PerformanceFactEventConsumerApplicationService}
     * @param operations 业务处理参数或成员，类型为 {@code OperationsEventConsumerApplicationService}
     * @param approvals 业务处理参数或成员，类型为 {@code ContractApprovalEventConsumerApplicationService}
     * @param masterData 业务处理参数或成员，类型为 {@code MasterDataEventConsumerApplicationService}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     */
    public InboundEventReplayApplicationService(MasterDataEventConsumeLogPort inbox, ObjectMapper json, WmsAsnEventConsumerApplicationService wmsAsn, TmsAsnEventConsumerApplicationService tmsAsn, RfqEventConsumerApplicationService rfq, PurchaseOrderEventConsumerApplicationService purchaseOrders, QualitySourceEventConsumerApplicationService quality, SupplierReturnEventConsumerApplicationService returns, BmsFinanceEventConsumerApplicationService finance, PerformanceFactEventConsumerApplicationService performance, OperationsEventConsumerApplicationService operations, ContractApprovalEventConsumerApplicationService approvals, MasterDataEventConsumerApplicationService masterData, AuditLogRepository audit) {
        this.inbox = inbox;
        this.json = json;
        this.wmsAsn = wmsAsn;
        this.tmsAsn = tmsAsn;
        this.rfq = rfq;
        this.purchaseOrders = purchaseOrders;
        this.quality = quality;
        this.returns = returns;
        this.finance = finance;
        this.performance = performance;
        this.operations = operations;
        this.approvals = approvals;
        this.masterData = masterData;
        this.audit = audit;
    }

    /**
     * 执行命令 {@code replay}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     */
    public void replay(long id, String reason, CommandContext context) {
        context.requirePermission("supplier:event:replay");
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "重放原因不能为空");
        }
        var event = inbox.findForReplay(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "入站事件不存在"));
        if (event.status() != REPLAY_VALUE_3 || event.payloadJson() == null) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "只有已保存载荷的失败事件可重放");
        }
        inbox.markReplayRequested(id, context.operatorId(), reason);
        dispatch(event);
        audit.save(context, "REPLAY_INBOUND_EVENT", "INBOUND_EVENT", id, event.eventCode(), null, "{\"consumer\":\"" + event.consumerName() + "\"}");
    }

    /**
     * 执行命令 {@code dispatch}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code MasterDataEventConsumeLogPort.ReplayEvent}
     */
    @SuppressWarnings("PMD.SwitchStatementRule")
    private void dispatch(MasterDataEventConsumeLogPort.ReplayEvent event) {
        switch(event.consumerName()) {
            case "supplier-asn-wms" ->
                wmsAsn.consume(read(event, WmsAsnEvent.class));
            case "supplier-asn-tms" ->
                tmsAsn.consume(read(event, TmsAsnEvent.class));
            case "supplier-rfq-todo" ->
                rfq.consume(read(event, RfqEvent.class));
            case PurchaseOrderEventConsumerApplicationService.CONSUMER ->
                purchaseOrders.consume(read(event, PurchaseOrderEvent.class));
            case "supplier-quality-source" ->
                quality.consume(read(event, QualitySourceEvent.class));
            case "supplier-return" ->
                returns.consume(read(event, SupplierReturnExternalEvent.class));
            case "supplier-finance" ->
                finance.consume(read(event, BmsFinanceEvent.class));
            case "supplier-performance-fact" ->
                performance.consume(read(event, PerformanceFactEvent.class));
            case "supplier-operations" ->
                operations.consume(read(event, OperationsEvent.class));
            case ContractApprovalEventConsumerApplicationService.CONSUMER ->
                approvals.consume(read(event, ContractApprovalEvent.class));
            case MasterDataEventConsumerApplicationService.CONSUMER_NAME ->
                masterData.consume(read(event, MasterDataEvent.class));
            default ->
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "未注册的入站事件消费者: " + event.consumerName());
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code read}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code MasterDataEventConsumeLogPort.ReplayEvent}
     * @param type 业务处理参数或成员，类型为 {@code Class<T>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code T}
     */
    private <T> T read(MasterDataEventConsumeLogPort.ReplayEvent event, Class<T> type) {
        try {
            return json.readValue(event.payloadJson(), type);
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "入站事件载荷无法反序列化: " + event.eventCode());
        }
    }

    /**
     * 业务常量 {@code REPLAY_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int REPLAY_VALUE_3 = 3;
}
