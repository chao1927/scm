package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmImportQualityApplicationService;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmImportQualityMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MdmImportQualityControllerTest {
    @Test
    void delegatesImportAndQualityCommands() {
        StubImportQualityService service = new StubImportQualityService();
        MdmImportQualityController controller = new MdmImportQualityController(service);
        MdmImportQualityApplicationService.CreateImportTaskCommand command =
                new MdmImportQualityApplicationService.CreateImportTaskCommand(
                        "SKU", "sku.csv", "oss://sku.csv", "hash-1", "CREATE", false, "REJECT", 1001L, "idem-1");

        MdmImportQualityMapper.ImportTaskRow task = controller.createImportTask(command);

        assertThat(task.importTaskNo()).isEqualTo("IMP500001");
        assertThat(service.lastCreateImportTaskCommand).isEqualTo(command);
        assertThat(controller.issues(null, null)).isEmpty();
    }

    static class StubImportQualityService extends MdmImportQualityApplicationService {
        MdmImportQualityApplicationService.CreateImportTaskCommand lastCreateImportTaskCommand;

        StubImportQualityService() {
            super(null, null);
        }

        @Override
        public MdmImportQualityMapper.ImportTaskRow createImportTask(MdmImportQualityApplicationService.CreateImportTaskCommand command) {
            lastCreateImportTaskCommand = command;
            return new MdmImportQualityMapper.ImportTaskRow(null, "IMP500001", "SKU", "sku.csv",
                    "oss://sku.csv", "hash-1", "CREATE", false, "REJECT", 1, 0, 0, 0, null, null, 1);
        }

        @Override
        public List<MdmImportQualityMapper.QualityIssueRow> listQualityIssues(String typeCode, Integer status) {
            return List.of();
        }
    }
}
