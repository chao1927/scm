package com.chaobo.scm.purchase.infrastructure.persistence;

import com.chaobo.scm.purchase.infrastructure.persistence.order.PurchaseOrderMapper;
import com.chaobo.scm.purchase.infrastructure.persistence.orderchange.PurchaseOrderChangeMapper;
import com.chaobo.scm.purchase.infrastructure.persistence.price.PurchasePriceMapper;
import java.nio.charset.StandardCharsets;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseCorePersistenceContractTest {

    @Test
    void priceOverlapQueryIncludesPriceTypeAndInclusiveDateRange() throws Exception {
        var method = PurchasePriceMapper.class.getMethod("findActiveOverlaps", long.class, String.class, long.class, int.class, String.class, java.time.LocalDate.class, java.time.LocalDate.class);
        var sql = String.join(" ", method.getAnnotation(Select.class).value()).replaceAll("\\s+", " ");

        assertThat(sql).contains("price_type = #{priceType}");
        assertThat(sql).contains("coalesce(effective_to, '9999-12-31') < #{effectiveFrom}");
    }

    @Test
    void purchaseOrderAndChangeUpdatesUseCompareAndSetVersion() throws Exception {
        var orderMethod = PurchaseOrderMapper.class.getMethod("updateHeader", long.class, java.math.BigDecimal.class, java.math.BigDecimal.class, java.math.BigDecimal.class, int.class, int.class, int.class, int.class, java.time.OffsetDateTime.class, String.class, long.class);
        var changeMethod = PurchaseOrderChangeMapper.class.getMethod("updateStatus", long.class, int.class, int.class, int.class, long.class);

        assertThat(String.join(" ", orderMethod.getAnnotation(Update.class).value())).contains("version = #{expectedVersion}");
        assertThat(String.join(" ", changeMethod.getAnnotation(Update.class).value())).contains("version = #{expectedVersion}");
    }

    @Test
    void completeSchemaPersistsBaseOrderVersion() throws Exception {
        try (var stream = getClass().getResourceAsStream("/db/schema.sql")) {
            assertThat(stream).isNotNull();
            var sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(sql).contains("add column base_order_version int null");
            assertThat(sql).contains("set change_record.base_order_version = purchase_order_record.version");
            assertThat(sql).contains("modify column base_order_version int not null");
        }
    }
}
