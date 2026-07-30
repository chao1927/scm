package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.oms.application.ReverseAfterSaleApplicationService;
import com.chaobo.scm.oms.infrastructure.persistence.ReverseAfterSaleMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ReverseAfterSaleController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'oms:*', 'oms:after-sale:manage')")
public class ReverseAfterSaleController {

    /**
     * service（类型：{@code ReverseAfterSaleApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final ReverseAfterSaleApplicationService service;

    /**
     * 创建 ReverseAfterSaleController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code ReverseAfterSaleApplicationService}
     */
    public ReverseAfterSaleController(ReverseAfterSaleApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code ReverseAfterSaleApplicationService.Create}
     * @return 执行命令的结果，类型为 {@code ReverseAfterSaleMapper.Row}
     */
    @PostMapping("/api/oms/v1/reverse-after-sales")
    public ReverseAfterSaleMapper.Row create(@RequestBody ReverseAfterSaleApplicationService.Create body) {
        return service.create(body);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code Version}
     * @return 执行命令的结果，类型为 {@code ReverseAfterSaleMapper.Row}
     */
    @PostMapping("/api/oms/v1/reverse-after-sales/{no}/approve")
    public ReverseAfterSaleMapper.Row approve(@PathVariable String no, @RequestBody Version body) {
        return service.approve(no, body.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code refund}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code Version}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ReverseAfterSaleMapper.Row}
     */
    @PostMapping("/api/oms/v1/reverse-after-sales/{no}/request-refund")
    public ReverseAfterSaleMapper.Row refund(@PathVariable String no, @RequestBody Version body) {
        return service.requestRefund(no, body.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code reship}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code Version}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ReverseAfterSaleMapper.Row}
     */
    @PostMapping("/api/oms/v1/reverse-after-sales/{no}/request-reship")
    public ReverseAfterSaleMapper.Row reship(@PathVariable String no, @RequestBody Version body) {
        return service.requestReship(no, body.version());
    }

    /**
     * 查询并返回 {@code get}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ReverseAfterSaleMapper.Row}
     */
    @GetMapping("/api/oms/v1/reverse-after-sales/{no}")
    public ReverseAfterSaleMapper.Row get(@PathVariable String no) {
        return service.get(no);
    }

    /**
     * Version。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Version(long version) {
    }

}
