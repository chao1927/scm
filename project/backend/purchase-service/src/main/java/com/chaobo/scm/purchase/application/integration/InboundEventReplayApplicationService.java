package com.chaobo.scm.purchase.application.integration;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * logs（类型：{@code InboundEventLogPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InboundEventLogPort logs;

    /**
     * handlers（类型：{@code Map<String,InboundEventReplayHandler>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final Map<String, InboundEventReplayHandler> handlers;

    /**
     * 创建 InboundEventReplayApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param logs 业务处理参数或成员，类型为 {@code InboundEventLogPort}
     * @param handlers 业务处理参数或成员，类型为 {@code List<InboundEventReplayHandler>}
     */
    public InboundEventReplayApplicationService(InboundEventLogPort logs, List<InboundEventReplayHandler> handlers) {
        this.logs = logs;
        this.handlers = handlers.stream().collect(Collectors.toMap(InboundEventReplayHandler::consumerName, Function.identity()));
    }

    /**
     * 执行命令 {@code replay}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param consumeLogId 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void replay(long consumeLogId, String reason, CommandContext context) {
        context.requirePermission("purchase:event:replay");
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "人工重放必须填写原因");
        }
        var event = logs.findForReplay(consumeLogId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "入站失败事件不存在"));
        var handler = handlers.get(event.consumerName());
        if (handler == null) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "没有可用的事件重放处理器: " + event.consumerName());
        }
        logs.markReplayRequested(consumeLogId, context.operatorId(), reason);
        handler.replay(event);
    }
}
