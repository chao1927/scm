package com.chaobo.scm.tms.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.tms.application.storage.TmsObjectStoragePort;
import com.chaobo.scm.tms.infrastructure.persistence.TrackingMapper;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TMS 附件下载权限测试。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class TmsAttachmentDownloadApplicationServiceTest {

    @Test
    void downloadsLabelOnlyAfterCarrierScopeIsAccepted() throws Exception {
        WaybillApplicationServiceTest.MemoryWaybillMapper waybills =
            new WaybillApplicationServiceTest.MemoryWaybillMapper();
        waybills.insertWaybill(waybill("SF"));
        waybills.insertLabel(new WaybillMapper.LabelRow(
            null, "LBL1", "WB1", "PKG1", "V1", "oss://labels/LBL1.pdf",
            1, 0, null, null, 1));
        TrackingReceiptApplicationServiceTest.MemoryTrackingMapper tracking =
            new TrackingReceiptApplicationServiceTest.MemoryTrackingMapper();
        RecordingStorage storage = new RecordingStorage();
        TmsAttachmentDownloadApplicationService service =
            new TmsAttachmentDownloadApplicationService(waybills, tracking, storage);

        TmsObjectStoragePort.StoredObject result = service.downloadLabel(
            "LBL1", access(Set.of("SF")));

        assertThat(result.inputStream().readAllBytes())
            .isEqualTo("file".getBytes(StandardCharsets.UTF_8));
        assertThat(storage.lastReference).isEqualTo("oss://labels/LBL1.pdf");
    }

    @Test
    void rejectsReceiptDownloadBeforeOpeningObjectWhenCarrierIsOutOfScope() {
        WaybillApplicationServiceTest.MemoryWaybillMapper waybills =
            new WaybillApplicationServiceTest.MemoryWaybillMapper();
        waybills.insertWaybill(waybill("SF"));
        TrackingReceiptApplicationServiceTest.MemoryTrackingMapper tracking =
            new TrackingReceiptApplicationServiceTest.MemoryTrackingMapper();
        tracking.insertReceipt(new TrackingMapper.ReceiptRow(
            null, "RCP1", "WB1", 1, "张三",
            LocalDateTime.parse("2026-07-30T10:00:00"), null,
            "oss://proof/RCP1.jpg"));
        RecordingStorage storage = new RecordingStorage();
        TmsAttachmentDownloadApplicationService service =
            new TmsAttachmentDownloadApplicationService(waybills, tracking, storage);

        assertThatThrownBy(() -> service.downloadReceipt("RCP1", access(Set.of("YTO"))))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("CARRIER/SF");
        assertThat(storage.lastReference).isNull();
    }

    private static WaybillMapper.WaybillRow waybill(String carrierCode) {
        return new WaybillMapper.WaybillRow(
            null, "WB1", "TASK1", carrierCode, "顺丰", "SF1",
            "EXPRESS", "ok", 1, null, null, 1);
    }

    private static ScmAccessContext access(Set<String> carriers) {
        return new ScmAccessContext(
            1001, "tester", "TMS", Set.of("tms:attachment:download"),
            Map.of("CARRIER", carriers));
    }

    private static final class RecordingStorage implements TmsObjectStoragePort {
        private String lastReference;

        @Override
        public StoredObject open(String objectReference) {
            lastReference = objectReference;
            byte[] content = "file".getBytes(StandardCharsets.UTF_8);
            return new StoredObject(new ByteArrayInputStream(content), content.length,
                "application/octet-stream", "file.bin");
        }
    }
}
