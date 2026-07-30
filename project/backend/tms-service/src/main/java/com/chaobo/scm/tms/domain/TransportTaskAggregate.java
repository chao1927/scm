package com.chaobo.scm.tms.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * TransportTaskAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class TransportTaskAggregate {

    /**
     * PENDING_ACCEPT（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PENDING_ACCEPT = 1;

    /**
     * ACCEPTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int ACCEPTED = 2;

    /**
     * CANCELLED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int CANCELLED = 3;

    /**
     * IN_TRANSIT（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int IN_TRANSIT = 4;

    /**
     * DELIVERED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int DELIVERED = 5;

    /**
     * taskNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String taskNo;

    /**
     * sourceSystem（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String sourceSystem;

    /**
     * sourceOrderNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String sourceOrderNo;

    /**
     * sourceLineNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String sourceLineNo;

    /**
     * scenario（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String scenario;

    /**
     * shipperId（类型：{@code Long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final Long shipperId;

    /**
     * warehouseId（类型：{@code Long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final Long warehouseId;

    /**
     * originAddress（类型：{@code Address}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final Address originAddress;

    /**
     * destinationAddress（类型：{@code Address}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final Address destinationAddress;

    /**
     * packages（类型：{@code List<PackageItem>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<PackageItem> packages;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * carrierCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private String carrierCode;

    /**
     * carrierName（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String carrierName;

    /**
     * logisticsProductCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private String logisticsProductCode;

    /**
     * feeResponsibility（类型：{@code String}）。
     *
     * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
     */
    private String feeResponsibility;

    /**
     * version（类型：{@code long}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private long version;

    /**
     * events（类型：{@code List<TmsEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<TmsEvent> events = new ArrayList<>();

    /**
     * 创建 TransportTaskAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param sourceOrderNo 可追踪业务编码，类型为 {@code String}
     * @param sourceLineNo 可追踪业务编码，类型为 {@code String}
     * @param scenario 业务处理参数或成员，类型为 {@code String}
     * @param shipperId 业务或技术标识，类型为 {@code Long}
     * @param warehouseId 业务或技术标识，类型为 {@code Long}
     * @param originAddress 业务处理参数或成员，类型为 {@code Address}
     * @param destinationAddress 业务处理参数或成员，类型为 {@code Address}
     * @param packages 业务处理参数或成员，类型为 {@code List<PackageItem>}
     * @param status 生命周期状态，类型为 {@code int}
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param carrierName 业务处理参数或成员，类型为 {@code String}
     * @param logisticsProductCode 可追踪业务编码，类型为 {@code String}
     * @param feeResponsibility 金额或计费值，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private TransportTaskAggregate(String taskNo, String sourceSystem, String sourceOrderNo, String sourceLineNo, String scenario, Long shipperId, Long warehouseId, Address originAddress, Address destinationAddress, List<PackageItem> packages, int status, String carrierCode, String carrierName, String logisticsProductCode, String feeResponsibility, long version) {
        if (blank(taskNo) || blank(sourceSystem) || blank(sourceOrderNo) || blank(scenario)) {
            throw new IllegalArgumentException("transport task references are required");
        }
        if (!List.of(PURCHASE_INBOUND, SALES_OUTBOUND, AFTERSALE_RETURN, SUPPLIER_RETURN, TRANSFER).contains(scenario)) {
            throw new IllegalArgumentException("unsupported transport scenario");
        }
        if (shipperId == null || shipperId <= 0 || warehouseId == null || warehouseId <= 0) {
            throw new IllegalArgumentException("shipper and warehouse are required");
        }
        if (originAddress == null || destinationAddress == null) {
            throw new IllegalArgumentException("origin and destination addresses are required");
        }
        validatePackages(packages);
        this.taskNo = taskNo;
        this.sourceSystem = sourceSystem;
        this.sourceOrderNo = sourceOrderNo;
        this.sourceLineNo = sourceLineNo;
        this.scenario = scenario;
        this.shipperId = shipperId;
        this.warehouseId = warehouseId;
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.packages = new ArrayList<>(packages);
        this.status = status;
        this.carrierCode = carrierCode;
        this.carrierName = carrierName;
        this.logisticsProductCode = logisticsProductCode;
        this.feeResponsibility = feeResponsibility;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param sourceOrderNo 可追踪业务编码，类型为 {@code String}
     * @param sourceLineNo 可追踪业务编码，类型为 {@code String}
     * @param scenario 业务处理参数或成员，类型为 {@code String}
     * @param shipperId 业务或技术标识，类型为 {@code Long}
     * @param warehouseId 业务或技术标识，类型为 {@code Long}
     * @param originAddress 业务处理参数或成员，类型为 {@code Address}
     * @param destinationAddress 业务处理参数或成员，类型为 {@code Address}
     * @param packages 业务处理参数或成员，类型为 {@code List<PackageItem>}
     * @param logisticsProductCode 可追踪业务编码，类型为 {@code String}
     * @param feeResponsibility 金额或计费值，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code TransportTaskAggregate}
     */
    public static TransportTaskAggregate create(String taskNo, String sourceSystem, String sourceOrderNo, String sourceLineNo, String scenario, Long shipperId, Long warehouseId, Address originAddress, Address destinationAddress, List<PackageItem> packages, String logisticsProductCode, String feeResponsibility) {
        if (blank(logisticsProductCode) || blank(feeResponsibility)) {
            throw new IllegalArgumentException("logistics product and fee responsibility are required");
        }
        TransportTaskAggregate aggregate = new TransportTaskAggregate(taskNo, sourceSystem, sourceOrderNo, sourceLineNo, scenario, shipperId, warehouseId, originAddress, destinationAddress, packages, PENDING_ACCEPT, null, null, logisticsProductCode, feeResponsibility, 1);
        aggregate.events.add(TmsEvent.of("TransportTaskCreated", taskNo, sourceSystem + "|" + sourceOrderNo + "|" + scenario));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param sourceOrderNo 可追踪业务编码，类型为 {@code String}
     * @param sourceLineNo 可追踪业务编码，类型为 {@code String}
     * @param scenario 业务处理参数或成员，类型为 {@code String}
     * @param shipperId 业务或技术标识，类型为 {@code Long}
     * @param warehouseId 业务或技术标识，类型为 {@code Long}
     * @param originAddress 业务处理参数或成员，类型为 {@code Address}
     * @param destinationAddress 业务处理参数或成员，类型为 {@code Address}
     * @param packages 业务处理参数或成员，类型为 {@code List<PackageItem>}
     * @param status 生命周期状态，类型为 {@code int}
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param carrierName 业务处理参数或成员，类型为 {@code String}
     * @param logisticsProductCode 可追踪业务编码，类型为 {@code String}
     * @param feeResponsibility 金额或计费值，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransportTaskAggregate}
     */
    public static TransportTaskAggregate restore(String taskNo, String sourceSystem, String sourceOrderNo, String sourceLineNo, String scenario, Long shipperId, Long warehouseId, Address originAddress, Address destinationAddress, List<PackageItem> packages, int status, String carrierCode, String carrierName, String logisticsProductCode, String feeResponsibility, long version) {
        return new TransportTaskAggregate(taskNo, sourceSystem, sourceOrderNo, sourceLineNo, scenario, shipperId, warehouseId, originAddress, destinationAddress, packages, status, carrierCode, carrierName, logisticsProductCode, feeResponsibility, version);
    }

    /**
     * 处理当前类型职责中的操作 {@code accept}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param carrierName 业务处理参数或成员，类型为 {@code String}
     * @param logisticsProductCode 可追踪业务编码，类型为 {@code String}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void accept(String carrierCode, String carrierName, String logisticsProductCode, long expectedVersion) {
        if (status != PENDING_ACCEPT) {
            throw new IllegalStateException("transport task is not pending accept");
        }
        if (version != expectedVersion) {
            throw new IllegalStateException("transport task version conflict");
        }
        if (blank(carrierCode) || blank(carrierName) || blank(logisticsProductCode)) {
            throw new IllegalArgumentException("carrier and logistics product are required");
        }
        this.carrierCode = carrierCode;
        this.carrierName = carrierName;
        this.logisticsProductCode = logisticsProductCode;
        status = ACCEPTED;
        version++;
        events.add(TmsEvent.of("TransportTaskAccepted", taskNo, carrierCode + "|" + logisticsProductCode));
    }

    /**
     * 处理当前类型职责中的操作 {@code start}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void start(long expectedVersion) {
        requireState(ACCEPTED, expectedVersion, "transport task is not accepted");
        status = IN_TRANSIT;
        version++;
        events.add(TmsEvent.of("TRANSFER".equals(scenario) ? "TransferInTransit" : "TransportStarted", taskNo, eventPayload()));
    }

    /**
     * 处理当前类型职责中的操作 {@code deliver}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void deliver(long expectedVersion) {
        requireState(IN_TRANSIT, expectedVersion, "transport task is not in transit");
        status = DELIVERED;
        version++;
        events.add(TmsEvent.of("TRANSFER".equals(scenario) ? "TransferDelivered" : "TransportDelivered", taskNo, eventPayload()));
    }

    /**
     * 处理当前类型职责中的操作 {@code eventPayload}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String eventPayload() {
        return "TRANSFER".equals(scenario) ? "{\"transferNo\":\"" + sourceOrderNo + "\",\"version\":" + version + "}" : sourceOrderNo;
    }

    /**
     * 查询并返回 {@code requireState}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param expectedStatus 生命周期状态，类型为 {@code int}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     * @param message 业务处理参数或成员，类型为 {@code String}
     */
    private void requireState(int expectedStatus, long expectedVersion, String message) {
        if (status != expectedStatus) {
            throw new IllegalStateException(message);
        }
        if (version != expectedVersion) {
            throw new IllegalStateException("transport task version conflict");
        }
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
     * 处理当前类型职责中的操作 {@code taskNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String taskNo() {
        return taskNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code sourceSystem}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String sourceSystem() {
        return sourceSystem;
    }

    /**
     * 处理当前类型职责中的操作 {@code sourceOrderNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String sourceOrderNo() {
        return sourceOrderNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code sourceLineNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String sourceLineNo() {
        return sourceLineNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code scenario}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String scenario() {
        return scenario;
    }

    /**
     * 处理当前类型职责中的操作 {@code shipperId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    public Long shipperId() {
        return shipperId;
    }

    /**
     * 处理当前类型职责中的操作 {@code warehouseId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    public Long warehouseId() {
        return warehouseId;
    }

    /**
     * 处理当前类型职责中的操作 {@code originAddress}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Address}
     */
    public Address originAddress() {
        return originAddress;
    }

    /**
     * 处理当前类型职责中的操作 {@code destinationAddress}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Address}
     */
    public Address destinationAddress() {
        return destinationAddress;
    }

    /**
     * 处理当前类型职责中的操作 {@code packages}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PackageItem>}
     */
    public List<PackageItem> packages() {
        return List.copyOf(packages);
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int status() {
        return status;
    }

    /**
     * 处理当前类型职责中的操作 {@code carrierCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String carrierCode() {
        return carrierCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code carrierName}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String carrierName() {
        return carrierName;
    }

    /**
     * 处理当前类型职责中的操作 {@code logisticsProductCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String logisticsProductCode() {
        return logisticsProductCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code feeResponsibility}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String feeResponsibility() {
        return feeResponsibility;
    }

    /**
     * 处理当前类型职责中的操作 {@code version}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long version() {
        return version;
    }

    /**
     * 校验业务约束 {@code validatePackages}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param packages 业务处理参数或成员，类型为 {@code List<PackageItem>}
     */
    private static void validatePackages(List<PackageItem> packages) {
        if (packages == null || packages.isEmpty()) {
            throw new IllegalArgumentException("transport packages are required");
        }
        for (PackageItem item : packages) {
            if (item == null || blank(item.packageNo()) || item.quantity() == null || item.quantity().signum() <= 0) {
                throw new IllegalArgumentException("invalid transport package");
            }
            if (item.weightKg() != null && item.weightKg().signum() < 0) {
                throw new IllegalArgumentException("package weight cannot be negative");
            }
        }
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
     * Address。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Address(String province, String city, String district, String detail, String contactName, String contactPhone) {

        public Address {
            if (blank(province) || blank(city) || blank(detail) || blank(contactName) || blank(contactPhone)) {
                throw new IllegalArgumentException("address province, city, detail, contact and phone are required");
            }
        }
    }

    /**
     * PackageItem。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record PackageItem(String packageNo, BigDecimal quantity, BigDecimal weightKg, BigDecimal volumeCbm) {
    }

    /**
     * 业务常量 {@code AFTERSALE_RETURN}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String AFTERSALE_RETURN = "AFTERSALE_RETURN";

    /**
     * 业务常量 {@code PURCHASE_INBOUND}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String PURCHASE_INBOUND = "PURCHASE_INBOUND";

    /**
     * 业务常量 {@code SALES_OUTBOUND}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String SALES_OUTBOUND = "SALES_OUTBOUND";

    /**
     * 业务常量 {@code SUPPLIER_RETURN}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String SUPPLIER_RETURN = "SUPPLIER_RETURN";

    /**
     * 业务常量 {@code TRANSFER}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String TRANSFER = "TRANSFER";
}
