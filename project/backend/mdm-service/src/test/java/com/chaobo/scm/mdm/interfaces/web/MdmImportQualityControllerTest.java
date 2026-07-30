package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmImportQualityApplicationService;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmImportQualityMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * MdmImportQualityControllerTest。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class MdmImportQualityControllerTest {

    /**
     * 处理当前类型职责中的操作 {@code delegatesImportAndQualityCommands}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void delegatesImportAndQualityCommands() {
        StubImportQualityService service = new StubImportQualityService();
        MdmImportQualityController controller = new MdmImportQualityController(service);
        MdmImportQualityApplicationService.CreateImportTaskCommand command = new MdmImportQualityApplicationService.CreateImportTaskCommand("SKU", "sku.csv", "oss://sku.csv", "hash-1", "CREATE", false, "REJECT", 1001L, "idem-1");
        MdmImportQualityMapper.ImportTaskRow task = controller.createImportTask(command);
        assertThat(task.importTaskNo()).isEqualTo("IMP500001");
        assertThat(service.lastCreateImportTaskCommand).isEqualTo(command);
        assertThat(controller.issues(null, null)).isEmpty();
    }

    /**
     * StubImportQualityService。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class StubImportQualityService extends MdmImportQualityApplicationService {

        /**
         * lastCreateImportTaskCommand（类型：{@code MdmImportQualityApplicationService.CreateImportTaskCommand}）。
         *
         * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
         */
        MdmImportQualityApplicationService.CreateImportTaskCommand lastCreateImportTaskCommand;

        /**
         * 创建 StubImportQualityService。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         */
        StubImportQualityService() {
            super(null, null);
        }

        /**
         * 执行命令 {@code createImportTask}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param command 用例输入命令，类型为 {@code MdmImportQualityApplicationService.CreateImportTaskCommand}
         * @return 执行命令的结果，类型为 {@code MdmImportQualityMapper.ImportTaskRow}
         */
        @Override
        public MdmImportQualityMapper.ImportTaskRow createImportTask(MdmImportQualityApplicationService.CreateImportTaskCommand command) {
            lastCreateImportTaskCommand = command;
            return new MdmImportQualityMapper.ImportTaskRow(null, "IMP500001", "SKU", "sku.csv", "oss://sku.csv", "hash-1", "CREATE", false, "REJECT", 1, 0, 0, 0, null, null, 1);
        }

        /**
         * 查询并返回 {@code listQualityIssues}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param typeCode 可追踪业务编码，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code Integer}
         * @return 查询并返回的结果，类型为 {@code List<MdmImportQualityMapper.QualityIssueRow>}
         */
        @Override
        public List<MdmImportQualityMapper.QualityIssueRow> listQualityIssues(String typeCode, Integer status) {
            return List.of();
        }
    }
}
