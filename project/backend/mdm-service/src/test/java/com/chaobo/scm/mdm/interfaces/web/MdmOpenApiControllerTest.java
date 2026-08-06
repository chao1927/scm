package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmOpenApiApplicationService;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * MdmOpenApiControllerTest。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class MdmOpenApiControllerTest {

    /**
     * 处理当前类型职责中的操作 {@code delegatesOpenApiQuery}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void delegatesOpenApiQuery() {
        StubOpenApiService service = new StubOpenApiService();
        MdmMasterDataOpenApiController openApiController = new MdmMasterDataOpenApiController(service);
        MdmOpenApiApplicationService.QueryRequest query = new MdmOpenApiApplicationService.QueryRequest(List.of(new MdmOpenApiApplicationService.QueryItem("SKU", "SKU-001")));
        assertThat(openApiController.query(query).items()).hasSize(1);
        assertThat(service.lastQueryRequest).isEqualTo(query);
    }

    /**
     * StubOpenApiService。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class StubOpenApiService extends MdmOpenApiApplicationService {

        /**
         * lastQueryRequest（类型：{@code MdmOpenApiApplicationService.QueryRequest}）。
         *
         * <p>保存当前对象所需的接口请求参数；其具体生命周期由所属对象统一管理。
         */
        MdmOpenApiApplicationService.QueryRequest lastQueryRequest;

        /**
         * 创建 StubOpenApiService。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         */
        StubOpenApiService() {
            super(null, null, null, null);
        }

        /**
         * 查询并返回 {@code query}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param request 接口请求参数，类型为 {@code MdmOpenApiApplicationService.QueryRequest}
         * @return 查询并返回的结果，类型为 {@code MdmOpenApiApplicationService.QueryResponse}
         */
        @Override
        public MdmOpenApiApplicationService.QueryResponse query(MdmOpenApiApplicationService.QueryRequest request) {
            lastQueryRequest = request;
            return new MdmOpenApiApplicationService.QueryResponse(List.of(new MdmOpenApiApplicationService.Snapshot("MDR200001", "SKU", "SKU-001", "测试商品", "{}", 3, 1, 3)));
        }

    }
}
