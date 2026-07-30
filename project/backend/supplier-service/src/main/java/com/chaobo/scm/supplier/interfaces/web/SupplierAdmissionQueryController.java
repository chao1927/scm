package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.supplier.application.profile.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * SupplierAdmissionQueryController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/supplier/v1/admissions")
public class SupplierAdmissionQueryController {

    /**
     * service（类型：{@code SupplierAdmissionQueryApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierAdmissionQueryApplicationService service;

    /**
     * 创建 SupplierAdmissionQueryController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code SupplierAdmissionQueryApplicationService}
     */
    public SupplierAdmissionQueryController(SupplierAdmissionQueryApplicationService service) {
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PageResult<SupplierAdmissionView>>}
     */
    @GetMapping
    @PreAuthorize("hasAuthority('supplier:admission:view')")
    public ApiResponse<PageResult<SupplierAdmissionView>> page(@RequestParam(required = false) Integer status, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, HttpServletRequest request) {
        return ApiResponse.success(service.page(status, keyword, pageNo, pageSize), request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<SupplierAdmissionView>}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier:admission:view')")
    public ApiResponse<SupplierAdmissionView> detail(@PathVariable long id, HttpServletRequest request) {
        return ApiResponse.success(service.detail(id), request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }
}
