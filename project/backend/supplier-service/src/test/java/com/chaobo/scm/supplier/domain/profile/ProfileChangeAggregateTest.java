package com.chaobo.scm.supplier.domain.profile;
import com.chaobo.scm.common.error.BusinessException;import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;import org.junit.jupiter.api.Test;import java.util.List;import java.util.concurrent.atomic.AtomicLong;import static org.assertj.core.api.Assertions.*;
class ProfileChangeAggregateTest{
 private final Ids ids=new Ids();
 @Test void shouldSubmitAndWithdraw(){var a=ProfileChangeAggregate.submit(10,2,"更新联系人",List.of(new ProfileFieldChange("contactName","张三","李四")),1,ids);assertThat(a.status()).isEqualTo(ProfileChangeStatus.PENDING);a.withdraw("资料需调整",1,ids);assertThat(a.status()).isEqualTo(ProfileChangeStatus.WITHDRAWN);assertThat(a.pullEvents()).extracting(e->e.eventType()).containsExactly("SupplierProfileChangeSubmitted","SupplierProfileChangeWithdrawn");}
 @Test void shouldRejectImmutableField(){assertThatThrownBy(()->new ProfileFieldChange("supplierCode","S1","S2")).isInstanceOf(BusinessException.class);}
 private static class Ids implements IdentifierGenerator{private final AtomicLong s=new AtomicLong(1);public long nextId(){return s.getAndIncrement();}public String nextBusinessNo(String p){return p+s.getAndIncrement();}}
}
