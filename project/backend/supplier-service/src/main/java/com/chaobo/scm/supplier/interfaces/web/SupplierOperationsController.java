package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.supplier.application.integration.InboundEventReplayApplicationService;
import com.chaobo.scm.supplier.application.operations.*;
import com.chaobo.scm.supplier.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.security.core.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * SupplierOperationsController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/supplier/v1/operations")
public class SupplierOperationsController {

    /**
     * service（类型：{@code SupplierOperationsApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierOperationsApplicationService service;

    /**
     * inboundReplay（类型：{@code InboundEventReplayApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InboundEventReplayApplicationService inboundReplay;

    /**
     * contexts（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contexts;

    /**
     * 创建 SupplierOperationsController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code SupplierOperationsApplicationService}
     * @param inboundReplay 业务处理参数或成员，类型为 {@code InboundEventReplayApplicationService}
     * @param contexts 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public SupplierOperationsController(SupplierOperationsApplicationService service, InboundEventReplayApplicationService inboundReplay, CommandContextFactory contexts) {
        this.service = service;
        this.inboundReplay = inboundReplay;
        this.contexts = contexts;
    }

    /**
     * 处理当前类型职责中的操作 {@code work}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<OperationViews.WorkItem>>}
     */
    @GetMapping("/work-items")
    public ApiResponse<List<OperationViews.WorkItem>> work(@RequestParam(required = false) Long supplierId, @RequestParam(required = false) Integer status, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return ok(service.workItems(supplierId, scope(jwt), status, pageNo, pageSize), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code processWork}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code Process}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/work-items/{id}/process")
    public ApiResponse<Void> processWork(@PathVariable long id, @Valid @RequestBody Process body, HttpServletRequest request, Authentication authentication) {
        service.processWork(id, body.version(), body.close(), contexts.create(request, authentication));
        return ok(null, request);
    }

    /**
     * 处理当前类型职责中的操作 {@code warnings}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<OperationViews.Warning>>}
     */
    @GetMapping("/warnings")
    public ApiResponse<List<OperationViews.Warning>> warnings(@RequestParam(required = false) Long supplierId, @RequestParam(required = false) Integer status, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return ok(service.warnings(supplierId, scope(jwt), status, pageNo, pageSize), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code processWarning}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code Process}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/warnings/{id}/process")
    public ApiResponse<Void> processWarning(@PathVariable long id, @Valid @RequestBody Process body, HttpServletRequest request, Authentication authentication) {
        service.processWarning(id, body.version(), body.close(), contexts.create(request, authentication));
        return ok(null, request);
    }

    /**
     * 处理当前类型职责中的操作 {@code failed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<OperationViews.FailedEvent>>}
     */
    @GetMapping("/failed-events")
    public ApiResponse<List<OperationViews.FailedEvent>> failed(HttpServletRequest request) {
        return ok(service.failedEvents(), request);
    }

    /**
     * 执行命令 {@code replay}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code Replay}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/failed-events/{id}/replay")
    public ApiResponse<Void> replay(@PathVariable long id, @Valid @RequestBody Replay body, HttpServletRequest request, Authentication authentication) {
        var context = contexts.create(request, authentication);
        if (INBOUND.equals(body.direction())) {
            inboundReplay.replay(id, body.reason(), context);
        } else {
            service.replay(id, body.direction(), body.reason(), context);
        }
        return ok(null, request);
    }

    /**
     * 处理当前类型职责中的操作 {@code reconcile}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Reconcile}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/data-reconciliations")
    public ApiResponse<Void> reconcile(@Valid @RequestBody Reconcile body, HttpServletRequest request, Authentication authentication) {
        service.reconcile(body.type(), body.targetSystem(), body.businessDate(), body.remoteCount(), body.remoteAmount(), contexts.create(request, authentication));
        return ok(null, request);
    }

    /**
     * 处理当前类型职责中的操作 {@code reconciliations}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<OperationViews.Reconciliation>>}
     */
    @GetMapping("/data-reconciliations")
    public ApiResponse<List<OperationViews.Reconciliation>> reconciliations(HttpServletRequest request) {
        return ok(service.reconciliations(), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code dashboard}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<OperationViews.Dashboard>}
     */
    @GetMapping("/dashboard")
    public ApiResponse<OperationViews.Dashboard> dashboard(HttpServletRequest request) {
        return ok(service.dashboard(), request);
    }

    /** 查询供应商协同的持久化操作审计日志。 */
    @GetMapping("/operation-logs")
    @PreAuthorize("hasAnyAuthority('*', 'supplier:*', 'supplier:operation-log:read')")
    public ApiResponse<List<OperationViews.OperationLog>> operationLogs(
            @RequestParam(defaultValue = "100") int limit, HttpServletRequest request) {
        return ok(service.operationLogs(limit), request);
    }

    /**
     * 执行命令 {@code createExport}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code ExportCreate}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Long>}
     */
    @PostMapping("/exports")
    @PreAuthorize("hasAnyAuthority('*', 'supplier:*', 'supplier:export:create')")
    public ApiResponse<Long> createExport(@Valid @RequestBody ExportCreate body, HttpServletRequest request, Authentication authentication) {
        return ok(service.createExport(body.exportType(), body.supplierId(), body.queryJson(), contexts.create(request, authentication)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code exports}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<OperationViews.ExportTask>>}
     */
    @GetMapping("/exports")
    @PreAuthorize("hasAnyAuthority('*', 'supplier:*', 'supplier:export:read')")
    public ApiResponse<List<OperationViews.ExportTask>> exports(@RequestParam(required = false) Long supplierId, @RequestParam(required = false) Integer status, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return ok(service.exportTasks(supplierId, scope(jwt), status, pageNo, pageSize), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code exportDetail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<OperationViews.ExportTask>}
     */
    @GetMapping("/exports/{id}")
    @PreAuthorize("hasAnyAuthority('*', 'supplier:*', 'supplier:export:read')")
    public ApiResponse<OperationViews.ExportTask> exportDetail(@PathVariable long id,
                                                               @AuthenticationPrincipal Jwt jwt,
                                                               HttpServletRequest request) {
        return ok(service.exportTask(id, scope(jwt)), request);
    }

    /**
     * 人工重试失败导出任务。
     */
    @PostMapping("/exports/{id}/retry")
    @PreAuthorize("hasAnyAuthority('*', 'supplier:*', 'supplier:export:retry')")
    public ApiResponse<Void> retryExport(@PathVariable long id, @Valid @RequestBody ExportRetry body,
                                         HttpServletRequest request, Authentication authentication) {
        service.retryExport(id, body.version(), contexts.create(request, authentication));
        return ok(null, request);
    }

    /**
     * 下载已完成的真实导出文件。
     */
    @GetMapping("/exports/{id}/file")
    @PreAuthorize("hasAnyAuthority('*', 'supplier:*', 'supplier:export:read')")
    public ResponseEntity<byte[]> downloadExport(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        var file = service.downloadExport(id, scope(jwt));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CONTENT_TYPE, file.contentType())
                .body(file.bytes());
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code T}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<T>}
     */
    private <T> ApiResponse<T> ok(T value, HttpServletRequest request) {
        return ApiResponse.success(value, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code scope}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    private Long scope(Jwt jwt) {
        Number value = jwt != null && jwt.hasClaim("supplier_id") ? jwt.getClaim("supplier_id") : null;
        return value == null ? null : value.longValue();
    }

    /**
     * Process。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Process(@PositiveOrZero int version, boolean close) {
    }

    /**
     * Replay。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Replay(@Pattern(regexp = "INBOUND|OUTBOUND") String direction, @NotBlank String reason) {
    }

    /**
     * Reconcile。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Reconcile(@Pattern(regexp = "ASN|SUPPLIER_RETURN|STATEMENT") String type, @NotBlank String targetSystem, @NotNull LocalDate businessDate, @PositiveOrZero long remoteCount, BigDecimal remoteAmount) {
    }

    /**
     * ExportCreate。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ExportCreate(@Pattern(regexp = "WORK_ITEM|WARNING|FAILED_EVENT|RECONCILIATION|SCORE|QUALITY|RETURN") String exportType, Long supplierId, String queryJson) {
    }

    /**
     * ExportComplete。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ExportRetry(@PositiveOrZero int version) {
    }

    /**
     * 业务常量 {@code INBOUND}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String INBOUND = "INBOUND";
}
