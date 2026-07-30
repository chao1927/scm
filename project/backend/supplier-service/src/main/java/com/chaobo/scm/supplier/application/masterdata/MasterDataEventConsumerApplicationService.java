package com.chaobo.scm.supplier.application.masterdata;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.application.item.SupplierItemMasterDataStatusService;
import com.chaobo.scm.supplier.application.profile.AdmissionRegistrationProjectionPort;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.util.Locale;
import java.util.Map;

/**
 * MasterDataEventConsumerApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class MasterDataEventConsumerApplicationService {

    /**
     * CONSUMER_NAME（类型：{@code String}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final String CONSUMER_NAME = "supplier-master-data-snapshot";

    /**
     * snapshots（类型：{@code MasterDataSnapshotPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataSnapshotPort snapshots;

    /**
     * consumeLog（类型：{@code MasterDataEventConsumeLogPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataEventConsumeLogPort consumeLog;

    /**
     * itemStatus（类型：{@code SupplierItemMasterDataStatusService}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private final SupplierItemMasterDataStatusService itemStatus;

    /**
     * json（类型：{@code ObjectMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ObjectMapper json;

    /**
     * admissions（类型：{@code AdmissionRegistrationProjectionPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AdmissionRegistrationProjectionPort admissions;

    /**
     * 创建 MasterDataEventConsumerApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param snapshots 业务处理参数或成员，类型为 {@code MasterDataSnapshotPort}
     * @param consumeLog 业务处理参数或成员，类型为 {@code MasterDataEventConsumeLogPort}
     * @param itemStatus 生命周期状态，类型为 {@code SupplierItemMasterDataStatusService}
     * @param json 业务处理参数或成员，类型为 {@code ObjectMapper}
     * @param admissions 业务处理参数或成员，类型为 {@code AdmissionRegistrationProjectionPort}
     */
    @Autowired
    public MasterDataEventConsumerApplicationService(MasterDataSnapshotPort snapshots, MasterDataEventConsumeLogPort consumeLog, SupplierItemMasterDataStatusService itemStatus, ObjectMapper json, AdmissionRegistrationProjectionPort admissions) {
        this.snapshots = snapshots;
        this.consumeLog = consumeLog;
        this.itemStatus = itemStatus;
        this.json = json;
        this.admissions = admissions;
    }

    /**
     * 创建 MasterDataEventConsumerApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param snapshots 业务处理参数或成员，类型为 {@code MasterDataSnapshotPort}
     * @param consumeLog 业务处理参数或成员，类型为 {@code MasterDataEventConsumeLogPort}
     * @param json 业务处理参数或成员，类型为 {@code ObjectMapper}
     */
    MasterDataEventConsumerApplicationService(MasterDataSnapshotPort snapshots, MasterDataEventConsumeLogPort consumeLog, ObjectMapper json) {
        this.snapshots = snapshots;
        this.consumeLog = consumeLog;
        this.itemStatus = null;
        this.json = json;
        this.admissions = null;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code MasterDataEvent}
     * @return 执行命令的结果，类型为 {@code MasterDataEventConsumeResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public MasterDataEventConsumeResult consume(MasterDataEvent event) {
        validateSource(event);
        var claim = consumeLog.claim(event.sourceSystem(), event.eventCode(), event.eventType(), CONSUMER_NAME, event.sourceSystem() + ":" + event.eventCode());
        if (claim == MasterDataEventConsumeLogPort.ClaimResult.ALREADY_SUCCEEDED) {
            return MasterDataEventConsumeResult.ignored("重复事件");
        }
        if (claim == MasterDataEventConsumeLogPort.ClaimResult.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "主数据事件正在消费");
        }
        consumeLog.savePayload(event.sourceSystem(), event.eventCode(), CONSUMER_NAME, json.writeValueAsString(event));
        try {
            boolean ignored = switch(event.eventType()) {
                case "SupplierEnabled", "SupplierFrozen", "SupplierDisabled" ->
                    refreshSupplier(event);
                case "SkuEnabled", "SkuChanged", "SkuDisabled" ->
                    refreshSku(event);
                default ->
                    true;
            };
            consumeLog.markSucceeded(event.sourceSystem(), event.eventCode(), CONSUMER_NAME, ignored);
            return ignored ? MasterDataEventConsumeResult.ignored("不属于供应商快照订阅范围或版本过期") : MasterDataEventConsumeResult.succeeded();
        } catch (RuntimeException exception) {
            throw exception;
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code refreshSupplier}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code MasterDataEvent}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private boolean refreshSupplier(MasterDataEvent event) {
        Map<String, Object> data = event.data();
        long supplierId = requiredLong(data, "supplierId", event.aggregateId());
        long version = sourceVersion(data, event.aggregateVersion());
        var current = snapshots.findSupplier(supplierId);
        if (current.isPresent() && current.get().sourceVersion() >= version) {
            return true;
        }
        int status = intValue(data, "lifecycleStatus", supplierStatus(event.eventType()));
        int risk = intValue(data, "riskLevel", current.map(MasterDataSnapshotPort.SupplierSnapshot::riskLevel).orElse(1));
        String code = requiredText(data, "supplierCode", current.map(MasterDataSnapshotPort.SupplierSnapshot::supplierCode).orElse(null));
        String name = requiredText(data, "supplierName", current.map(MasterDataSnapshotPort.SupplierSnapshot::supplierName).orElse(null));
        snapshots.saveSupplier(new MasterDataSnapshotPort.SupplierSnapshot(supplierId, code, name, status, risk, json.writeValueAsString(data), version));
        Long admissionId = longValue(data, "admissionId", null);
        boolean admissionAvailable = admissions != null && admissionId != null && admissionId > 0;
        boolean supplierEnabled = SUPPLIER_ENABLED.equals(event.eventType());
        if (admissionAvailable && supplierEnabled) {
            admissions.registered(admissionId, supplierId, code, event.eventCode(), version, event.occurredAt());
        }
        boolean itemStatusAvailable = itemStatus != null;
        boolean supplierUnavailable = SUPPLIER_FROZEN.equals(event.eventType()) || SUPPLIER_DISABLED.equals(event.eventType());
        if (itemStatusAvailable && supplierUnavailable) {
            itemStatus.pauseBySupplier(supplierId, "SupplierFrozen".equals(event.eventType()) ? "供应商已冻结" : "供应商已停用");
        }
        return false;
    }

    /**
     * 处理当前类型职责中的操作 {@code refreshSku}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code MasterDataEvent}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private boolean refreshSku(MasterDataEvent event) {
        Map<String, Object> data = event.data();
        String skuCode = requiredText(data, "skuCode", null);
        long version = sourceVersion(data, event.aggregateVersion());
        var current = snapshots.findSku(skuCode);
        if (current.isPresent() && current.get().sourceVersion() >= version) {
            return true;
        }
        int status = intValue(data, "skuStatus", skuStatus(event.eventType()));
        String name = requiredText(data, "skuName", current.map(MasterDataSnapshotPort.SkuSnapshot::skuName).orElse(null));
        String unit = text(data, "baseUnit", current.map(MasterDataSnapshotPort.SkuSnapshot::baseUnit).orElse(null));
        Long categoryId = longValue(data, "categoryId", current.map(MasterDataSnapshotPort.SkuSnapshot::categoryId).orElse(null));
        snapshots.saveSku(new MasterDataSnapshotPort.SkuSnapshot(skuCode, name, status, unit, categoryId, json.writeValueAsString(data), version));
        if (itemStatus != null && event.eventType().equals(SKU_DISABLED)) {
            itemStatus.pauseBySku(skuCode, "SKU已停用");
        }
        return false;
    }

    /**
     * 校验业务约束 {@code validateSource}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code MasterDataEvent}
     */
    private void validateSource(MasterDataEvent event) {
        String source = event.sourceSystem().toUpperCase(Locale.ROOT);
        if (!MDM.equals(source) && !MASTER_DATA.equals(source)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "只接受主数据系统事件");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code supplierStatus}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    private int supplierStatus(String eventType) {
        return switch(eventType) {
            case "SupplierEnabled" ->
                3;
            case "SupplierFrozen" ->
                4;
            default ->
                5;
        };
    }

    /**
     * 处理当前类型职责中的操作 {@code skuStatus}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    private int skuStatus(String eventType) {
        return "SkuDisabled".equals(eventType) ? 2 : 1;
    }

    /**
     * 处理当前类型职责中的操作 {@code sourceVersion}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param data 业务处理参数或成员，类型为 {@code Map<String,Object>}
     * @param fallback 业务处理参数或成员，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    private long sourceVersion(Map<String, Object> data, long fallback) {
        return requiredLong(data, "sourceVersion", fallback);
    }

    /**
     * 查询并返回 {@code requiredLong}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param data 业务处理参数或成员，类型为 {@code Map<String,Object>}
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param fallback 业务处理参数或成员，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    private long requiredLong(Map<String, Object> data, String key, long fallback) {
        Long value = longValue(data, key, fallback > 0 ? fallback : null);
        if (value == null || value <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, key + "不能为空");
        }
        return value.longValue();
    }

    /**
     * 处理当前类型职责中的操作 {@code longValue}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param data 业务处理参数或成员，类型为 {@code Map<String,Object>}
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param fallback 业务处理参数或成员，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    private Long longValue(Map<String, Object> data, String key, Long fallback) {
        Object value = data.get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, key + "必须是整数");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code intValue}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param data 业务处理参数或成员，类型为 {@code Map<String,Object>}
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param fallback 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    private int intValue(Map<String, Object> data, String key, int fallback) {
        Object value = data.get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, key + "必须是整数");
        }
    }

    /**
     * 查询并返回 {@code requiredText}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param data 业务处理参数或成员，类型为 {@code Map<String,Object>}
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param fallback 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code String}
     */
    private String requiredText(Map<String, Object> data, String key, String fallback) {
        String value = text(data, key, fallback);
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, key + "不能为空");
        }
        return value;
    }

    /**
     * 处理当前类型职责中的操作 {@code text}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param data 业务处理参数或成员，类型为 {@code Map<String,Object>}
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param fallback 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String text(Map<String, Object> data, String key, String fallback) {
        Object value = data.get(key);
        return value == null ? fallback : value.toString();
    }

    /**
     * 业务常量 {@code MASTER_DATA}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String MASTER_DATA = "MASTER_DATA";

    /**
     * 业务常量 {@code MDM}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String MDM = "MDM";

    /**
     * 业务常量 {@code SKU_DISABLED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String SKU_DISABLED = "SkuDisabled";

    /**
     * 业务常量 {@code SUPPLIER_DISABLED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String SUPPLIER_DISABLED = "SupplierDisabled";

    /**
     * 业务常量 {@code SUPPLIER_FROZEN}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String SUPPLIER_FROZEN = "SupplierFrozen";

    /**
     * 业务常量 {@code SUPPLIER_ENABLED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String SUPPLIER_ENABLED = "SupplierEnabled";
}
