package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MasterDataRecordApplicationService;
import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * MasterDataRecordControllerTest。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class MasterDataRecordControllerTest {

    /**
     * 处理当前类型职责中的操作 {@code delegatesRecordCommandsToApplicationService}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void delegatesRecordCommandsToApplicationService() {
        StubRecordService service = new StubRecordService();
        MasterDataRecordController controller = new MasterDataRecordController(service);
        MasterDataRecordApplicationService.CreateRecordCommand command = new MasterDataRecordApplicationService.CreateRecordCommand("SKU", "SKU-001", "测试商品", "{}", 1001L, "idem-1");
        MasterDataRecordMapper.RecordRow created = controller.create(command);
        assertThat(created.recordNo()).isEqualTo("MDR200001");
        assertThat(service.lastCreateCommand).isEqualTo(command);
    }

    /**
     * 转换数据模型 {@code mapsQueryAndVersionEndpoints}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void mapsQueryAndVersionEndpoints() {
        StubRecordService service = new StubRecordService();
        MasterDataRecordController controller = new MasterDataRecordController(service);
        assertThat(controller.list("SKU", 3, 1, 20)).isEmpty();
        assertThat(controller.versions("MDR200001")).isEmpty();
        assertThat(service.lastQuery).isEqualTo(new MasterDataRecordApplicationService.Query("SKU", 3, 1, 20));
        assertThat(service.lastVersionRecordNo).isEqualTo("MDR200001");
    }

    /**
     * StubRecordService。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class StubRecordService extends MasterDataRecordApplicationService {

        /**
         * lastCreateCommand（类型：{@code MasterDataRecordApplicationService.CreateRecordCommand}）。
         *
         * <p>保存当前对象所需的用例输入命令；其具体生命周期由所属对象统一管理。
         */
        MasterDataRecordApplicationService.CreateRecordCommand lastCreateCommand;

        /**
         * lastQuery（类型：{@code MasterDataRecordApplicationService.Query}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        MasterDataRecordApplicationService.Query lastQuery;

        /**
         * lastVersionRecordNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        String lastVersionRecordNo;

        /**
         * 创建 StubRecordService。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         */
        StubRecordService() {
            super(null, null);
        }

        /**
         * 执行命令 {@code create}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param command 用例输入命令，类型为 {@code MasterDataRecordApplicationService.CreateRecordCommand}
         * @return 执行命令的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
         */
        @Override
        public MasterDataRecordMapper.RecordRow create(MasterDataRecordApplicationService.CreateRecordCommand command) {
            lastCreateCommand = command;
            return new MasterDataRecordMapper.RecordRow(null, "MDR200001", "SKU", "SKU-001", "测试商品", "{}", 1, 0, null, 1);
        }

        /**
         * 查询并返回 {@code list}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param query 业务处理参数或成员，类型为 {@code MasterDataRecordApplicationService.Query}
         * @return 查询并返回的结果，类型为 {@code List<MasterDataRecordMapper.RecordRow>}
         */
        @Override
        public List<MasterDataRecordMapper.RecordRow> list(MasterDataRecordApplicationService.Query query) {
            lastQuery = query;
            return List.of();
        }

        /**
         * 查询并返回 {@code listVersions}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param recordNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code List<MasterDataRecordMapper.VersionRow>}
         */
        @Override
        public List<MasterDataRecordMapper.VersionRow> listVersions(String recordNo) {
            lastVersionRecordNo = recordNo;
            return List.of();
        }
    }
}
