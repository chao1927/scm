package com.chaobo.scm.supplier.application.outbox;
import org.junit.jupiter.api.Test;import java.util.*;import static org.assertj.core.api.Assertions.assertThat;
class OutboxDispatchApplicationServiceTest{
 @Test void shouldMarkPublishedAfterBrokerSuccess(){var store=new Store();var service=new OutboxDispatchApplicationService(store,message->{});service.dispatch(message());assertThat(store.published).isEqualTo(1);assertThat(store.failed).isZero();}
 @Test void shouldMarkFailedAfterBrokerFailure(){var store=new Store();var service=new OutboxDispatchApplicationService(store,message->{throw new IllegalStateException("MQ不可用");});service.dispatch(message());assertThat(store.failed).isEqualTo(1);assertThat(store.reason).isEqualTo("MQ不可用");}
 private OutboxMessage message(){return new OutboxMessage(1,"SUP-1","SupplierItemEnabled","SUPPLIER_ITEM",10,"{}",0);}
 private static class Store implements OutboxDispatchPort{int published,failed;String reason;public List<OutboxMessage>claim(int b,int r){return List.of();}public void markPublished(long id){published++;}public void markFailed(long id,String reason){failed++;this.reason=reason;}}
}
