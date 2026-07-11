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

@Service
public class MasterDataEventConsumerApplicationService {
    public static final String CONSUMER_NAME = "supplier-master-data-snapshot";
    private final MasterDataSnapshotPort snapshots;
    private final MasterDataEventConsumeLogPort consumeLog;
    private final SupplierItemMasterDataStatusService itemStatus;
    private final ObjectMapper json;
    private final AdmissionRegistrationProjectionPort admissions;

    @Autowired public MasterDataEventConsumerApplicationService(MasterDataSnapshotPort snapshots,
                                                     MasterDataEventConsumeLogPort consumeLog, SupplierItemMasterDataStatusService itemStatus,
                                                     ObjectMapper json, AdmissionRegistrationProjectionPort admissions) {
        this.snapshots = snapshots;
        this.consumeLog = consumeLog;
        this.itemStatus = itemStatus;
        this.json = json;
        this.admissions = admissions;
    }
    MasterDataEventConsumerApplicationService(MasterDataSnapshotPort snapshots, MasterDataEventConsumeLogPort consumeLog, ObjectMapper json) { this.snapshots=snapshots;this.consumeLog=consumeLog;this.itemStatus=null;this.json=json;this.admissions=null; }

    @Transactional
    public MasterDataEventConsumeResult consume(MasterDataEvent event) {
        validateSource(event);
        var claim = consumeLog.claim(event.sourceSystem(), event.eventCode(), event.eventType(), CONSUMER_NAME,
                event.sourceSystem() + ":" + event.eventCode());
        if (claim == MasterDataEventConsumeLogPort.ClaimResult.ALREADY_SUCCEEDED) {
            return MasterDataEventConsumeResult.ignored("重复事件");
        }
        if (claim == MasterDataEventConsumeLogPort.ClaimResult.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "主数据事件正在消费");
        }
        consumeLog.savePayload(event.sourceSystem(),event.eventCode(),CONSUMER_NAME,json.writeValueAsString(event));
        try {
            boolean ignored = switch (event.eventType()) {
                case "SupplierEnabled", "SupplierFrozen", "SupplierDisabled" -> refreshSupplier(event);
                case "SkuEnabled", "SkuChanged", "SkuDisabled" -> refreshSku(event);
                default -> true;
            };
            consumeLog.markSucceeded(event.sourceSystem(), event.eventCode(), CONSUMER_NAME, ignored);
            return ignored ? MasterDataEventConsumeResult.ignored("不属于供应商快照订阅范围或版本过期")
                    : MasterDataEventConsumeResult.succeeded();
        } catch (RuntimeException exception) {
            throw exception;
        }
    }

    private boolean refreshSupplier(MasterDataEvent event) {
        Map<String, Object> data = event.data();
        long supplierId = requiredLong(data, "supplierId", event.aggregateId());
        long version = sourceVersion(data, event.aggregateVersion());
        var current = snapshots.findSupplier(supplierId);
        if (current.isPresent() && current.get().sourceVersion() >= version) return true;
        int status = intValue(data, "lifecycleStatus", supplierStatus(event.eventType()));
        int risk = intValue(data, "riskLevel", current.map(MasterDataSnapshotPort.SupplierSnapshot::riskLevel).orElse(1));
        String code = requiredText(data, "supplierCode", current.map(MasterDataSnapshotPort.SupplierSnapshot::supplierCode).orElse(null));
        String name = requiredText(data, "supplierName", current.map(MasterDataSnapshotPort.SupplierSnapshot::supplierName).orElse(null));
        snapshots.saveSupplier(new MasterDataSnapshotPort.SupplierSnapshot(supplierId, code, name, status, risk,
                json.writeValueAsString(data), version));
        Long admissionId=longValue(data,"admissionId",null);
        if(admissions!=null&&admissionId!=null&&admissionId>0&&event.eventType().equals("SupplierEnabled")) admissions.registered(admissionId,supplierId,code,event.eventCode(),version,event.occurredAt());
        if (itemStatus != null && (event.eventType().equals("SupplierFrozen") || event.eventType().equals("SupplierDisabled"))) itemStatus.pauseBySupplier(supplierId,event.eventType().equals("SupplierFrozen")?"供应商已冻结":"供应商已停用");
        return false;
    }

    private boolean refreshSku(MasterDataEvent event) {
        Map<String, Object> data = event.data();
        String skuCode = requiredText(data, "skuCode", null);
        long version = sourceVersion(data, event.aggregateVersion());
        var current = snapshots.findSku(skuCode);
        if (current.isPresent() && current.get().sourceVersion() >= version) return true;
        int status = intValue(data, "skuStatus", skuStatus(event.eventType()));
        String name = requiredText(data, "skuName", current.map(MasterDataSnapshotPort.SkuSnapshot::skuName).orElse(null));
        String unit = text(data, "baseUnit", current.map(MasterDataSnapshotPort.SkuSnapshot::baseUnit).orElse(null));
        Long categoryId = longValue(data, "categoryId", current.map(MasterDataSnapshotPort.SkuSnapshot::categoryId).orElse(null));
        snapshots.saveSku(new MasterDataSnapshotPort.SkuSnapshot(skuCode, name, status, unit, categoryId,
                json.writeValueAsString(data), version));
        if (itemStatus != null && event.eventType().equals("SkuDisabled")) itemStatus.pauseBySku(skuCode,"SKU已停用");
        return false;
    }

    private void validateSource(MasterDataEvent event) {
        String source = event.sourceSystem().toUpperCase(Locale.ROOT);
        if (!source.equals("MDM") && !source.equals("MASTER_DATA")) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "只接受主数据系统事件");
        }
    }
    private int supplierStatus(String eventType) { return switch (eventType) { case "SupplierEnabled" -> 3; case "SupplierFrozen" -> 4; default -> 5; }; }
    private int skuStatus(String eventType) { return eventType.equals("SkuDisabled") ? 2 : 1; }
    private long sourceVersion(Map<String, Object> data, long fallback) { return requiredLong(data, "sourceVersion", fallback); }
    private long requiredLong(Map<String, Object> data, String key, long fallback) { Long value = longValue(data, key, fallback > 0 ? fallback : null); if (value == null || value <= 0) throw new BusinessException(ErrorCode.VALIDATION_FAILED, key + "不能为空"); return value; }
    private Long longValue(Map<String, Object> data, String key, Long fallback) { Object value = data.get(key); if (value == null) return fallback; if (value instanceof Number n) return n.longValue(); try { return Long.valueOf(value.toString()); } catch (NumberFormatException e) { throw new BusinessException(ErrorCode.VALIDATION_FAILED, key + "必须是整数"); } }
    private int intValue(Map<String, Object> data, String key, int fallback) { Object value = data.get(key); if (value == null) return fallback; if (value instanceof Number n) return n.intValue(); try { return Integer.parseInt(value.toString()); } catch (NumberFormatException e) { throw new BusinessException(ErrorCode.VALIDATION_FAILED, key + "必须是整数"); } }
    private String requiredText(Map<String, Object> data, String key, String fallback) { String value = text(data, key, fallback); if (value == null || value.isBlank()) throw new BusinessException(ErrorCode.VALIDATION_FAILED, key + "不能为空"); return value; }
    private String text(Map<String, Object> data, String key, String fallback) { Object value = data.get(key); return value == null ? fallback : value.toString(); }
}
