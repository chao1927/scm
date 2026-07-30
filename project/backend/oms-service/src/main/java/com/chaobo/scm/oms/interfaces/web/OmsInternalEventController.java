package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.common.security.ScmAccessContexts;
import com.chaobo.scm.oms.application.OmsExternalEvent;
import com.chaobo.scm.oms.application.OmsExternalEventHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * OMS 事件人工补偿入口。
 *
 * <p>正常的跨系统业务事件只能通过 RocketMQ 消费者进入 Inbox。本控制器仅供
 * 已授权运维人员在故障恢复时手工重放，并强制填写原因、记录操作者和事件标识。
 */
@RestController
@RequestMapping("/internal/oms/v1")
@org.springframework.security.access.prepost.PreAuthorize(
        "hasAnyAuthority('*', 'oms:*', 'oms:event:manual-consume')")
public class OmsInternalEventController {

    private static final Logger LOG =
            LoggerFactory.getLogger(OmsInternalEventController.class);
    private final OmsExternalEventHandler handler;

    public OmsInternalEventController(OmsExternalEventHandler handler) {
        this.handler = handler;
    }

    /**
     * 人工补偿一条外部业务事件。
     *
     * @param event 待补偿事件
     * @param reason 人工补偿原因
     * @param authentication 当前认证上下文
     */
    @PostMapping("/events")
    public void manualConsume(
            @Valid @RequestBody EventRequest event,
            @RequestHeader("X-Manual-Operation-Reason") @NotBlank String reason,
            Authentication authentication) {
        ScmAccessContext context = ScmAccessContexts.require(authentication);
        context.requirePermission("oms:event:manual-consume");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("人工补偿原因不能为空");
        }
        handler.consume(event.toExternalEvent());
        LOG.warn("OMS 事件已人工补偿，operatorId={},username={},sourceSystem={},"
                        + "eventCode={},eventType={},reason={}",
                context.operatorId(), context.username(), event.sourceSystem(),
                event.eventCode(), event.eventType(), reason.trim());
    }

    /**
     * 人工补偿请求。字段与标准 V1 信封的 {@code data} 对齐。
     */
    public record EventRequest(
            @NotBlank String sourceSystem,
            @NotBlank String eventCode,
            @NotBlank String eventType,
            @NotBlank String businessNo,
            String fulfillmentNo,
            String reservationRefNo,
            String reservationNo,
            BigDecimal quantity,
            String outboundNo,
            String wmsOrderNo,
            String afterSaleNo,
            String reason,
            BigDecimal receivedQty,
            BigDecimal acceptedQty,
            BigDecimal amount,
            boolean unmatched,
            String payload) {

        OmsExternalEvent toExternalEvent() {
            return new OmsExternalEvent(
                    sourceSystem, eventCode, eventType, businessNo, fulfillmentNo,
                    reservationRefNo, reservationNo, quantity, outboundNo,
                    wmsOrderNo, afterSaleNo, reason, receivedQty, acceptedQty,
                    amount, unmatched, payload);
        }
    }
}
