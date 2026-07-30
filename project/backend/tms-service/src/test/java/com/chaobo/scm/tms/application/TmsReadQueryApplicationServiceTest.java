package com.chaobo.scm.tms.application;

import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.tms.infrastructure.persistence.TmsReadQueryMapper;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TMS 标准页面查询数据范围测试。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class TmsReadQueryApplicationServiceTest {

    @Test
    void filtersAllStandardQueriesByCarrierScope() {
        TmsReadQueryApplicationService service =
            new TmsReadQueryApplicationService(new FixedQueryMapper());
        ScmAccessContext sfAccess = new ScmAccessContext(
            1001, "dispatcher", "TMS", Set.of("tms:query:read"),
            Map.of("CARRIER", Set.of("SF")));

        assertThat(service.labels(null, sfAccess))
            .extracting(TmsReadQueryMapper.LabelView::carrierCode)
            .containsExactly("SF");
        assertThat(service.tracks(null, sfAccess))
            .extracting(TmsReadQueryMapper.TrackView::carrierCode)
            .containsExactly("SF");
        assertThat(service.receipts(null, sfAccess))
            .extracting(TmsReadQueryMapper.ReceiptView::carrierCode)
            .containsExactly("SF");
        assertThat(service.carriers(sfAccess))
            .extracting(TmsReadQueryMapper.CarrierView::carrierCode)
            .containsExactly("SF");
        assertThat(service.operationLogs(sfAccess))
            .extracting(TmsReadQueryMapper.OperationLogView::carrierCode)
            .containsExactly("SF");
    }

    private static final class FixedQueryMapper implements TmsReadQueryMapper {

        @Override
        public List<LabelView> listLabels(String waybillNo) {
            return List.of(label("SF"), label("YTO"));
        }

        @Override
        public List<TrackView> listTracks(String waybillNo) {
            return List.of(track("SF"), track("YTO"));
        }

        @Override
        public List<ReceiptView> listReceipts(String waybillNo) {
            return List.of(receipt("SF"), receipt("YTO"));
        }

        @Override
        public List<CarrierView> listCarriers() {
            return List.of(carrier("SF"), carrier("YTO"));
        }

        @Override
        public List<OperationLogView> listOperationLogs() {
            return List.of(log("SF"), log("YTO"));
        }

        private LabelView label(String carrier) {
            return new LabelView("LBL-" + carrier, "WB-" + carrier, "PKG1", "V1",
                "oss://labels/test.pdf", 1, 0, null, carrier, carrier,
                LocalDateTime.parse("2026-07-30T10:00:00"));
        }

        private TrackView track(String carrier) {
            return new TrackView("TRK-" + carrier, "WB-" + carrier, "IN_TRANSIT",
                "运输中", "上海", LocalDateTime.parse("2026-07-30T10:00:00"),
                "CARRIER", carrier, carrier);
        }

        private ReceiptView receipt(String carrier) {
            return new ReceiptView("RCP-" + carrier, "WB-" + carrier, 1, "张三",
                LocalDateTime.parse("2026-07-30T10:00:00"), null,
                "oss://proof/test.jpg", carrier, carrier);
        }

        private CarrierView carrier(String carrier) {
            return new CarrierView(carrier, carrier, 1, 1,
                LocalDateTime.parse("2026-07-30T10:00:00"));
        }

        private OperationLogView log(String carrier) {
            return new OperationLogView("CALLBACK_RECEIVED", "WB-" + carrier,
                1001L, "idem", LocalDateTime.parse("2026-07-30T10:00:00"), carrier);
        }
    }
}
