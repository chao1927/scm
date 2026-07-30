package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.oms.application.FulfillmentApplicationService;
import com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * OmsOutboundController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/oms/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'oms:*', 'oms:outbound:manage')")
public class OmsOutboundController {

    /**
     * service（类型：{@code FulfillmentApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final FulfillmentApplicationService service;

    /**
     * 创建 OmsOutboundController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code FulfillmentApplicationService}
     */
    public OmsOutboundController(FulfillmentApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code FulfillmentApplicationService.CreateOutboundCommand}
     * @return 执行命令的结果，类型为 {@code FulfillmentMapper.FulfillmentRow}
     */
    @PostMapping("/fulfillments/{fulfillmentNo}/outbound")
    public FulfillmentMapper.FulfillmentRow create(@PathVariable String fulfillmentNo, @RequestBody FulfillmentApplicationService.CreateOutboundCommand command) {
        return service.createOutbound(fulfillmentNo, command);
    }

    /**
     * 执行命令 {@code dispatch}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code FulfillmentApplicationService.OutboundCommand}
     * @return 执行命令的结果，类型为 {@code FulfillmentMapper.OutboundRow}
     */
    @PostMapping("/outbounds/{outboundNo}/dispatch")
    public FulfillmentMapper.OutboundRow dispatch(@PathVariable String outboundNo, @RequestBody FulfillmentApplicationService.OutboundCommand command) {
        return service.dispatchOutbound(outboundNo, command);
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code FulfillmentApplicationService.CancelOutboundCommand}
     * @return 执行命令的结果，类型为 {@code FulfillmentMapper.OutboundRow}
     */
    @PostMapping("/outbounds/{outboundNo}/cancel")
    public FulfillmentMapper.OutboundRow cancel(@PathVariable String outboundNo, @RequestBody FulfillmentApplicationService.CancelOutboundCommand command) {
        return service.cancelOutbound(outboundNo, command);
    }

    /**
     * 执行命令 {@code retry}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code FulfillmentApplicationService.OutboundCommand}
     * @return 执行命令的结果，类型为 {@code FulfillmentMapper.OutboundRow}
     */
    @PostMapping("/outbounds/{outboundNo}/retry")
    public FulfillmentMapper.OutboundRow retry(@PathVariable String outboundNo, @RequestBody FulfillmentApplicationService.OutboundCommand command) {
        return service.retryOutbound(outboundNo, command);
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<FulfillmentMapper.OutboundRow>}
     */
    @GetMapping("/outbounds")
    public List<FulfillmentMapper.OutboundRow> list() {
        return service.listOutbounds();
    }

    /**
     * 查询并返回 {@code get}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code FulfillmentMapper.OutboundRow}
     */
    @GetMapping("/outbounds/{outboundNo}")
    public FulfillmentMapper.OutboundRow get(@PathVariable String outboundNo) {
        return service.getOutbound(outboundNo);
    }
}
