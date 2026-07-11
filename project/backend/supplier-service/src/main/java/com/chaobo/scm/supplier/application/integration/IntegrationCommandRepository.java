package com.chaobo.scm.supplier.application.integration;
import java.time.OffsetDateTime;import java.util.List;
public interface IntegrationCommandRepository{void save(IntegrationCommand command);List<IntegrationCommand> lockDispatchable(int size);boolean markExecuting(long id);void markSucceeded(long id,String reference);void markRetry(long id,int expectedRetry,OffsetDateTime nextRetry,String reason,int maxRetries);void retryManually(long id,String reason);}
