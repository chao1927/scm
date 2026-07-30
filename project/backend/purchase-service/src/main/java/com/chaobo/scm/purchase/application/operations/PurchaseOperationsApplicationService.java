package com.chaobo.scm.purchase.application.operations;

import com.chaobo.scm.purchase.infrastructure.persistence.integration.PurchaseOperationsMapper;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * PurchaseOperationsApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class PurchaseOperationsApplicationService {

    /**
     * mapper（类型：{@code PurchaseOperationsMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseOperationsMapper mapper;

    /**
     * 创建 PurchaseOperationsApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code PurchaseOperationsMapper}
     */
    public PurchaseOperationsApplicationService(PurchaseOperationsMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code failedEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PurchaseOperationsViews.FailedEvent>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<PurchaseOperationsViews.FailedEvent> failedEvents() {
        return mapper.failedInboundEvents(100);
    }

    /**
     * 处理当前类型职责中的操作 {@code failedCommands}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PurchaseOperationsViews.FailedCommand>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<PurchaseOperationsViews.FailedCommand> failedCommands() {
        return mapper.failedCommands(100);
    }

    /**
     * 执行命令 {@code replayCommand}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param commandId 业务或技术标识，类型为 {@code long}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void replayCommand(long commandId, CommandContext context) {
        context.requirePermission("purchase:integration-command:replay");
        context.requiredIdempotencyKey();
        if (mapper.replayCommand(commandId) != 1) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "只有最终失败的集成命令可重放");
        }
    }
}
