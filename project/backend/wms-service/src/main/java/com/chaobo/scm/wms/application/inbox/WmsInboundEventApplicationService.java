package com.chaobo.scm.wms.application.inbox;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.inbound.InboundOrderApplicationService;
import com.chaobo.scm.wms.application.outbound.OutboundApplicationService;
import com.chaobo.scm.wms.application.transfer.TransferOperationApplicationService;
import com.chaobo.scm.wms.application.returning.ReturnOperationApplicationService;
import com.chaobo.scm.wms.infrastructure.persistence.event.WmsInboxMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * WmsInboundEventApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class WmsInboundEventApplicationService {

    /**
     * inbox（类型：{@code WmsInboxMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final WmsInboxMapper inbox;

    /**
     * inboundOrders（类型：{@code InboundOrderApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InboundOrderApplicationService inboundOrders;

    /**
     * outboundOrders（类型：{@code OutboundApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final OutboundApplicationService outboundOrders;

    /**
     * objectMapper（类型：{@code ObjectMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final ObjectMapper objectMapper;

    /**
     * transfers（类型：{@code TransferOperationApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final TransferOperationApplicationService transfers;

    /**
     * returns（类型：{@code ReturnOperationApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ReturnOperationApplicationService returns;

    /**
     * 创建 WmsInboundEventApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param inbox 业务处理参数或成员，类型为 {@code WmsInboxMapper}
     * @param inboundOrders 业务处理参数或成员，类型为 {@code InboundOrderApplicationService}
     * @param outboundOrders 业务处理参数或成员，类型为 {@code OutboundApplicationService}
     * @param objectMapper 持久化访问依赖，类型为 {@code ObjectMapper}
     * @param transfers 业务处理参数或成员，类型为 {@code TransferOperationApplicationService}
     * @param returns 业务处理参数或成员，类型为 {@code ReturnOperationApplicationService}
     */
    public WmsInboundEventApplicationService(WmsInboxMapper inbox, InboundOrderApplicationService inboundOrders, OutboundApplicationService outboundOrders, ObjectMapper objectMapper, TransferOperationApplicationService transfers, ReturnOperationApplicationService returns) {
        this.inbox = inbox;
        this.inboundOrders = inboundOrders;
        this.outboundOrders = outboundOrders;
        this.objectMapper = objectMapper;
        this.transfers = transfers;
        this.returns = returns;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param envelope 业务处理参数或成员，类型为 {@code EventEnvelope}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code ConsumeResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsumeResult consume(EventEnvelope envelope, long operatorId) {
        validate(envelope);
        var existed = inbox.find(envelope.sourceSystem(), envelope.eventCode());
        if (existed == null) {
            inbox.insert(envelope.sourceSystem(), envelope.eventCode(), envelope.eventType(), envelope.payload());
            existed = inbox.find(envelope.sourceSystem(), envelope.eventCode());
        }
        if (existed.status() == CONSUME_VALUE_2) {
            return new ConsumeResult(true, "事件已处理");
        }
        try {
            dispatch(existed, operatorId);
            inbox.markSucceeded(existed.id());
            return new ConsumeResult(false, "处理成功");
        } catch (RuntimeException ex) {
            inbox.markFailed(existed.id(), trim(ex.getMessage()));
            throw ex;
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code failedEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<FailedEventView>}
     */
    public List<FailedEventView> failedEvents(int limit) {
        int batchSize = limit <= 0 ? 50 : Math.min(limit, 200);
        return inbox.failed(batchSize).stream().map(FailedEventView::from).toList();
    }

    /**
     * 执行命令 {@code replay}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param inboxId 业务或技术标识，类型为 {@code long}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code ConsumeResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsumeResult replay(long inboxId, long operatorId) {
        var row = inbox.failed(200).stream().filter(event -> event.id() == inboxId).findFirst().orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "失败入站事件不存在"));
        try {
            dispatch(row, operatorId);
            inbox.markSucceeded(row.id());
            return new ConsumeResult(false, "重放成功");
        } catch (RuntimeException ex) {
            inbox.markFailed(row.id(), trim(ex.getMessage()));
            throw ex;
        }
    }

    /**
     * 执行命令 {@code dispatch}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code WmsInboxMapper.Row}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @SuppressWarnings("PMD.SwitchStatementRule")
    private void dispatch(WmsInboxMapper.Row row, long operatorId) {
        var payload = readPayload(row.payload());
        switch(row.eventType()) {
            case "CreateInboundOrderRequested" ->
                inboundOrders.create(new InboundOrderApplicationService.Create(text(payload, "sourceType"),
                    text(payload, "sourceNo"), longValue(payload, "warehouseId"), longValue(payload, "ownerId"),
                    offsetDateTime(payload, "expectedArrivalAt"), row.eventCode()), operatorId);
            case "CreateOutboundOrderRequested" ->
                outboundOrders.create(text(payload, "sourceType"), text(payload, "sourceNo"),
                    longValue(payload, "warehouseId"), longValue(payload, "ownerId"), operatorId);
            case "TransferStockReserved" ->
                {
                    requireSource(row, "INVENTORY");
                    transfers.create(new TransferOperationApplicationService.Create(text(payload, "transferNo"), longValue(payload, "ownerId"), longValue(payload, "sourceWarehouseId"), longValue(payload, "targetWarehouseId"), text(payload, "sku"), nullableText(payload, "batchNo"), decimal(payload, "requestedQty")), operatorId);
                }
            case "TransferInTransit" ->
                {
                    requireSource(row, "INVENTORY");
                    transfers.prepareInbound(text(payload, "transferNo"), intValue(payload, "version"), operatorId);
                }
            case "TransferCancelled" ->
                {
                    requireSource(row, "INVENTORY");
                    transfers.cancel(text(payload, "transferNo"));
                }
            case "CreateReturnInboundRequested" ->
                {
                    requireSource(row, "OMS");
                    returns.create(new ReturnOperationApplicationService.Create(text(payload, "afterSaleNo"), text(payload, "rmaNo"), longValue(payload, "ownerId"), longValue(payload, "returnWarehouseId"), text(payload, "sku"), nullableText(payload, "batchNo"), decimal(payload, "qty")), operatorId);
                }
            default ->
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "不支持的WMS入站事件类型");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code readPayload}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param payload 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code JsonNode}
     */
    private JsonNode readPayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "入站事件载荷不是合法JSON");
        }
    }

    /**
     * 校验业务约束 {@code validate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param envelope 业务处理参数或成员，类型为 {@code EventEnvelope}
     */
    private static void validate(EventEnvelope envelope) {
        if (envelope.sourceSystem() == null || envelope.sourceSystem().isBlank() || envelope.eventCode() == null || envelope.eventCode().isBlank() || envelope.eventType() == null || envelope.eventType().isBlank() || envelope.payload() == null || envelope.payload().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "入站事件信封缺少必填字段");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code text}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param payload 业务处理参数或成员，类型为 {@code JsonNode}
     * @param field 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String text(JsonNode payload, String field) {
        var value = payload.get(field);
        if (value == null || value.asText().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "入站事件载荷缺少字段: " + field);
        }
        return value.asText();
    }

    /**
     * 处理当前类型职责中的操作 {@code longValue}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param payload 业务处理参数或成员，类型为 {@code JsonNode}
     * @param field 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    private static long longValue(JsonNode payload, String field) {
        var value = payload.get(field);
        if (value == null || !value.canConvertToLong() || value.asLong() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "入站事件载荷字段不合法: " + field);
        }
        return value.asLong();
    }

    /**
     * 处理当前类型职责中的操作 {@code intValue}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param payload 业务处理参数或成员，类型为 {@code JsonNode}
     * @param field 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    private static int intValue(JsonNode payload, String field) {
        var value = payload.get(field);
        if (value == null || !value.canConvertToInt() || value.asInt() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "入站事件载荷字段不合法: " + field);
        }
        return value.asInt();
    }

    /**
     * 处理当前类型职责中的操作 {@code decimal}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param payload 业务处理参数或成员，类型为 {@code JsonNode}
     * @param field 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code java.math.BigDecimal}
     */
    private static java.math.BigDecimal decimal(JsonNode payload, String field) {
        var value = payload.get(field);
        if (value == null || !value.isNumber() || value.decimalValue().signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "入站事件载荷字段不合法: " + field);
        }
        return value.decimalValue();
    }

    /**
     * 处理当前类型职责中的操作 {@code nullableText}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param payload 业务处理参数或成员，类型为 {@code JsonNode}
     * @param field 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String nullableText(JsonNode payload, String field) {
        var value = payload.get(field);
        return value == null || value.isNull() || value.asText().isBlank() ? null : value.asText();
    }

    /**
     * 查询并返回 {@code requireSource}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param row 业务处理参数或成员，类型为 {@code WmsInboxMapper.Row}
     * @param expected 业务处理参数或成员，类型为 {@code String}
     */
    private static void requireSource(WmsInboxMapper.Row row, String expected) {
        if (!expected.equalsIgnoreCase(row.sourceSystem())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "WMS调拨事件来源不合法");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code offsetDateTime}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param payload 业务处理参数或成员，类型为 {@code JsonNode}
     * @param field 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OffsetDateTime}
     */
    private static OffsetDateTime offsetDateTime(JsonNode payload, String field) {
        var value = payload.get(field);
        if (value == null || value.asText().isBlank()) {
            return null;
        }
        return OffsetDateTime.parse(value.asText());
    }

    /**
     * 处理当前类型职责中的操作 {@code trim}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param message 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String trim(String message) {
        if (message == null) {
            return "UNKNOWN_ERROR";
        }
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }

    /**
     * EventEnvelope。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record EventEnvelope(String sourceSystem, String eventCode, String eventType, String payload) {
    }

    /**
     * ConsumeResult。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ConsumeResult(boolean duplicated, String message) {
    }

    /**
     * FailedEventView。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record FailedEventView(long id, String sourceSystem, String eventCode, String eventType, int retryCount, String lastError) {

        /**
         * 转换数据模型 {@code from}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param row 业务处理参数或成员，类型为 {@code WmsInboxMapper.Row}
         * @return 转换数据模型的结果，类型为 {@code FailedEventView}
         */
        static FailedEventView from(WmsInboxMapper.Row row) {
            return new FailedEventView(row.id(), row.sourceSystem(), row.eventCode(), row.eventType(), row.retryCount(), row.lastError());
        }
    }

    /**
     * 业务常量 {@code CONSUME_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int CONSUME_VALUE_2 = 2;
}
