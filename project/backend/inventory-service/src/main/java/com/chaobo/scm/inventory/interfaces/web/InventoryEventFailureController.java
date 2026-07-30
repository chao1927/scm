package com.chaobo.scm.inventory.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.common.security.ScmAccessContexts;
import com.chaobo.scm.inventory.application.InventoryEventFailureApplicationService;
import com.chaobo.scm.inventory.application.InventoryEventFailureStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存事件失败查询和人工重放 HTTP 入口。
 *
 * <p>查询和重放权限分离；操作人来自认证上下文，不能由请求体伪造。
 *
 * @author SCM Team
 */
@RestController
@RequestMapping("/api/inventory/v1/event-logs")
public class InventoryEventFailureController {

    private final InventoryEventFailureApplicationService service;

    public InventoryEventFailureController(
            InventoryEventFailureApplicationService service) {
        this.service = service;
    }

    /**
     * 分页查询入站或出站失败事件。
     *
     * @param direction 事件方向
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param request HTTP 请求
     * @param authentication 认证上下文
     * @return 失败事件分页
     */
    @GetMapping("/failures")
    public ApiResponse<PageResult<InventoryEventFailureStore.FailureEvent>> failures(
            @RequestParam InventoryEventFailureStore.Direction direction,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request,
            Authentication authentication) {
        ScmAccessContexts.require(authentication)
                .requirePermission("inventory:event:page");
        return ok(service.failures(direction, pageNo, pageSize), request);
    }

    /**
     * 人工重放指定失败事件。
     *
     * @param direction 事件方向
     * @param eventCode 事件编码
     * @param body 重放原因
     * @param idempotencyKey 请求幂等键
     * @param request HTTP 请求
     * @param authentication 认证上下文
     * @return 重放结果
     */
    @PostMapping("/{direction}/{eventCode}/replay")
    public ApiResponse<InventoryEventFailureApplicationService.ReplayResult> replay(
            @PathVariable InventoryEventFailureStore.Direction direction,
            @PathVariable String eventCode,
            @Valid @RequestBody ReplayRequest body,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            HttpServletRequest request,
            Authentication authentication) {
        ScmAccessContext access = ScmAccessContexts.require(authentication);
        access.requirePermission("inventory:event:manage");
        return ok(service.replay(
                new InventoryEventFailureApplicationService.ReplayCommand(
                        direction,
                        eventCode,
                        idempotencyKey,
                        body.reason(),
                        access.operatorId())), request);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(
                data,
                request.getHeader("X-Request-Id"),
                request.getHeader("X-Trace-Id"));
    }

    /**
     * 人工重放请求。
     */
    public record ReplayRequest(@NotBlank String reason) {
    }
}
