package com.chaobo.scm.mdm.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.integration.MasterDataCollaborationApi;
import com.chaobo.scm.mdm.domain.MasterDataRecordAggregate;
import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmCollaborationMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;

/** 将供应商准入和生命周期命令映射到 MDM 正式主数据记录。 */
@Service
public class MdmCollaborationApplicationService {

    private static final String CREATE = "CREATE_SUPPLIER";
    private static final String CHANGE_STATUS = "CHANGE_SUPPLIER_STATUS";
    private final MasterDataRecordApplicationService records;
    private final MasterDataRecordMapper recordMapper;
    private final MdmCollaborationMapper collaboration;
    private final ObjectMapper json;

    public MdmCollaborationApplicationService(MasterDataRecordApplicationService records,
                                             MasterDataRecordMapper recordMapper,
                                             MdmCollaborationMapper collaboration,
                                             ObjectMapper json) {
        this.records = records;
        this.recordMapper = recordMapper;
        this.collaboration = collaboration;
        this.json = json;
    }

    @Transactional(rollbackFor = Exception.class)
    public MasterDataCollaborationApi.SupplierResult create(
            MasterDataCollaborationApi.CreateSupplierCommand command) {
        requireKey(command.idempotencyKey());
        if (command.admissionId() <= 0 || blank(command.supplierCode()) || blank(command.supplierName())
                || blank(command.taxNo()) || blank(command.supplierType())) {
            throw invalid("供应商主数据创建命令不完整");
        }
        String fingerprint = command.toString();
        MdmCollaborationMapper.Receipt duplicate = receipt(command.idempotencyKey(), CREATE,
                fingerprint);
        if (duplicate != null) {
            MdmCollaborationMapper.SupplierMapping mapping = collaboration.findSupplier(
                    command.admissionId());
            return accepted(mapping == null ? command.admissionId() : mapping.supplierId(),
                    command.supplierCode());
        }
        MdmCollaborationMapper.SupplierMapping existed = collaboration.findSupplier(
                command.admissionId());
        if (existed != null) {
            requireFingerprint(existed.requestFingerprint(), fingerprint);
            collaboration.insertReceipt(new MdmCollaborationMapper.Receipt(command.idempotencyKey(),
                    CREATE, fingerprint, existed.recordNo()));
            return accepted(existed.supplierId(), existed.supplierCode());
        }
        String payload = payload(command);
        MasterDataRecordMapper.RecordRow row = recordMapper.findRecordByCode("SUPPLIER",
                command.supplierCode());
        if (row == null) {
            row = records.create(new MasterDataRecordApplicationService.CreateRecordCommand(
                    "SUPPLIER", command.supplierCode(), command.supplierName(), payload, 0L,
                    command.idempotencyKey() + "-CREATE"));
            row = records.submitReview(row.recordNo(), new MasterDataRecordApplicationService.StateCommand(
                    "供应商准入已通过", row.version(), 0L,
                    command.idempotencyKey() + "-SUBMIT"));
            row = records.approve(row.recordNo(), new MasterDataRecordApplicationService.StateCommand(
                    "准入同步自动建档", row.version(), 0L,
                    command.idempotencyKey() + "-APPROVE"));
        } else if (!row.dataName().equals(command.supplierName())
                || !row.dataPayload().equals(payload)) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT,
                    "供应商编码已对应不同的 MDM 快照");
        }
        long supplierId = command.admissionId();
        collaboration.insertSupplier(new MdmCollaborationMapper.SupplierMapping(supplierId,
                command.admissionId(), command.supplierCode(), row.recordNo(), fingerprint));
        collaboration.insertReceipt(new MdmCollaborationMapper.Receipt(command.idempotencyKey(), CREATE,
                fingerprint, row.recordNo()));
        return accepted(supplierId, command.supplierCode());
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(MasterDataCollaborationApi.ChangeSupplierStatusCommand command) {
        requireKey(command.idempotencyKey());
        if (command.supplierId() <= 0 || command.targetStatus() < 3 || command.targetStatus() > 5
                || blank(command.reason())) {
            throw invalid("供应商状态变更命令不完整");
        }
        String fingerprint = command.toString();
        if (receipt(command.idempotencyKey(), CHANGE_STATUS, fingerprint) != null) {
            return;
        }
        MdmCollaborationMapper.SupplierMapping mapping = collaboration.findSupplier(command.supplierId());
        if (mapping == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "MDM 供应商映射不存在");
        }
        MasterDataRecordMapper.RecordRow row = records.get(mapping.recordNo());
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "MDM 供应商记录不存在");
        }
        if (command.targetStatus() == 3 && row.status() == MasterDataRecordAggregate.FROZEN) {
            records.enable(row.recordNo(), state(command, row.version()));
        } else if (command.targetStatus() == 4 && row.status() == MasterDataRecordAggregate.ENABLED) {
            records.freeze(row.recordNo(), state(command, row.version()));
        } else if (command.targetStatus() == 5
                && row.status() != MasterDataRecordAggregate.DISABLED) {
            records.disable(row.recordNo(), state(command, row.version()));
        } else if (!matches(command.targetStatus(), row.status())) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "MDM 供应商状态不允许目标变更");
        }
        collaboration.insertReceipt(new MdmCollaborationMapper.Receipt(command.idempotencyKey(),
                CHANGE_STATUS, fingerprint, row.recordNo()));
    }

    private MasterDataRecordApplicationService.StateCommand state(
            MasterDataCollaborationApi.ChangeSupplierStatusCommand command, long version) {
        return new MasterDataRecordApplicationService.StateCommand(command.reason(), version, 0L,
                command.idempotencyKey());
    }

    private String payload(MasterDataCollaborationApi.CreateSupplierCommand command) {
        var value = new LinkedHashMap<String, Object>();
        value.put("taxNo", command.taxNo());
        value.put("supplierType", command.supplierType());
        value.put("contactName", command.contactName());
        value.put("contactMobile", command.contactMobile());
        value.put("settlement", command.settlementJson());
        try {
            return json.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("供应商主数据序列化失败", exception);
        }
    }

    private MdmCollaborationMapper.Receipt receipt(String key, String type, String fingerprint) {
        MdmCollaborationMapper.Receipt receipt = collaboration.findReceipt(key);
        if (receipt != null && (!receipt.commandType().equals(type)
                || !receipt.requestFingerprint().equals(fingerprint))) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT,
                    "Dubbo 幂等键已用于不同 MDM 命令");
        }
        return receipt;
    }

    private static boolean matches(int targetStatus, int recordStatus) {
        return targetStatus == 3 && recordStatus == MasterDataRecordAggregate.ENABLED
                || targetStatus == 4 && recordStatus == MasterDataRecordAggregate.FROZEN
                || targetStatus == 5 && recordStatus == MasterDataRecordAggregate.DISABLED;
    }

    private static void requireFingerprint(String actual, String expected) {
        if (!actual.equals(expected)) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT,
                    "同一供应商准入已存在不同请求快照");
        }
    }

    private static MasterDataCollaborationApi.SupplierResult accepted(long supplierId, String code) {
        return new MasterDataCollaborationApi.SupplierResult(true, supplierId, code, null);
    }

    private static void requireKey(String key) {
        if (blank(key)) {
            throw invalid("Dubbo 命令必须提供幂等键");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static BusinessException invalid(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }
}
