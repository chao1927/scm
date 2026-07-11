package com.chaobo.scm.supplier.application.masterdata;

public interface MasterDataEventConsumeLogPort {
    enum ClaimResult { CLAIMED, ALREADY_SUCCEEDED, IN_PROGRESS }
    ClaimResult claim(String sourceSystem, String eventCode, String eventType,
                      String consumerName, String idempotentKey);
    void markSucceeded(String sourceSystem, String eventCode, String consumerName, boolean ignored);
    void recordFailure(String sourceSystem, String eventCode, String eventType,
                       String consumerName, String idempotentKey, String reason);
    default void savePayload(String sourceSystem,String eventCode,String consumerName,String payloadJson){}
    default java.util.Optional<ReplayEvent> findForReplay(long consumeLogId){return java.util.Optional.empty();}
    default void markReplayRequested(long consumeLogId,long operatorId,String reason){}
    record ReplayEvent(long id,String sourceSystem,String eventCode,String eventType,String consumerName,String payloadJson,int status){}
}
