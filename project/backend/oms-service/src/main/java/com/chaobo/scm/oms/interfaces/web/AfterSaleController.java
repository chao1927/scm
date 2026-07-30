package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.oms.application.AfterSaleApplicationService;
import com.chaobo.scm.oms.infrastructure.persistence.CancellationMapper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AfterSaleController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/oms/v1/after-sales")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'oms:*', 'oms:after-sale:manage')")
public class AfterSaleController {

    /**
     * service（类型：{@code AfterSaleApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final AfterSaleApplicationService service;

    /**
     * 创建 AfterSaleController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code AfterSaleApplicationService}
     */
    public AfterSaleController(AfterSaleApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code AfterSaleApplicationService.CreateCommand}
     * @return 执行命令的结果，类型为 {@code CancellationMapper.AfterSaleRow}
     */
    @PostMapping
    public CancellationMapper.AfterSaleRow create(@RequestBody AfterSaleApplicationService.CreateCommand command) {
        return service.create(command);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param afterSaleNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code AfterSaleApplicationService.ApproveCommand}
     * @return 执行命令的结果，类型为 {@code CancellationMapper.AfterSaleRow}
     */
    @PostMapping("/{afterSaleNo}/approve")
    public CancellationMapper.AfterSaleRow approve(@PathVariable String afterSaleNo, @RequestBody AfterSaleApplicationService.ApproveCommand command) {
        return service.approve(afterSaleNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code requestRefund}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param afterSaleNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code AfterSaleApplicationService.RefundCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CancellationMapper.AfterSaleRow}
     */
    @PostMapping("/{afterSaleNo}/request-refund")
    public CancellationMapper.AfterSaleRow requestRefund(@PathVariable String afterSaleNo, @RequestBody AfterSaleApplicationService.RefundCommand command) {
        return service.requestRefund(afterSaleNo, command);
    }

    /**
     * 执行命令 {@code complete}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param afterSaleNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code AfterSaleApplicationService.CompleteCommand}
     * @return 执行命令的结果，类型为 {@code CancellationMapper.AfterSaleRow}
     */
    @PostMapping("/{afterSaleNo}/complete")
    public CancellationMapper.AfterSaleRow complete(@PathVariable String afterSaleNo, @RequestBody AfterSaleApplicationService.CompleteCommand command) {
        return service.complete(afterSaleNo, command);
    }

}
