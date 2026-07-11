package com.chaobo.scm.supplier.domain.item;
import com.chaobo.scm.common.error.BusinessException;import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;import org.junit.jupiter.api.Test;import java.math.BigDecimal;import java.util.concurrent.atomic.AtomicLong;import static org.assertj.core.api.Assertions.*;
class SupplierItemAggregateTest{
 private final Ids ids=new Ids();private SupplyCondition condition(){return new SupplyCondition(new BigDecimal("10"),new BigDecimal("2"),7,"件",null,null);}
 @Test void shouldPauseAndResume(){var a=SupplierItemAggregate.enable(10,"SKU-1","SP-1",condition(),1,ids);a.pause("产能不足",1,ids);assertThat(a.status()).isEqualTo(SupplyStatus.PAUSED);a.resume(1,ids);assertThat(a.status()).isEqualTo(SupplyStatus.AVAILABLE);}
 @Test void shouldNotPauseTwice(){var a=SupplierItemAggregate.enable(10,"SKU-1","SP-1",condition(),1,ids);a.pause("产能不足",1,ids);assertThatThrownBy(()->a.pause("重复",1,ids)).isInstanceOf(BusinessException.class);}
 private static class Ids implements IdentifierGenerator{private final AtomicLong s=new AtomicLong(1);public long nextId(){return s.getAndIncrement();}public String nextBusinessNo(String p){return p+s.getAndIncrement();}}
}
