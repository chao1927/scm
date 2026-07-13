package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.domain.MasterDataRecordAggregate;
import com.chaobo.scm.mdm.domain.MasterDataVersionAggregate;
import com.chaobo.scm.mdm.domain.MdmEvent;
import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MasterDataRecordApplicationService {
    private final MasterDataRecordMapper mapper;
    private final MdmMapper mdmMapper;
    private final AtomicLong recordSequence = new AtomicLong(200000);

    public MasterDataRecordApplicationService(MasterDataRecordMapper mapper, MdmMapper mdmMapper) {
        this.mapper = mapper;
        this.mdmMapper = mdmMapper;
    }

    @Transactional
    public MasterDataRecordMapper.RecordRow create(CreateRecordCommand command) {
        if (mdmMapper.findType(command.typeCode()) == null) {
            throw new IllegalStateException("type does not exist");
        }
        if (mapper.findRecordByCode(command.typeCode(), command.dataCode()) != null) {
            throw new IllegalStateException("master data code already exists");
        }
        MasterDataRecordAggregate aggregate = MasterDataRecordAggregate.create("MDR" + recordSequence.incrementAndGet(),
                command.typeCode(), command.dataCode(), command.dataName(), command.dataPayload());
        MasterDataRecordMapper.RecordRow row = toRow(aggregate);
        mapper.insertRecord(row);
        saveEvents(aggregate.pullEvents());
        log("CREATE_MASTER_DATA_RECORD", row.recordNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public MasterDataRecordMapper.RecordRow change(String recordNo, ChangeRecordCommand command) {
        MasterDataRecordAggregate aggregate = load(recordNo);
        aggregate.change(command.dataName(), command.dataPayload(), command.reason(), command.expectedVersion());
        mapper.updateRecord(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("CHANGE_MASTER_DATA_RECORD", recordNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRecord(recordNo);
    }

    @Transactional
    public MasterDataRecordMapper.RecordRow submitReview(String recordNo, StateCommand command) {
        MasterDataRecordAggregate aggregate = load(recordNo);
        aggregate.submitReview(command.reason(), command.expectedVersion());
        mapper.updateRecord(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("SUBMIT_MASTER_DATA_RECORD", recordNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRecord(recordNo);
    }

    @Transactional
    public MasterDataRecordMapper.RecordRow approve(String recordNo, StateCommand command) {
        MasterDataRecordAggregate aggregate = load(recordNo);
        aggregate.approve(command.reason(), command.expectedVersion());
        mapper.updateRecord(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        MasterDataVersionAggregate version = MasterDataVersionAggregate.generate(
                "MDV" + recordNo.substring(Math.max(0, recordNo.length() - 6)) + "V" + aggregate.currentVersionNo(),
                aggregate, command.reason());
        mapper.insertVersion(toRow(version));
        saveEvents(version.pullEvents());
        log("APPROVE_MASTER_DATA_RECORD", recordNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRecord(recordNo);
    }

    @Transactional
    public MasterDataRecordMapper.RecordRow reject(String recordNo, StateCommand command) {
        MasterDataRecordAggregate aggregate = load(recordNo);
        aggregate.reject(command.reason(), command.expectedVersion());
        mapper.updateRecord(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("REJECT_MASTER_DATA_RECORD", recordNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRecord(recordNo);
    }

    @Transactional
    public MasterDataRecordMapper.RecordRow freeze(String recordNo, StateCommand command) {
        MasterDataRecordAggregate aggregate = load(recordNo);
        aggregate.freeze(command.reason(), command.expectedVersion());
        mapper.updateRecord(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("FREEZE_MASTER_DATA_RECORD", recordNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRecord(recordNo);
    }

    @Transactional
    public MasterDataRecordMapper.RecordRow disable(String recordNo, StateCommand command) {
        MasterDataRecordAggregate aggregate = load(recordNo);
        aggregate.disable(command.reason(), command.expectedVersion());
        mapper.updateRecord(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("DISABLE_MASTER_DATA_RECORD", recordNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRecord(recordNo);
    }

    public MasterDataRecordMapper.RecordRow get(String recordNo) {
        return mapper.findRecord(recordNo);
    }

    public List<MasterDataRecordMapper.RecordRow> list(Query query) {
        int pageNo = query.pageNo() == null || query.pageNo() < 1 ? 1 : query.pageNo();
        int pageSize = query.pageSize() == null ? 20 : query.pageSize();
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("page size must be between 1 and 100");
        }
        return mapper.listRecords(emptyToNull(query.typeCode()), query.status(), pageSize, (pageNo - 1) * pageSize);
    }

    public List<MasterDataRecordMapper.VersionRow> listVersions(String recordNo) {
        return mapper.listVersions(recordNo);
    }

    public MasterDataRecordMapper.VersionRow getVersion(String versionNo) {
        return mapper.findVersion(versionNo);
    }

    private MasterDataRecordAggregate load(String recordNo) {
        MasterDataRecordMapper.RecordRow row = mapper.findRecord(recordNo);
        if (row == null) {
            throw new IllegalArgumentException("master data record not found");
        }
        return MasterDataRecordAggregate.restore(row.recordNo(), row.typeCode(), row.dataCode(), row.dataName(),
                row.dataPayload(), row.status(), row.currentVersionNo(), row.reason(), row.version());
    }

    private MasterDataRecordMapper.RecordRow toRow(MasterDataRecordAggregate aggregate) {
        return new MasterDataRecordMapper.RecordRow(null, aggregate.recordNo(), aggregate.typeCode(),
                aggregate.dataCode(), aggregate.dataName(), aggregate.dataPayload(), aggregate.status(),
                aggregate.currentVersionNo(), aggregate.reason(), aggregate.version());
    }

    private MasterDataRecordMapper.VersionRow toRow(MasterDataVersionAggregate aggregate) {
        return new MasterDataRecordMapper.VersionRow(null, aggregate.versionNo(), aggregate.recordNo(),
                aggregate.typeCode(), aggregate.dataCode(), aggregate.versionNumber(), aggregate.snapshotPayload(),
                aggregate.changeSummary(), LocalDateTime.now());
    }

    private void saveEvents(List<MdmEvent> events) {
        for (MdmEvent event : events) {
            mapper.insertOutbox(new MdmMapper.OutboxRow(event.eventType(), event.businessNo(), event.payload(), 1,
                    event.occurredAt()));
        }
    }

    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new MdmMapper.OperationLogRow(operationType, businessNo, operatorId,
                idempotencyKey, LocalDateTime.now()));
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    public record CreateRecordCommand(String typeCode, String dataCode, String dataName, String dataPayload,
                                      Long operatorId, String idempotencyKey) {}

    public record ChangeRecordCommand(String dataName, String dataPayload, String reason, long expectedVersion,
                                      Long operatorId, String idempotencyKey) {}

    public record StateCommand(String reason, long expectedVersion, Long operatorId, String idempotencyKey) {}
    public record Query(String typeCode, Integer status, Integer pageNo, Integer pageSize) {}
}
