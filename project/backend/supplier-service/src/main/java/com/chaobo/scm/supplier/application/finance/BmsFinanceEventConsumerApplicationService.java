package com.chaobo.scm.supplier.application.finance;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.integration.InboundEventPayloadStore;
import com.chaobo.scm.supplier.application.masterdata.MasterDataEventConsumeLogPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BmsFinanceEventConsumerApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class BmsFinanceEventConsumerApplicationService {

    /**
     * CONSUMER（类型：{@code String}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final String CONSUMER = "supplier-finance";

    /**
     * inbox（类型：{@code MasterDataEventConsumeLogPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataEventConsumeLogPort inbox;

    /**
     * finance（类型：{@code SupplierFinanceApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierFinanceApplicationService finance;

    /**
     * lifecycle（类型：{@code SupplierFinanceLifecycleApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierFinanceLifecycleApplicationService lifecycle;

    /**
     * payloads（类型：{@code InboundEventPayloadStore}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InboundEventPayloadStore payloads;

    /**
     * 创建 BmsFinanceEventConsumerApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param inbox 业务处理参数或成员，类型为 {@code MasterDataEventConsumeLogPort}
     * @param finance 业务处理参数或成员，类型为 {@code SupplierFinanceApplicationService}
     * @param lifecycle 业务处理参数或成员，类型为 {@code SupplierFinanceLifecycleApplicationService}
     * @param payloads 业务处理参数或成员，类型为 {@code InboundEventPayloadStore}
     */
    public BmsFinanceEventConsumerApplicationService(MasterDataEventConsumeLogPort inbox, SupplierFinanceApplicationService finance, SupplierFinanceLifecycleApplicationService lifecycle, InboundEventPayloadStore payloads) {
        this.inbox = inbox;
        this.finance = finance;
        this.lifecycle = lifecycle;
        this.payloads = payloads;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code BmsFinanceEvent}
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("PMD.SwitchStatementRule")
    public void consume(BmsFinanceEvent event) {
        var claim = inbox.claim("BMS", event.eventCode(), event.eventType(), CONSUMER, "BMS:" + event.eventCode());
        if (claim == MasterDataEventConsumeLogPort.ClaimResult.ALREADY_SUCCEEDED) {
            return;
        }
        if (claim == MasterDataEventConsumeLogPort.ClaimResult.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "财务事件正在处理");
        }
        payloads.save("BMS", event.eventCode(), CONSUMER, event);
        try {
            switch(event.eventType()) {
                case "BmsReconciliationIssued", "BmsReconciliationChanged" ->
                    finance.importStatement(event.statementNo(), event.supplierId(), event.currency(), event.amount(), event.sourceVersion());
                case "BmsReconciliationClosed" ->
                    lifecycle.closeFromBms(event.statementNo(), event.supplierId(), event.sourceVersion());
                case "BmsInvoiceValidated" ->
                    {
                        if (event.invoiceId() == null) {
                            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "发票ID不能为空");
                        }
                        finance.recordInvoiceValidation(event.invoiceId(), event.passed(), event.message());
                    }
                default ->
                    {
                        inbox.markSucceeded("BMS", event.eventCode(), CONSUMER, true);
                        return;
                    }
            }
            inbox.markSucceeded("BMS", event.eventCode(), CONSUMER, false);
        } catch (RuntimeException exception) {
            inbox.recordFailure("BMS", event.eventCode(), event.eventType(), CONSUMER, "BMS:" + event.eventCode(), exception.getMessage());
            throw exception;
        }
    }
}
