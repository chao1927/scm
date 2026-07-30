package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.inventory.infrastructure.persistence.InventoryEventMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

/**
 * StockTransferEventApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class StockTransferEventApplicationService {

    /**
     * inbox（类型：{@code InventoryEventMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InventoryEventMapper inbox;

    /**
     * transfers（类型：{@code StockTransferApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final StockTransferApplicationService transfers;

    /**
     * 创建 StockTransferEventApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param inbox 业务处理参数或成员，类型为 {@code InventoryEventMapper}
     * @param transfers 业务处理参数或成员，类型为 {@code StockTransferApplicationService}
     */
    public StockTransferEventApplicationService(InventoryEventMapper inbox, StockTransferApplicationService transfers) {
        this.inbox = inbox;
        this.transfers = transfers;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code EventEnvelope}
     * @return 执行命令的结果，类型为 {@code ConsumeResult}
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("PMD.SwitchStatementRule")
    public ConsumeResult consume(EventEnvelope event) {
        validate(event);
        InventoryEventMapper.InboxRow row = inbox.findInbox(event.sourceSystem(), event.eventCode());
        if (row == null) {
            inbox.insertInbox(event.sourceSystem(), event.eventCode(), event.eventType(), payload(event));
            row = inbox.findInbox(event.sourceSystem(), event.eventCode());
        }
        if (row.status() == CONSUME_VALUE_2) {
            return new ConsumeResult(true, "调拨事件已处理");
        }
        try {
            switch(event.eventType()) {
                case "TransferOutboundCompleted" ->
                    {
                        requireSource(event, "WMS");
                        transfers.recordOutbound(event.transferNo(), event.qty(), currentVersion(event.transferNo()));
                    }
                case "TransferInTransit" ->
                    {
                        requireSource(event, "TMS");
                        transfers.markInTransit(event.transferNo(), currentVersion(event.transferNo()));
                    }
                case "TransferReceived" ->
                    {
                        requireSource(event, "WMS");
                        transfers.receive(event.transferNo(), event.qty(), event.finalReceipt(), currentVersion(event.transferNo()));
                    }
                default ->
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "不支持的调拨事件");
            }
            inbox.markInboxSucceeded(row.id());
            return new ConsumeResult(false, "调拨事件处理成功");
        } catch (RuntimeException exception) {
            inbox.markInboxFailed(row.id(), message(exception));
            throw exception;
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code currentVersion}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    private int currentVersion(String transferNo) {
        return transfers.detail(transferNo).version();
    }

    /**
     * 校验业务约束 {@code validate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code EventEnvelope}
     */
    private static void validate(EventEnvelope event) {
        if (event == null || blank(event.sourceSystem()) || blank(event.eventCode()) || blank(event.eventType()) || blank(event.transferNo()) || event.version() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "调拨事件信封不完整");
        }
    }

    /**
     * 查询并返回 {@code requireSource}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param event 业务处理参数或成员，类型为 {@code EventEnvelope}
     * @param source 业务处理参数或成员，类型为 {@code String}
     */
    private static void requireSource(EventEnvelope event, String source) {
        if (!source.equalsIgnoreCase(event.sourceSystem())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "调拨事件来源不合法");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code payload}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code EventEnvelope}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String payload(EventEnvelope event) {
        return "{\"transferNo\":\"" + event.transferNo() + "\",\"qty\":" + (event.qty() == null ? "null" : event.qty()) + ",\"version\":" + event.version() + "}";
    }

    /**
     * 处理当前类型职责中的操作 {@code message}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param exception 业务处理参数或成员，类型为 {@code RuntimeException}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String message(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null ? exception.getClass().getSimpleName() : message.substring(0, Math.min(1000, message.length()));
    }

    /**
     * 处理当前类型职责中的操作 {@code blank}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * EventEnvelope。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record EventEnvelope(String sourceSystem, String eventCode, String eventType, String transferNo, BigDecimal qty, boolean finalReceipt, int version) {
    }

    /**
     * ConsumeResult。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ConsumeResult(boolean duplicated, String message) {
    }

    /**
     * 业务常量 {@code CONSUME_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int CONSUME_VALUE_2 = 2;
}
