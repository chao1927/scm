package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.oms.application.FulfillmentApplicationService;
import com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OmsInventoryController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/oms/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'oms:*', 'oms:inventory:manage')")
public class OmsInventoryController {

    /**
     * service（类型：{@code FulfillmentApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final FulfillmentApplicationService service;

    /**
     * 创建 OmsInventoryController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code FulfillmentApplicationService}
     */
    public OmsInventoryController(FulfillmentApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code reserve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code FulfillmentApplicationService.ReserveCommand}
     * @return 执行命令的结果，类型为 {@code FulfillmentMapper.FulfillmentRow}
     */
    @PostMapping("/fulfillments/{fulfillmentNo}/reserve")
    public FulfillmentMapper.FulfillmentRow reserve(@PathVariable String fulfillmentNo, @RequestBody FulfillmentApplicationService.ReserveCommand command) {
        return service.reserve(fulfillmentNo, command);
    }

    /**
     * 执行命令 {@code release}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reservationRefNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code FulfillmentApplicationService.ReleaseCommand}
     * @return 执行命令的结果，类型为 {@code FulfillmentMapper.FulfillmentRow}
     */
    @PostMapping("/reservations/{reservationRefNo}/release")
    public FulfillmentMapper.FulfillmentRow release(@PathVariable String reservationRefNo, @RequestBody FulfillmentApplicationService.ReleaseCommand command) {
        return service.releaseReservation(reservationRefNo, command);
    }
}
