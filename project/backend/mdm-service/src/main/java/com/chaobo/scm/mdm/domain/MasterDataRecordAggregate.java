package com.chaobo.scm.mdm.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * MasterDataRecordAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class MasterDataRecordAggregate {

    /**
     * DRAFT（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int DRAFT = 1;

    /**
     * PENDING_REVIEW（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PENDING_REVIEW = 2;

    /**
     * ENABLED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int ENABLED = 3;

    /**
     * REJECTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int REJECTED = 4;

    /**
     * FROZEN（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int FROZEN = 5;

    /**
     * DISABLED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int DISABLED = 6;

    /**
     * recordNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String recordNo;

    /**
     * typeCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String typeCode;

    /**
     * dataCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String dataCode;

    /**
     * dataName（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String dataName;

    /**
     * dataPayload（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String dataPayload;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * currentVersionNo（类型：{@code int}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private int currentVersionNo;

    /**
     * reason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String reason;

    /**
     * version（类型：{@code long}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private long version;

    /**
     * events（类型：{@code List<MdmEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<MdmEvent> events = new ArrayList<>();

    /**
     * 创建 MasterDataRecordAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param dataCode 可追踪业务编码，类型为 {@code String}
     * @param dataName 业务处理参数或成员，类型为 {@code String}
     * @param dataPayload 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param currentVersionNo 可追踪业务编码，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private MasterDataRecordAggregate(String recordNo, String typeCode, String dataCode, String dataName, String dataPayload, int status, int currentVersionNo, String reason, long version) {
        if (blank(recordNo) || blank(typeCode) || blank(dataCode) || blank(dataName) || blank(dataPayload)) {
            throw new IllegalArgumentException("master data record references and payload are required");
        }
        this.recordNo = recordNo;
        this.typeCode = typeCode;
        this.dataCode = dataCode;
        this.dataName = dataName;
        this.dataPayload = dataPayload;
        this.status = status;
        this.currentVersionNo = currentVersionNo;
        this.reason = reason;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param dataCode 可追踪业务编码，类型为 {@code String}
     * @param dataName 业务处理参数或成员，类型为 {@code String}
     * @param dataPayload 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code MasterDataRecordAggregate}
     */
    public static MasterDataRecordAggregate create(String recordNo, String typeCode, String dataCode, String dataName, String dataPayload) {
        MasterDataRecordAggregate aggregate = new MasterDataRecordAggregate(recordNo, typeCode, dataCode, dataName, dataPayload, DRAFT, 0, null, 1);
        aggregate.events.add(MdmEvent.of("MasterDataDraftCreated", recordNo, typeCode + "|" + dataCode));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param dataCode 可追踪业务编码，类型为 {@code String}
     * @param dataName 业务处理参数或成员，类型为 {@code String}
     * @param dataPayload 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param currentVersionNo 可追踪业务编码，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MasterDataRecordAggregate}
     */
    public static MasterDataRecordAggregate restore(String recordNo, String typeCode, String dataCode, String dataName, String dataPayload, int status, int currentVersionNo, String reason, long version) {
        return new MasterDataRecordAggregate(recordNo, typeCode, dataCode, dataName, dataPayload, status, currentVersionNo, reason, version);
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param dataName 业务处理参数或成员，类型为 {@code String}
     * @param dataPayload 业务处理参数或成员，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void change(String dataName, String dataPayload, String reason, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != DRAFT && status != REJECTED) {
            throw new IllegalStateException("master data record is not editable");
        }
        if (blank(dataName) || blank(dataPayload)) {
            throw new IllegalArgumentException("data name and payload are required");
        }
        this.dataName = dataName;
        this.dataPayload = dataPayload;
        this.reason = reason;
        status = DRAFT;
        version++;
        events.add(MdmEvent.of("MasterDataChanged", recordNo, reason == null ? "" : reason));
    }

    /**
     * 执行命令 {@code submitReview}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void submitReview(String reason, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != DRAFT && status != REJECTED) {
            throw new IllegalStateException("master data record cannot submit review");
        }
        status = PENDING_REVIEW;
        this.reason = reason;
        version++;
        events.add(MdmEvent.of("MasterDataSubmitted", recordNo, reason == null ? "" : reason));
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param remark 业务处理参数或成员，类型为 {@code String}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void approve(String remark, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != PENDING_REVIEW) {
            throw new IllegalStateException("master data record is not pending review");
        }
        status = ENABLED;
        currentVersionNo++;
        reason = remark;
        version++;
        events.add(MdmEvent.of("MasterDataEnabled", recordNo, typeCode + "|" + dataCode + "|" + currentVersionNo));
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void reject(String reason, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != PENDING_REVIEW) {
            throw new IllegalStateException("master data record is not pending review");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("reject reason is required");
        }
        status = REJECTED;
        this.reason = reason;
        version++;
        events.add(MdmEvent.of("MasterDataRejected", recordNo, reason));
    }

    /**
     * 执行命令 {@code freeze}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void freeze(String reason, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != ENABLED) {
            throw new IllegalStateException("master data record is not enabled");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("freeze reason is required");
        }
        status = FROZEN;
        this.reason = reason;
        version++;
        events.add(MdmEvent.of("MasterDataFrozen", recordNo, reason));
    }

    /**
     * 将已冻结主数据恢复为启用状态。
     *
     * @param reason 恢复原因
     * @param expectedVersion 预期版本
     */
    public void enable(String reason, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != FROZEN) {
            throw new IllegalStateException("only frozen master data can be enabled");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("enable reason is required");
        }
        status = ENABLED;
        this.reason = reason;
        version++;
        events.add(MdmEvent.of("MasterDataEnabled", recordNo, reason));
    }

    /**
     * 执行命令 {@code disable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void disable(String reason, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != ENABLED && status != FROZEN) {
            throw new IllegalStateException("master data record cannot be disabled");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("disable reason is required");
        }
        status = DISABLED;
        this.reason = reason;
        version++;
        events.add(MdmEvent.of("MasterDataDisabled", recordNo, reason));
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<MdmEvent>}
     */
    public List<MdmEvent> pullEvents() {
        List<MdmEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    /**
     * 处理当前类型职责中的操作 {@code recordNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String recordNo() {
        return recordNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code typeCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String typeCode() {
        return typeCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code dataCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String dataCode() {
        return dataCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code dataName}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String dataName() {
        return dataName;
    }

    /**
     * 处理当前类型职责中的操作 {@code dataPayload}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String dataPayload() {
        return dataPayload;
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
     * 处理当前类型职责中的操作 {@code currentVersionNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int currentVersionNo() {
        return currentVersionNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code reason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String reason() {
        return reason;
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
     * 校验业务约束 {@code ensureVersion}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    private void ensureVersion(long expectedVersion) {
        if (version != expectedVersion) {
            throw new IllegalStateException("master data record version conflict");
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
}
