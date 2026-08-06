package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.iam.application.IamAdminApplicationService;
import com.chaobo.scm.iam.infrastructure.persistence.IamAdminMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * IamAdminControllerTest。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class IamAdminControllerTest {

    /**
     * 处理当前类型职责中的操作 {@code delegatesAppCommands}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void delegatesAppCommands() {
        StubAdminService service = new StubAdminService();
        IamAdminController adminController = new IamAdminController(service);
        IamAdminApplicationService.CreateAppCommand createApp = new IamAdminApplicationService.CreateAppCommand("OMS", "订单系统", "/oms", 1001L, "idem-1");
        IamAdminMapper.AppRow app = adminController.createApp(createApp);
        assertThat(app.appCode()).isEqualTo("OMS");
        assertThat(service.lastCreateAppCommand).isEqualTo(createApp);
    }

    /**
     * StubAdminService。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class StubAdminService extends IamAdminApplicationService {

        /**
         * lastCreateAppCommand（类型：{@code IamAdminApplicationService.CreateAppCommand}）。
         *
         * <p>保存当前对象所需的用例输入命令；其具体生命周期由所属对象统一管理。
         */
        IamAdminApplicationService.CreateAppCommand lastCreateAppCommand;

        /**
         * 创建 StubAdminService。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         */
        StubAdminService() {
            super(null);
        }

        /**
         * 执行命令 {@code createApp}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param command 用例输入命令，类型为 {@code IamAdminApplicationService.CreateAppCommand}
         * @return 执行命令的结果，类型为 {@code IamAdminMapper.AppRow}
         */
        @Override
        public IamAdminMapper.AppRow createApp(IamAdminApplicationService.CreateAppCommand command) {
            lastCreateAppCommand = command;
            return new IamAdminMapper.AppRow(null, command.appCode(), command.appName(), command.homeUrl(), 1, 1);
        }

        /**
         * 查询并返回 {@code listApps}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<IamAdminMapper.AppRow>}
         */
        @Override
        public List<IamAdminMapper.AppRow> listApps() {
            return List.of();
        }

    }
}
