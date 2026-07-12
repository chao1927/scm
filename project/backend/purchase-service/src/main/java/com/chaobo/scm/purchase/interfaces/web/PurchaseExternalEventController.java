package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.integration.PurchaseExternalEvent;
import com.chaobo.scm.purchase.application.integration.PurchaseExternalEventConsumerApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/internal/purchase/v1/events")
public class PurchaseExternalEventController {
    private final PurchaseExternalEventConsumerApplicationService service;

    public PurchaseExternalEventController(PurchaseExternalEventConsumerApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<Void> consume(@Valid @RequestBody Request body, HttpServletRequest request) {
        var sourceSystem = request.getHeader("X-Source-System");
        var eventCode = request.getHeader("X-Event-Code");
        if (sourceSystem == null || sourceSystem.isBlank() || eventCode == null || eventCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "缺少外部事件请求头");
        }
        service.consume(body.toEvent(sourceSystem, eventCode));
        return ApiResponse.success(null, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record Request(@NotBlank String eventType, String businessNo, String orderNo, String rfqNo,
                          String quoteNo, String inboundNo, String asnNo, Long supplierId, Long purchaseOrgId,
                          String warehouseCode, String skuCode, BigDecimal quantity, BigDecimal receivedQty,
                          BigDecimal qualifiedQty, BigDecimal unqualifiedQty, BigDecimal putawayQty,
                          BigDecimal amount, String currency, String shipmentId, String waybillNo,
                          String carrierCode, String transportNode, String status, String reason,
                          Integer sourceVersion, OffsetDateTime occurredAt, Map<String, Object> payload) {
        PurchaseExternalEvent toEvent(String sourceSystem, String eventCode) {
            return new PurchaseExternalEvent(sourceSystem, eventCode, eventType, businessNo, orderNo, rfqNo,
                    quoteNo, inboundNo, asnNo, supplierId, purchaseOrgId, warehouseCode, skuCode, quantity,
                    receivedQty, qualifiedQty, unqualifiedQty, putawayQty, amount, currency, shipmentId,
                    waybillNo, carrierCode, transportNode, status, reason, sourceVersion, occurredAt, payload);
        }
    }
}
