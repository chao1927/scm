package com.chaobo.scm.mdm.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * MasterDataVersionAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class MasterDataVersionAggregate {

    /**
     * versionNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String versionNo;

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
     * versionNumber（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private final int versionNumber;

    /**
     * snapshotPayload（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String snapshotPayload;

    /**
     * changeSummary（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String changeSummary;

    /**
     * events（类型：{@code List<MdmEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<MdmEvent> events = new ArrayList<>();

    /**
     * 创建 MasterDataVersionAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param versionNo 可追踪业务编码，类型为 {@code String}
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param dataCode 可追踪业务编码，类型为 {@code String}
     * @param versionNumber 乐观锁或契约版本，类型为 {@code int}
     * @param snapshotPayload 业务处理参数或成员，类型为 {@code String}
     * @param changeSummary 业务处理参数或成员，类型为 {@code String}
     */
    private MasterDataVersionAggregate(String versionNo, String recordNo, String typeCode, String dataCode, int versionNumber, String snapshotPayload, String changeSummary) {
        if (blank(versionNo) || blank(recordNo) || blank(typeCode) || blank(dataCode) || blank(snapshotPayload)) {
            throw new IllegalArgumentException("master data version references and snapshot are required");
        }
        if (versionNumber <= 0) {
            throw new IllegalArgumentException("version number must be positive");
        }
        this.versionNo = versionNo;
        this.recordNo = recordNo;
        this.typeCode = typeCode;
        this.dataCode = dataCode;
        this.versionNumber = versionNumber;
        this.snapshotPayload = snapshotPayload;
        this.changeSummary = changeSummary;
    }

    /**
     * 处理当前类型职责中的操作 {@code generate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param versionNo 可追踪业务编码，类型为 {@code String}
     * @param record 业务处理参数或成员，类型为 {@code MasterDataRecordAggregate}
     * @param changeSummary 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MasterDataVersionAggregate}
     */
    public static MasterDataVersionAggregate generate(String versionNo, MasterDataRecordAggregate record, String changeSummary) {
        MasterDataVersionAggregate aggregate = new MasterDataVersionAggregate(versionNo, record.recordNo(), record.typeCode(), record.dataCode(), record.currentVersionNo(), record.dataPayload(), changeSummary);
        aggregate.events.add(MdmEvent.of("MasterDataVersionGenerated", versionNo, record.typeCode() + "|" + record.dataCode() + "|" + record.currentVersionNo()));
        return aggregate;
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
     * 处理当前类型职责中的操作 {@code versionNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String versionNo() {
        return versionNo;
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
     * 处理当前类型职责中的操作 {@code versionNumber}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int versionNumber() {
        return versionNumber;
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshotPayload}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String snapshotPayload() {
        return snapshotPayload;
    }

    /**
     * 处理当前类型职责中的操作 {@code changeSummary}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String changeSummary() {
        return changeSummary;
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
