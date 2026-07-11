package com.chaobo.scm.supplier.application.masterdata;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 失败记录必须与快照更新事务隔离，否则原事务回滚会同时抹掉失败状态。 */
@Service
public class MasterDataEventFailureApplicationService {
    private final MasterDataEventConsumeLogPort consumeLog;
    public MasterDataEventFailureApplicationService(MasterDataEventConsumeLogPort consumeLog) { this.consumeLog = consumeLog; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(MasterDataEvent event, String reason) {
        String value = reason == null ? "未知错误" : reason.substring(0, Math.min(reason.length(), 1000));
        consumeLog.recordFailure(event.sourceSystem(), event.eventCode(), event.eventType(),
                MasterDataEventConsumerApplicationService.CONSUMER_NAME,
                event.sourceSystem() + ":" + event.eventCode(), value);
    }
}
