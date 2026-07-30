package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmPublicationApplicationService;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmPublicationMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * MdmPublicationControllerTest。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class MdmPublicationControllerTest {

    /**
     * 处理当前类型职责中的操作 {@code delegatesPublicationCommandsToApplicationService}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void delegatesPublicationCommandsToApplicationService() {
        StubPublicationService service = new StubPublicationService();
        MdmPublicationController controller = new MdmPublicationController(service);
        MdmPublicationApplicationService.CreateSubscriptionCommand command = new MdmPublicationApplicationService.CreateSubscriptionCommand("SKU", "WMS", "mdm.sku.changed", null, 1001L, "idem-1");
        MdmPublicationMapper.SubscriptionRow created = controller.createSubscription(command);
        assertThat(created.subscriptionNo()).isEqualTo("SUB300001");
        assertThat(service.lastCreateSubscriptionCommand).isEqualTo(command);
    }

    /**
     * 处理当前类型职责中的操作 {@code openApiReceiptEndpointUsesInboxConsumer}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void openApiReceiptEndpointUsesInboxConsumer() {
        StubPublicationService service = new StubPublicationService();
        MdmPublicationReceiptOpenApiController controller = new MdmPublicationReceiptOpenApiController(service);
        MdmPublicationApplicationService.ReceiptEvent event = new MdmPublicationApplicationService.ReceiptEvent("evt-1", "MdmPublicationReceiptReceived", "PUB400001", "SUCCESS", null, "{}");
        controller.receipt(event);
        assertThat(service.lastReceiptEvent).isEqualTo(event);
    }

    /**
     * 转换数据模型 {@code mapsPublishAndListEndpoints}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void mapsPublishAndListEndpoints() {
        StubPublicationService service = new StubPublicationService();
        MdmPublicationController controller = new MdmPublicationController(service);
        MdmPublicationApplicationService.PublishCommand command = new MdmPublicationApplicationService.PublishCommand("MDV200001V1", 1001L, "idem-1");
        assertThat(controller.publish(command)).isEmpty();
        assertThat(controller.publications()).isEmpty();
        assertThat(service.lastPublishCommand).isEqualTo(command);
    }

    /**
     * StubPublicationService。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class StubPublicationService extends MdmPublicationApplicationService {

        /**
         * lastCreateSubscriptionCommand（类型：{@code MdmPublicationApplicationService.CreateSubscriptionCommand}）。
         *
         * <p>保存当前对象所需的用例输入命令；其具体生命周期由所属对象统一管理。
         */
        MdmPublicationApplicationService.CreateSubscriptionCommand lastCreateSubscriptionCommand;

        /**
         * lastPublishCommand（类型：{@code MdmPublicationApplicationService.PublishCommand}）。
         *
         * <p>保存当前对象所需的用例输入命令；其具体生命周期由所属对象统一管理。
         */
        MdmPublicationApplicationService.PublishCommand lastPublishCommand;

        /**
         * lastReceiptEvent（类型：{@code MdmPublicationApplicationService.ReceiptEvent}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        MdmPublicationApplicationService.ReceiptEvent lastReceiptEvent;

        /**
         * 创建 StubPublicationService。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         */
        StubPublicationService() {
            super(null, null);
        }

        /**
         * 执行命令 {@code createSubscription}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param command 用例输入命令，类型为 {@code MdmPublicationApplicationService.CreateSubscriptionCommand}
         * @return 执行命令的结果，类型为 {@code MdmPublicationMapper.SubscriptionRow}
         */
        @Override
        public MdmPublicationMapper.SubscriptionRow createSubscription(MdmPublicationApplicationService.CreateSubscriptionCommand command) {
            lastCreateSubscriptionCommand = command;
            return new MdmPublicationMapper.SubscriptionRow(null, "SUB300001", "SKU", "WMS", "mdm.sku.changed", null, 1, 1);
        }

        /**
         * 执行命令 {@code publish}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param command 用例输入命令，类型为 {@code MdmPublicationApplicationService.PublishCommand}
         * @return 执行命令的结果，类型为 {@code List<MdmPublicationMapper.PublicationRow>}
         */
        @Override
        public List<MdmPublicationMapper.PublicationRow> publish(MdmPublicationApplicationService.PublishCommand command) {
            lastPublishCommand = command;
            return List.of();
        }

        /**
         * 查询并返回 {@code listPublications}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<MdmPublicationMapper.PublicationRow>}
         */
        @Override
        public List<MdmPublicationMapper.PublicationRow> listPublications() {
            return List.of();
        }

        /**
         * 执行命令 {@code consumeReceipt}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param event 业务处理参数或成员，类型为 {@code MdmPublicationApplicationService.ReceiptEvent}
         */
        @Override
        public void consumeReceipt(MdmPublicationApplicationService.ReceiptEvent event) {
            lastReceiptEvent = event;
        }
    }
}
