package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.wms.application.returning.ReturnOperationApplicationService;
import com.chaobo.scm.wms.infrastructure.security.WmsAccessControl;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

/**
 * ReturnOperationController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/wms/v1/return-operations")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*','wms:*','wms:return:write')")
public class ReturnOperationController {

    /**
     * service（类型：{@code ReturnOperationApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final ReturnOperationApplicationService service;

    /**
     * 创建 ReturnOperationController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code ReturnOperationApplicationService}
     */
    public ReturnOperationController(ReturnOperationApplicationService service) {
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code receive}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param b 业务处理参数或成员，类型为 {@code Quantity}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ReturnOperationApplicationService.Result}
     */
    @PostMapping("/{no}/receive")
    public ReturnOperationApplicationService.Result receive(@PathVariable String no, @RequestBody Quantity b, Authentication a) {
        var r = service.detail(no);
        WmsAccessControl.requireWarehouse(a, r.warehouseId());
        return service.receive(no, b.qty(), b.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code inspect}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param b 业务处理参数或成员，类型为 {@code ReturnOperationApplicationService.Inspect}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ReturnOperationApplicationService.Result}
     */
    @PostMapping("/{no}/inspect")
    public ReturnOperationApplicationService.Result inspect(@PathVariable String no, @RequestBody ReturnOperationApplicationService.Inspect b, Authentication a) {
        var r = service.detail(no);
        WmsAccessControl.requireWarehouse(a, r.warehouseId());
        return service.inspect(no, b);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ReturnOperationApplicationService.Result}
     */
    @GetMapping("/{no}")
    public ReturnOperationApplicationService.Result detail(@PathVariable String no, Authentication a) {
        var r = service.detail(no);
        WmsAccessControl.requireWarehouse(a, r.warehouseId());
        return r;
    }

    /**
     * Quantity。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Quantity(BigDecimal qty, int version) {
    }
}
