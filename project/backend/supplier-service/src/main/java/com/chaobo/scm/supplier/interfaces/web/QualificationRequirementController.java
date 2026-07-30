package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.supplier.application.qualification.QualificationRequirementApplicationService;
import com.chaobo.scm.supplier.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * QualificationRequirementController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/supplier/v1/qualification-requirements")
public class QualificationRequirementController {

    /**
     * service（类型：{@code QualificationRequirementApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final QualificationRequirementApplicationService service;

    /**
     * contexts（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contexts;

    /**
     * 创建 QualificationRequirementController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code QualificationRequirementApplicationService}
     * @param contexts 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public QualificationRequirementController(QualificationRequirementApplicationService service, CommandContextFactory contexts) {
        this.service = service;
        this.contexts = contexts;
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 查询并返回的结果，类型为 {@code ApiResponse<List<QualificationRequirementApplicationService.View>>}
     */
    @GetMapping
    public ApiResponse<List<QualificationRequirementApplicationService.View>> list(HttpServletRequest r) {
        return ApiResponse.success(service.list(), r.getHeader("X-Request-Id"), r.getHeader("X-Trace-Id"));
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param b 业务处理参数或成员，类型为 {@code Save}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Long>}
     */
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody Save b, HttpServletRequest r, Authentication a) {
        return ApiResponse.success(service.create(b.supplierType(), b.categoryId(), b.qualificationType(), b.mandatory(), contexts.create(r, a)), r.getHeader("X-Request-Id"), r.getHeader("X-Trace-Id"));
    }

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code Update}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable long id, @Valid @RequestBody Update b, HttpServletRequest r, Authentication a) {
        service.update(id, b.version(), b.supplierType(), b.categoryId(), b.qualificationType(), b.mandatory(), b.status(), contexts.create(r, a));
        return ApiResponse.success(null, r.getHeader("X-Request-Id"), r.getHeader("X-Trace-Id"));
    }

    /**
     * Save。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Save(String supplierType, Long categoryId, @NotBlank String qualificationType, boolean mandatory) {
    }

    /**
     * Update。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Update(@PositiveOrZero int version, String supplierType, Long categoryId, @NotBlank String qualificationType, boolean mandatory, @Min(1) @Max(2) int status) {
    }
}
