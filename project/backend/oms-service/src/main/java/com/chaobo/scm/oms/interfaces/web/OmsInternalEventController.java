package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.oms.application.FulfillmentApplicationService;
import com.chaobo.scm.oms.application.AfterSaleApplicationService;
import com.chaobo.scm.oms.application.CancellationApplicationService;
import com.chaobo.scm.common.security.ScmAccessContexts;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OmsInternalEventController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/internal/oms/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'oms:*', 'oms:event:consume')")
public class OmsInternalEventController {

    /**
     * service（类型：{@code FulfillmentApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final FulfillmentApplicationService service;

    /**
     * cancellationService（类型：{@code CancellationApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final CancellationApplicationService cancellationService;

    /**
     * afterSaleService（类型：{@code AfterSaleApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final AfterSaleApplicationService afterSaleService;

    /**
     * 创建 OmsInternalEventController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code FulfillmentApplicationService}
     * @param cancellationService 应用或外部协作依赖，类型为 {@code CancellationApplicationService}
     * @param afterSaleService 应用或外部协作依赖，类型为 {@code AfterSaleApplicationService}
     */
    public OmsInternalEventController(FulfillmentApplicationService service, CancellationApplicationService cancellationService, AfterSaleApplicationService afterSaleService) {
        this.service = service;
        this.cancellationService = cancellationService;
        this.afterSaleService = afterSaleService;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code EventRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     */
    @PostMapping("/events")
    @SuppressWarnings("PMD.SwitchStatementRule")
    public void consume(@RequestBody EventRequest event, Authentication authentication) {
        String expectedApp = switch(event.eventType()) {
            case "StockReserved", "StockReservationFailed", "StockReleased" ->
                "INVENTORY";
            case "WmsOutboundAccepted", "WmsOutboundShipped", "WmsOutboundCancelled" ->
                "WMS";
            case "RefundCompleted" ->
                "BMS";
            default ->
                "";
        };
        ScmAccessContexts.require(authentication).requireApplication(expectedApp);
        switch(event.eventType()) {
            case "StockReserved", "StockReservationFailed", "StockReleased", "WmsOutboundAccepted", "WmsOutboundShipped", "WmsOutboundCancelled" ->
                {
                    service.consumeEvent(new FulfillmentApplicationService.ExternalEvent(event.eventId(), event.eventType(), event.businessNo(), event.fulfillmentNo(), event.reservationRefNo(), event.reservationNo(), event.quantity(), event.outboundNo(), event.wmsOrderNo(), event.reason(), event.payload()));
                    if (WMS_OUTBOUND_CANCELLED.equals(event.eventType()) || STOCK_RELEASED.equals(event.eventType())) {
                        cancellationService.consumeEvent(new CancellationApplicationService.CancellationEvent(event.eventId() + ":cancellation", event.eventType(), event.businessNo(), event.outboundNo(), event.reservationRefNo(), event.payload()));
                    }
                }
            case "RefundCompleted" ->
                afterSaleService.consumeEvent(new AfterSaleApplicationService.RefundEvent(event.eventId(), event.eventType(), event.businessNo(), event.afterSaleNo(), event.quantity(), event.payload()));
            default ->
                throw new IllegalArgumentException("unsupported OMS event: " + event.eventType());
        }
    }

    /**
     * EventRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record EventRequest(String eventId, String eventType, String businessNo, String fulfillmentNo, String reservationRefNo, String reservationNo, java.math.BigDecimal quantity, String outboundNo, String wmsOrderNo, String afterSaleNo, String reason, String payload) {
    }

    /**
     * 业务常量 {@code STOCK_RELEASED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String STOCK_RELEASED = "StockReleased";

    /**
     * 业务常量 {@code WMS_OUTBOUND_CANCELLED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String WMS_OUTBOUND_CANCELLED = "WmsOutboundCancelled";
}
