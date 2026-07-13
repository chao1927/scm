package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MasterDataRecordApplicationService;
import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MasterDataRecordControllerTest {
    @Test
    void delegatesRecordCommandsToApplicationService() {
        StubRecordService service = new StubRecordService();
        MasterDataRecordController controller = new MasterDataRecordController(service);
        MasterDataRecordApplicationService.CreateRecordCommand command =
                new MasterDataRecordApplicationService.CreateRecordCommand("SKU", "SKU-001", "测试商品", "{}", 1001L, "idem-1");

        MasterDataRecordMapper.RecordRow created = controller.create(command);

        assertThat(created.recordNo()).isEqualTo("MDR200001");
        assertThat(service.lastCreateCommand).isEqualTo(command);
    }

    @Test
    void mapsQueryAndVersionEndpoints() {
        StubRecordService service = new StubRecordService();
        MasterDataRecordController controller = new MasterDataRecordController(service);

        assertThat(controller.list("SKU", 3, 1, 20)).isEmpty();
        assertThat(controller.versions("MDR200001")).isEmpty();
        assertThat(service.lastQuery).isEqualTo(new MasterDataRecordApplicationService.Query("SKU", 3, 1, 20));
        assertThat(service.lastVersionRecordNo).isEqualTo("MDR200001");
    }

    static class StubRecordService extends MasterDataRecordApplicationService {
        MasterDataRecordApplicationService.CreateRecordCommand lastCreateCommand;
        MasterDataRecordApplicationService.Query lastQuery;
        String lastVersionRecordNo;

        StubRecordService() {
            super(null, null);
        }

        @Override
        public MasterDataRecordMapper.RecordRow create(MasterDataRecordApplicationService.CreateRecordCommand command) {
            lastCreateCommand = command;
            return new MasterDataRecordMapper.RecordRow(null, "MDR200001", "SKU", "SKU-001",
                    "测试商品", "{}", 1, 0, null, 1);
        }

        @Override
        public List<MasterDataRecordMapper.RecordRow> list(MasterDataRecordApplicationService.Query query) {
            lastQuery = query;
            return List.of();
        }

        @Override
        public List<MasterDataRecordMapper.VersionRow> listVersions(String recordNo) {
            lastVersionRecordNo = recordNo;
            return List.of();
        }
    }
}
