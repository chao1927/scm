package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 库存事件失败查询和人工重放应用服务。
 *
 * <p>人工重放先落审计幂等记录，再从持久化的原始信封恢复事件。入站重放仍进入统一 Inbox，
 * 出站重放仍通过真实 Broker；任何异常都会在独立事务更新重放审计状态。
 *
 * @author SCM Team
 */
@Service
public class InventoryEventFailureApplicationService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 200;
    private static final int MAX_ERROR_LENGTH = 1000;

    private final InventoryEventFailureStore store;
    private final InventoryInboundEventApplicationService inbound;
    private final InventoryEventEnvelopeCodec codec;
    private final InventoryEventTransactions transactions;
    private final ObjectProvider<InventoryOutboundEventReplayer> outboundReplayers;

    public InventoryEventFailureApplicationService(
            InventoryEventFailureStore store,
            InventoryInboundEventApplicationService inbound,
            InventoryEventEnvelopeCodec codec,
            InventoryEventTransactions transactions,
            ObjectProvider<InventoryOutboundEventReplayer> outboundReplayers) {
        this.store = store;
        this.inbound = inbound;
        this.codec = codec;
        this.transactions = transactions;
        this.outboundReplayers = outboundReplayers;
    }

    /**
     * 分页查询失败事件。
     *
     * @param direction 事件方向
     * @param requestedPageNo 请求页码
     * @param requestedPageSize 请求每页数量
     * @return 失败事件分页
     */
    public PageResult<InventoryEventFailureStore.FailureEvent> failures(
            InventoryEventFailureStore.Direction direction,
            int requestedPageNo,
            int requestedPageSize) {
        int pageNo = Math.max(1, requestedPageNo);
        int pageSize = requestedPageSize <= 0
                ? DEFAULT_PAGE_SIZE
                : Math.min(requestedPageSize, MAX_PAGE_SIZE);
        int offset = (pageNo - 1) * pageSize;
        InventoryEventFailureStore.FailurePage page =
                store.failures(direction, offset, pageSize);
        return new PageResult<>(pageNo, pageSize, page.total(), page.records());
    }

    /**
     * 人工重放一个失败事件。
     *
     * @param command 重放命令
     * @return 重放结果
     */
    public ReplayResult replay(ReplayCommand command) {
        validate(command);
        InventoryEventFailureStore.ReplayRegistration registration =
                transactions.requiresNew(() -> store.registerReplay(
                        command.idempotencyKey(),
                        command.direction(),
                        command.eventCode(),
                        command.reason(),
                        command.operatorId()));
        if (!registration.newlyRegistered()) {
            return new ReplayResult(
                    registration.replayId(), false, registration.replayStatus(), "重放请求已处理");
        }
        try {
            InventoryEventFailureStore.FailureEvent event =
                    store.findFailure(command.direction(), command.eventCode());
            if (event == null) {
                throw new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "失败事件不存在或已处理");
            }
            replay(event);
            transactions.requiresNew(() -> {
                store.markReplaySucceeded(registration.replayId());
                return null;
            });
            return new ReplayResult(registration.replayId(), true, 2, "重放成功");
        } catch (RuntimeException exception) {
            transactions.requiresNew(() -> {
                store.markReplayFailed(
                        registration.replayId(),
                        errorMessage(exception));
                return null;
            });
            throw exception;
        }
    }

    private void replay(InventoryEventFailureStore.FailureEvent event) {
        if (event.direction() == InventoryEventFailureStore.Direction.INBOUND) {
            InventoryEventEnvelope envelope = codec.decode(event.rawJson());
            inbound.consume(envelope, event.rawJson());
            return;
        }
        InventoryOutboundEventReplayer replayer = outboundReplayers.getIfAvailable();
        if (replayer == null) {
            throw new BusinessException(
                    ErrorCode.EXTERNAL_CALL_FAILED,
                    "真实 RocketMQ 出站重放器不可用");
        }
        replayer.replay(event.eventCode());
    }

    private static void validate(ReplayCommand command) {
        if (command == null
                || command.direction() == null
                || blank(command.eventCode())
                || blank(command.idempotencyKey())
                || blank(command.reason())
                || command.operatorId() <= 0) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "人工重放参数不完整");
        }
    }

    private static String errorMessage(RuntimeException exception) {
        String value = exception.getMessage();
        if (value == null || value.isBlank()) {
            value = exception.getClass().getSimpleName();
        }
        return value.length() <= MAX_ERROR_LENGTH
                ? value
                : value.substring(0, MAX_ERROR_LENGTH);
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 人工重放命令。
     */
    public record ReplayCommand(
            InventoryEventFailureStore.Direction direction,
            String eventCode,
            String idempotencyKey,
            String reason,
            long operatorId) {
    }

    /**
     * 人工重放结果。
     */
    public record ReplayResult(
            long replayId,
            boolean replayed,
            int replayStatus,
            String message) {
    }
}
