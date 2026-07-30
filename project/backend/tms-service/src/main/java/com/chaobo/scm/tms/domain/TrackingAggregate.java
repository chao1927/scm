package com.chaobo.scm.tms.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TrackingAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class TrackingAggregate {

    /**
     * trackNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String trackNo;

    /**
     * waybillNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String waybillNo;

    /**
     * nodeCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String nodeCode;

    /**
     * description（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String description;

    /**
     * location（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String location;

    /**
     * trackAt（类型：{@code LocalDateTime}）。
     *
     * <p>保存当前对象所需的业务时间；其具体生命周期由所属对象统一管理。
     */
    private final LocalDateTime trackAt;

    /**
     * sourceType（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String sourceType;

    /**
     * rawEventId（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final String rawEventId;

    /**
     * manualReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String manualReason;

    /**
     * events（类型：{@code List<TmsEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<TmsEvent> events = new ArrayList<>();

    /**
     * 创建 TrackingAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param trackNo 可追踪业务编码，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param nodeCode 可追踪业务编码，类型为 {@code String}
     * @param description 业务处理参数或成员，类型为 {@code String}
     * @param location 业务处理参数或成员，类型为 {@code String}
     * @param trackAt 业务时间，类型为 {@code LocalDateTime}
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param rawEventId 业务或技术标识，类型为 {@code String}
     * @param manualReason 业务处理参数或成员，类型为 {@code String}
     */
    private TrackingAggregate(String trackNo, String waybillNo, String nodeCode, String description, String location, LocalDateTime trackAt, String sourceType, String rawEventId, String manualReason) {
        if (blank(trackNo) || blank(waybillNo) || blank(nodeCode) || blank(description) || trackAt == null || blank(sourceType)) {
            throw new IllegalArgumentException("tracking node references are required");
        }
        this.trackNo = trackNo;
        this.waybillNo = waybillNo;
        this.nodeCode = nodeCode;
        this.description = description;
        this.location = location;
        this.trackAt = trackAt;
        this.sourceType = sourceType;
        this.rawEventId = rawEventId;
        this.manualReason = manualReason;
    }

    /**
     * 处理当前类型职责中的操作 {@code append}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param trackNo 可追踪业务编码，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param nodeCode 可追踪业务编码，类型为 {@code String}
     * @param description 业务处理参数或成员，类型为 {@code String}
     * @param location 业务处理参数或成员，类型为 {@code String}
     * @param trackAt 业务时间，类型为 {@code LocalDateTime}
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param rawEventId 业务或技术标识，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TrackingAggregate}
     */
    public static TrackingAggregate append(String trackNo, String waybillNo, String nodeCode, String description, String location, LocalDateTime trackAt, String sourceType, String rawEventId) {
        TrackingAggregate aggregate = new TrackingAggregate(trackNo, waybillNo, nodeCode, description, location, trackAt, sourceType, rawEventId, null);
        aggregate.events.add(TmsEvent.of("TrackingAppended", trackNo, waybillNo + "|" + nodeCode));
        if (ARRIVED.equals(nodeCode)) {
            aggregate.events.add(TmsEvent.of("TransportArrived", waybillNo, location == null ? "" : location));
        }
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code supplement}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param trackNo 可追踪业务编码，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param nodeCode 可追踪业务编码，类型为 {@code String}
     * @param description 业务处理参数或成员，类型为 {@code String}
     * @param location 业务处理参数或成员，类型为 {@code String}
     * @param trackAt 业务时间，类型为 {@code LocalDateTime}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TrackingAggregate}
     */
    public static TrackingAggregate supplement(String trackNo, String waybillNo, String nodeCode, String description, String location, LocalDateTime trackAt, String reason) {
        if (blank(reason)) {
            throw new IllegalArgumentException("supplement reason is required");
        }
        TrackingAggregate aggregate = new TrackingAggregate(trackNo, waybillNo, nodeCode, description, location, trackAt, "MANUAL", null, reason);
        aggregate.events.add(TmsEvent.of("TrackingSupplemented", trackNo, waybillNo + "|" + nodeCode + "|" + reason));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<TmsEvent>}
     */
    public List<TmsEvent> pullEvents() {
        List<TmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    /**
     * 处理当前类型职责中的操作 {@code trackNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String trackNo() {
        return trackNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code waybillNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String waybillNo() {
        return waybillNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code nodeCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String nodeCode() {
        return nodeCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code description}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String description() {
        return description;
    }

    /**
     * 处理当前类型职责中的操作 {@code location}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String location() {
        return location;
    }

    /**
     * 处理当前类型职责中的操作 {@code trackAt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LocalDateTime}
     */
    public LocalDateTime trackAt() {
        return trackAt;
    }

    /**
     * 处理当前类型职责中的操作 {@code sourceType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String sourceType() {
        return sourceType;
    }

    /**
     * 处理当前类型职责中的操作 {@code rawEventId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String rawEventId() {
        return rawEventId;
    }

    /**
     * 处理当前类型职责中的操作 {@code manualReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String manualReason() {
        return manualReason;
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
     * 业务常量 {@code ARRIVED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String ARRIVED = "ARRIVED";
}
