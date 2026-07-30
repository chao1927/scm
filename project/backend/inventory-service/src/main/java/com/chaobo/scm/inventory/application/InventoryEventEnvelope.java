package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 库存上下文统一业务事件信封。
 *
 * <p>信封元数据用于幂等、版本校验、聚合顺序控制和链路追踪；{@code payload} 仅保存事件类型自己的
 * 业务字段。消费端必须先验证信封版本，再把载荷转换为库存命令，未知版本不得猜测解析。
 *
 * @author SCM Team
 */
public record InventoryEventEnvelope(
        String eventId,
        String eventType,
        String eventVersion,
        String sourceContext,
        String sourceSystem,
        String aggregateType,
        String aggregateId,
        long aggregateVersion,
        String businessKey,
        String idempotencyKey,
        String occurredAt,
        String traceId,
        Map<String, Object> payload) {

    /**
     * 当前库存事件信封版本。
     */
    public static final String CURRENT_VERSION = "1.0";

    /**
     * 获取必填文本载荷字段。
     *
     * @param name 字段名
     * @return 非空文本值
     */
    public String requiredText(String name) {
        Object value = payloadValue(name);
        String text = value.toString();
        if (text.isBlank()) {
            throw invalidPayload(name);
        }
        return text;
    }

    /**
     * 获取可选文本载荷字段。
     *
     * @param name 字段名
     * @return 文本值；字段不存在时返回 {@code null}
     */
    public String optionalText(String name) {
        Object value = payload == null ? null : payload.get(name);
        return value == null ? null : value.toString();
    }

    /**
     * 获取必填长整型载荷字段。
     *
     * @param name 字段名
     * @return 长整型值
     */
    public long requiredLong(String name) {
        Object value = payloadValue(name);
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException exception) {
            throw invalidPayload(name);
        }
    }

    /**
     * 获取必填十进制载荷字段，避免浮点运算进入库存数量模型。
     *
     * @param name 字段名
     * @return 十进制数量
     */
    public BigDecimal requiredDecimal(String name) {
        Object value = payloadValue(name);
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException exception) {
            throw invalidPayload(name);
        }
    }

    private Object payloadValue(String name) {
        Object value = payload == null ? null : payload.get(name);
        if (value == null) {
            throw invalidPayload(name);
        }
        return value;
    }

    private static BusinessException invalidPayload(String name) {
        return new BusinessException(
                ErrorCode.VALIDATION_FAILED,
                "库存事件载荷缺少或错误字段: " + name);
    }
}
