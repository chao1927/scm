package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.purchase.application.integration.PurchaseExternalEvent;
import com.chaobo.scm.purchase.application.integration.PurchaseExternalEventHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 采购外部事件人工补偿兼容入口。
 *
 * <p>该 HTTP 接口只供具有专用权限的运维人员补偿历史失败消息，不参与自动业务
 * 事件链路。系统间业务事件必须通过 RocketMQ PushConsumer 进入同一个 Inbox 应用
 * 服务，禁止调用方把该接口作为消息传输或生产运行时降级通道。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/internal/purchase/v1/events")
public class PurchaseExternalEventController {

    private static final Logger LOG =
            LoggerFactory.getLogger(PurchaseExternalEventController.class);

    /**
     * service（类型：{@code PurchaseExternalEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseExternalEventHandler service;

    /**
     * 创建 PurchaseExternalEventController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 统一经过 Inbox 的外部事件处理端口
     */
    public PurchaseExternalEventController(PurchaseExternalEventHandler service) {
        this.service = service;
    }

    /**
     * 人工补偿一个外部业务事件。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Request}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping
    public ApiResponse<Void> manualConsume(
            @Valid @RequestBody Request body,
            HttpServletRequest request,
            Authentication authentication) {
        ScmAccessContext access = access(authentication);
        access.requirePermission("purchase:event:manual-consume");
        String operationReason = request.getHeader("X-Manual-Operation-Reason");
        if (operationReason == null || operationReason.isBlank()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "人工事件补偿必须填写 X-Manual-Operation-Reason"
            );
        }
        var sourceSystem = request.getHeader("X-Source-System");
        var eventCode = request.getHeader("X-Event-Code");
        if (sourceSystem == null || sourceSystem.isBlank() || eventCode == null || eventCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "缺少外部事件请求头");
        }
        LOG.info(
                "采购外部事件人工补偿，operatorId={}, sourceSystem={}, eventCode={}, reason={}",
                access.operatorId(),
                sourceSystem,
                eventCode,
                operationReason.trim().replace('\r', ' ').replace('\n', ' ')
        );
        service.consume(body.toEvent(sourceSystem, eventCode));
        return ApiResponse.success(null, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    private static ScmAccessContext access(Authentication authentication) {
        if (authentication == null
                || !(authentication.getDetails() instanceof ScmAccessContext access)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "当前请求没有有效访问令牌");
        }
        return access;
    }

    /**
     * Request。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Request(@NotBlank String eventType, String businessNo, String orderNo, String rfqNo, String quoteNo, String inboundNo, String asnNo, Long supplierId, Long purchaseOrgId, String warehouseCode, String skuCode, BigDecimal quantity, BigDecimal receivedQty, BigDecimal qualifiedQty, BigDecimal unqualifiedQty, BigDecimal putawayQty, BigDecimal amount, String currency, String shipmentId, String waybillNo, String carrierCode, String transportNode, String status, String reason, Integer sourceVersion, OffsetDateTime occurredAt, Map<String, Object> payload) {

        /**
         * 转换数据模型 {@code toEvent}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param eventCode 可追踪业务编码，类型为 {@code String}
         * @return 转换数据模型的结果，类型为 {@code PurchaseExternalEvent}
         */
        PurchaseExternalEvent toEvent(String sourceSystem, String eventCode) {
            return new PurchaseExternalEvent(sourceSystem, eventCode, eventType, businessNo, orderNo, rfqNo, quoteNo, inboundNo, asnNo, supplierId, purchaseOrgId, warehouseCode, skuCode, quantity, receivedQty, qualifiedQty, unqualifiedQty, putawayQty, amount, currency, shipmentId, waybillNo, carrierCode, transportNode, status, reason, sourceVersion, occurredAt, payload);
        }
    }
}
