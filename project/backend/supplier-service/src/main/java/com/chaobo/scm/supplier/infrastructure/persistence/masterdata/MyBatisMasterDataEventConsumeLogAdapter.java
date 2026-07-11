package com.chaobo.scm.supplier.infrastructure.persistence.masterdata;

import com.chaobo.scm.supplier.application.masterdata.MasterDataEventConsumeLogPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MyBatisMasterDataEventConsumeLogAdapter implements MasterDataEventConsumeLogPort {
    private final MasterDataEventConsumeLogMapper mapper;
    public MyBatisMasterDataEventConsumeLogAdapter(MasterDataEventConsumeLogMapper mapper) { this.mapper = mapper; }

    @Override public ClaimResult claim(String sourceSystem, String eventCode, String eventType,
                                       String consumerName, String idempotentKey) {
        if (mapper.insertProcessing(sourceSystem, eventCode, eventType, consumerName, idempotentKey) == 1) return ClaimResult.CLAIMED;
        var row = mapper.find(sourceSystem, eventCode, consumerName);
        if (row == null) return ClaimResult.IN_PROGRESS;
        if (row.status() == 2 || row.status() == 4) return ClaimResult.ALREADY_SUCCEEDED;
        if (row.status() == 3 && mapper.retryFailed(sourceSystem, eventCode, consumerName) == 1) return ClaimResult.CLAIMED;
        return ClaimResult.IN_PROGRESS;
    }
    @Override public void markSucceeded(String sourceSystem, String eventCode, String consumerName, boolean ignored) {
        mapper.markSucceeded(sourceSystem, eventCode, consumerName, ignored ? 4 : 2);
    }
    @Override @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String sourceSystem, String eventCode, String eventType,
                                        String consumerName, String idempotentKey, String reason) {
        String safeReason = reason == null ? "未知错误" : reason.substring(0, Math.min(reason.length(), 1000));
        mapper.recordFailure(sourceSystem, eventCode, eventType, consumerName, idempotentKey, safeReason);
    }
    @Override public void savePayload(String sourceSystem,String eventCode,String consumerName,String payloadJson){mapper.savePayload(sourceSystem,eventCode,consumerName,payloadJson);}
    @Override public java.util.Optional<ReplayEvent> findForReplay(long id){return java.util.Optional.ofNullable(mapper.findForReplay(id));}
    @Override public void markReplayRequested(long id,long operatorId,String reason){if(mapper.markReplayRequested(id,operatorId,reason)!=1)throw new com.chaobo.scm.common.error.BusinessException(com.chaobo.scm.common.error.ErrorCode.STATE_CONFLICT,"只有已保存载荷的失败事件可重放");}
}
