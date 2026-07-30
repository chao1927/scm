package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.packing.PackingApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackingController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/wms/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'wms:*', 'wms:packing:write')")
public class PackingController {

    /**
     * service（类型：{@code PackingApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final PackingApplicationService service;

    /**
     * 创建 PackingController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code PackingApplicationService}
     */
    public PackingController(PackingApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code bindContainer}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code BindContainer}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<PackingApplicationService.ContainerResult>}
     */
    @PostMapping("/containers/bind")
    public ApiResponse<PackingApplicationService.ContainerResult> bindContainer(@Valid @RequestBody BindContainer body, HttpServletRequest request) {
        return ok(service.bindContainer(body.containerNo(), body.outboundId(), body.pickTaskId()), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code sealContainer}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code SealContainer}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PackingApplicationService.ContainerResult>}
     */
    @PostMapping("/containers/seal")
    public ApiResponse<PackingApplicationService.ContainerResult> sealContainer(@Valid @RequestBody SealContainer body, HttpServletRequest request) {
        return ok(service.sealContainer(body.containerNo(), body.version()), request);
    }

    /**
     * 执行命令 {@code createPacking}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code CreatePacking}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<PackingApplicationService.PackingResult>}
     */
    @PostMapping("/packing")
    public ApiResponse<PackingApplicationService.PackingResult> createPacking(@Valid @RequestBody CreatePacking body, HttpServletRequest request) {
        return ok(service.createPacking(body.packingNo(), body.outboundId(), body.containerNo()), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code verifyPacking}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code VerifyPacking}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PackingApplicationService.PackingResult>}
     */
    @PostMapping("/packing/verify")
    public ApiResponse<PackingApplicationService.PackingResult> verifyPacking(@Valid @RequestBody VerifyPacking body, HttpServletRequest request) {
        return ok(service.verifyPacking(body.packingNo(), body.version()), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param data 业务处理参数或成员，类型为 {@code T}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<T>}
     */
    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * BindContainer。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record BindContainer(@NotBlank String containerNo, @Positive long outboundId, @Positive long pickTaskId) {
    }

    /**
     * SealContainer。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record SealContainer(@NotBlank String containerNo, @PositiveOrZero int version) {
    }

    /**
     * CreatePacking。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreatePacking(@NotBlank String packingNo, @Positive long outboundId, @NotBlank String containerNo) {
    }

    /**
     * VerifyPacking。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record VerifyPacking(@NotBlank String packingNo, @PositiveOrZero int version) {
    }
}
