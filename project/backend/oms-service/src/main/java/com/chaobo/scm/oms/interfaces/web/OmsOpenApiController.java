package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.oms.application.OmsQueryApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OmsOpenApiController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/openapi/oms/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'oms:*', 'oms:openapi:read')")
public class OmsOpenApiController {

    /**
     * service（类型：{@code OmsQueryApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final OmsQueryApplicationService service;

    /**
     * 创建 OmsOpenApiController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code OmsQueryApplicationService}
     */
    public OmsOpenApiController(OmsQueryApplicationService service) {
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code order}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OmsQueryApplicationService.ExternalOrderView}
     */
    @GetMapping("/orders/{orderNo}")
    public OmsQueryApplicationService.ExternalOrderView order(@PathVariable String orderNo) {
        return service.order(orderNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code fulfillment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OmsQueryApplicationService.ExternalOrderView}
     */
    @GetMapping("/fulfillments/{fulfillmentNo}/tracking")
    public OmsQueryApplicationService.ExternalOrderView fulfillment(@PathVariable String fulfillmentNo) {
        return service.fulfillment(fulfillmentNo);
    }
}
