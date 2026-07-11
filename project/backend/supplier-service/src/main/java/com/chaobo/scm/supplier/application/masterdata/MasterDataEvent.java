package com.chaobo.scm.supplier.application.masterdata;

import java.time.OffsetDateTime;
import java.util.Map;

/** 主数据上下文发布的事实；data 保存业务载荷，聚合版本用于处理乱序消息。 */
public record MasterDataEvent(
        String eventCode,
        String eventType,
        String sourceSystem,
        long aggregateId,
        long aggregateVersion,
        OffsetDateTime occurredAt,
        Map<String, Object> data) {

    public MasterDataEvent {
        if (eventCode == null || eventCode.isBlank()) throw new IllegalArgumentException("主数据事件编号不能为空");
        if (eventType == null || eventType.isBlank()) throw new IllegalArgumentException("主数据事件类型不能为空");
        if (sourceSystem == null || sourceSystem.isBlank()) throw new IllegalArgumentException("主数据来源不能为空");
        data = data == null ? Map.of() : Map.copyOf(data);
    }
}
