package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.iam.application.IamPermissionOpenApiApplicationService;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * IamPermissionOpenApiControllerTest。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class IamPermissionOpenApiControllerTest {

    /**
     * 处理当前类型职责中的操作 {@code delegatesPermissionOpenApiCommands}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void delegatesPermissionOpenApiCommands() {
        StubPermissionService service = new StubPermissionService();
        IamPermissionOpenApiController controller = new IamPermissionOpenApiController(service);
        IamPermissionOpenApiApplicationService.TokenValidationCommand command = new IamPermissionOpenApiApplicationService.TokenValidationCommand("token");
        IamPermissionOpenApiApplicationService.TokenValidationResult result = controller.validate(command);
        assertThat(result.valid()).isTrue();
        assertThat(service.lastTokenCommand).isEqualTo(command);
    }

    /**
     * StubPermissionService。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class StubPermissionService extends IamPermissionOpenApiApplicationService {

        /**
         * lastTokenCommand（类型：{@code IamPermissionOpenApiApplicationService.TokenValidationCommand}）。
         *
         * <p>保存当前对象所需的用例输入命令；其具体生命周期由所属对象统一管理。
         */
        IamPermissionOpenApiApplicationService.TokenValidationCommand lastTokenCommand;

        /**
         * 创建 StubPermissionService。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         */
        StubPermissionService() {
            super(null, null, new com.chaobo.scm.iam.infrastructure.jwt.IamJwtService(
                    "01234567890123456789012345678901"), null);
        }

        /**
         * 校验业务约束 {@code validateToken}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param command 用例输入命令，类型为 {@code IamPermissionOpenApiApplicationService.TokenValidationCommand}
         * @return 校验业务约束的结果，类型为 {@code IamPermissionOpenApiApplicationService.TokenValidationResult}
         */
        @Override
        public IamPermissionOpenApiApplicationService.TokenValidationResult validateToken(IamPermissionOpenApiApplicationService.TokenValidationCommand command) {
            lastTokenCommand = command;
            return new IamPermissionOpenApiApplicationService.TokenValidationResult(true, 1001L, "admin", "IAM", 1);
        }
    }
}
