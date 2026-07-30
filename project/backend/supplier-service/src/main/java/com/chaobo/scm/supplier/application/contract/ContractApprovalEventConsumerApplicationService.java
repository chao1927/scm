package com.chaobo.scm.supplier.application.contract;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.integration.InboundEventPayloadStore;
import com.chaobo.scm.supplier.application.masterdata.MasterDataEventConsumeLogPort;
import com.chaobo.scm.supplier.application.shared.CommandContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

/**
 * ContractApprovalEventConsumerApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class ContractApprovalEventConsumerApplicationService {

    /**
     * CONSUMER（类型：{@code String}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final String CONSUMER = "supplier-contract-approval";

    /**
     * inbox（类型：{@code MasterDataEventConsumeLogPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataEventConsumeLogPort inbox;

    /**
     * payloads（类型：{@code InboundEventPayloadStore}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InboundEventPayloadStore payloads;

    /**
     * contracts（类型：{@code SupplierContractApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierContractApplicationService contracts;

    /**
     * 创建 ContractApprovalEventConsumerApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param inbox 业务处理参数或成员，类型为 {@code MasterDataEventConsumeLogPort}
     * @param payloads 业务处理参数或成员，类型为 {@code InboundEventPayloadStore}
     * @param contracts 业务处理参数或成员，类型为 {@code SupplierContractApplicationService}
     */
    public ContractApprovalEventConsumerApplicationService(MasterDataEventConsumeLogPort inbox, InboundEventPayloadStore payloads, SupplierContractApplicationService contracts) {
        this.inbox = inbox;
        this.payloads = payloads;
        this.contracts = contracts;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code ContractApprovalEvent}
     */
    @Transactional(rollbackFor = Exception.class)
    public void consume(ContractApprovalEvent event) {
        if (!IAM.equals(event.sourceSystem())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "合同审批事件来源必须为IAM");
        }
        var claim = inbox.claim("IAM", event.eventCode(), event.eventType(), CONSUMER, "IAM:" + event.eventCode());
        if (claim == MasterDataEventConsumeLogPort.ClaimResult.ALREADY_SUCCEEDED) {
            return;
        }
        if (claim == MasterDataEventConsumeLogPort.ClaimResult.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "合同审批事件正在处理");
        }
        payloads.save("IAM", event.eventCode(), CONSUMER, event);
        try {
            var c = new CommandContext(0, "IAM", 0, null, event.eventCode(), null, "IAM:" + event.eventCode(), Set.of("supplier:contract:approve"));
            if (event.approved()) {
                contracts.approve(event.contractId(), event.contractVersion(), c);
            } else {
                contracts.rejectApproval(event.contractId(), event.contractVersion(), event.comment(), c);
            }
            inbox.markSucceeded("IAM", event.eventCode(), CONSUMER, false);
        } catch (RuntimeException ex) {
            inbox.recordFailure("IAM", event.eventCode(), event.eventType(), CONSUMER, "IAM:" + event.eventCode(), ex.getMessage());
            throw ex;
        }
    }

    /**
     * 业务常量 {@code IAM}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String IAM = "IAM";
}
