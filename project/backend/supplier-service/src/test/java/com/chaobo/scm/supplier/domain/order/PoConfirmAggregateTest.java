package com.chaobo.scm.supplier.domain.order;
import com.chaobo.scm.common.error.BusinessException;import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;import org.junit.jupiter.api.Test;import java.math.BigDecimal;import java.time.*;import java.util.*;import java.util.concurrent.atomic.AtomicLong;import static org.assertj.core.api.Assertions.*;
class PoConfirmAggregateTest{
 private final Ids ids=new Ids();
 @Test void shouldConfirmAllLines(){var a=order();var decisions=a.lines().stream().map(v->new PoConfirmAggregate.LineDecision(v.lineId(),v.orderQty(),LocalDate.now().plusDays(3))).toList();a.confirm(decisions,"可按期供货",1,ids);assertThat(a.status()).isEqualTo(PoConfirmStatus.CONFIRMED);assertThat(a.lines()).allMatch(v->v.status()==2);}
 @Test void shouldRejectPartialConfirmation(){var a=order();var one=a.lines().get(0);assertThatThrownBy(()->a.confirm(List.of(new PoConfirmAggregate.LineDecision(one.lineId(),one.orderQty(),LocalDate.now().plusDays(3))),null,1,ids)).isInstanceOf(BusinessException.class);}
 @Test void shouldRejectOverConfirmation(){var a=order();var decisions=a.lines().stream().map(v->new PoConfirmAggregate.LineDecision(v.lineId(),v.orderQty().add(BigDecimal.ONE),LocalDate.now().plusDays(3))).toList();assertThatThrownBy(()->a.confirm(decisions,null,1,ids)).isInstanceOf(BusinessException.class);}
 private PoConfirmAggregate order(){return PoConfirmAggregate.receive(100,"PO-100",10,OffsetDateTime.now().plusDays(1),List.of(new PoConfirmAggregate.NewLine("SKU-1",new BigDecimal("10"),LocalDate.now().plusDays(3)),new PoConfirmAggregate.NewLine("SKU-2",new BigDecimal("20"),LocalDate.now().plusDays(5))),1,ids);}
 private static class Ids implements IdentifierGenerator{private final AtomicLong s=new AtomicLong(1);public long nextId(){return s.getAndIncrement();}public String nextBusinessNo(String p){return p+s.getAndIncrement();}}
}
