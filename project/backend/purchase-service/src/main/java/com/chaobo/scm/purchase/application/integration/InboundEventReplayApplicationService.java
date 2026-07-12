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

@Service
public class InboundEventReplayApplicationService {
    private final InboundEventLogPort logs;
    private final Map<String, InboundEventReplayHandler> handlers;

    public InboundEventReplayApplicationService(InboundEventLogPort logs, List<InboundEventReplayHandler> handlers) {
        this.logs = logs;
        this.handlers = handlers.stream().collect(Collectors.toMap(InboundEventReplayHandler::consumerName,
                Function.identity()));
    }

    @Transactional
    public void replay(long consumeLogId, String reason, CommandContext context) {
        context.requirePermission("purchase:event:replay");
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "人工重放必须填写原因");
        }
        var event = logs.findForReplay(consumeLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "入站失败事件不存在"));
        var handler = handlers.get(event.consumerName());
        if (handler == null) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "没有可用的事件重放处理器: " + event.consumerName());
        }
        logs.markReplayRequested(consumeLogId, context.operatorId(), reason);
        handler.replay(event);
    }
}
