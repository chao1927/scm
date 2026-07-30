package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 外部业务事实到库存命令的分发器。
 *
 * <p>仅处理中央库存已声明的事实类型，并校验来源系统。未知事件失败关闭，不能静默吞掉可能改变库存的事实。
 *
 * @author SCM Team
 */
@Service
public class InventoryInboundEventDispatcher implements InventoryInboundEventProcessor {

    private static final String WMS = "WMS";
    private static final String TMS = "TMS";

    private final InventoryApplicationService inventory;
    private final ReturnDispositionApplicationService dispositions;
    private final StockTransferApplicationService transfers;
    private final InventoryEventPublisher events;
    private final InventoryEventEnvelopeCodec codec;
    private final InventoryBatchFactPort batchFacts;

    public InventoryInboundEventDispatcher(
            InventoryApplicationService inventory,
            ReturnDispositionApplicationService dispositions,
            StockTransferApplicationService transfers,
            InventoryEventPublisher events,
            InventoryEventEnvelopeCodec codec,
            InventoryBatchFactPort batchFacts) {
        this.inventory = inventory;
        this.dispositions = dispositions;
        this.transfers = transfers;
        this.events = events;
        this.codec = codec;
        this.batchFacts = batchFacts;
    }

    @Override
    @SuppressWarnings("PMD.SwitchStatementRule")
    public void process(InventoryEventEnvelope event) {
        switch (event.eventType()) {
            case "ReturnInspected" ->
                applyReturnDisposition(event);
            case "WmsPutawayCompleted", "InboundOrderPutawayCompleted" -> {
                requireSource(event, WMS);
                InventoryApplicationService.AccountResult result =
                        inventory.inbound(accountCommand(event, "putawayQty", "qty"));
                recordExpiryFact(event, result.id());
                changed(event);
            }
            case "WmsShipmentHandedOver", "OutboundOrderShipped" -> {
                requireSource(event, WMS);
                inventory.outbound(accountCommand(event, "shippedQty", "qty"));
                changed(event);
            }
            case "WmsStocktakeDifferenceConfirmed", "StocktakeDifferenceConfirmed" -> {
                requireSource(event, WMS);
                inventory.adjust(accountCommand(event, "diffQty", "qty"));
                changed(event);
            }
            case "TransferOutboundCompleted" -> {
                requireSource(event, WMS);
                transfers.recordOutbound(
                        requiredAny(event, "transferNo"),
                        decimalAny(event, "qty", "outboundQty"),
                        currentTransferVersion(event));
            }
            case "TransferInTransit" -> {
                requireSource(event, TMS);
                transfers.markInTransit(
                        requiredAny(event, "transferNo"),
                        currentTransferVersion(event));
            }
            case "TransferReceived" -> {
                requireSource(event, WMS);
                transfers.receive(
                        requiredAny(event, "transferNo"),
                        decimalAny(event, "qty", "receivedQty"),
                        booleanValue(event.payload(), "finalReceipt"),
                        currentTransferVersion(event));
            }
            default ->
                throw new BusinessException(
                        ErrorCode.VALIDATION_FAILED,
                        "不支持的库存入站事件: " + event.eventType());
        }
    }

    private void recordExpiryFact(InventoryEventEnvelope event, long stockId) {
        String expiryDate = event.optionalText("expiryDate");
        if (expiryDate == null || expiryDate.isBlank()) {
            return;
        }
        try {
            batchFacts.recordExpiry(
                    stockId,
                    LocalDate.parse(expiryDate),
                    event.eventId(),
                    OffsetDateTime.parse(event.occurredAt()).toLocalDateTime());
        } catch (java.time.format.DateTimeParseException exception) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "库存事件效期或发生时间格式错误");
        }
    }

    private void applyReturnDisposition(InventoryEventEnvelope event) {
        requireSource(event, WMS);
        dispositions.apply(new ReturnDispositionApplicationService.Command(
                event.eventId(),
                requiredAny(event, "afterSaleNo"),
                event.requiredLong("ownerId"),
                event.requiredLong("warehouseId"),
                requiredAny(event, "sku", "skuCode"),
                event.optionalText("batchNo"),
                event.requiredDecimal("receivedQty"),
                decimalOrZero(event, "sellableQty"),
                decimalOrZero(event, "defectiveQty"),
                decimalOrZero(event, "frozenQty"),
                decimalOrZero(event, "scrappedQty"),
                decimalOrZero(event, "unmatchedQty")));
        events.publish(
                "ReturnDispositionApplied",
                "RETURN_DISPOSITION",
                requiredAny(event, "afterSaleNo"),
                codec.encodePayload(event.payload()));
    }

    private InventoryApplicationService.AccountCommand accountCommand(
            InventoryEventEnvelope event,
            String primaryQuantity,
            String fallbackQuantity) {
        return new InventoryApplicationService.AccountCommand(
                event.requiredLong("ownerId"),
                event.requiredLong("warehouseId"),
                requiredAny(event, "sku", "skuCode"),
                event.optionalText("batchNo"),
                decimalAny(event, primaryQuantity, fallbackQuantity),
                event.sourceSystem(),
                sourceNo(event));
    }

    private void changed(InventoryEventEnvelope event) {
        events.publish(
                "InventoryChanged",
                "INVENTORY_ACCOUNT",
                sourceNo(event),
                codec.encodePayload(event.payload()));
    }

    private int currentTransferVersion(InventoryEventEnvelope event) {
        return transfers.detail(requiredAny(event, "transferNo")).version();
    }

    private static String sourceNo(InventoryEventEnvelope event) {
        String sourceNo = event.optionalText("sourceNo");
        return sourceNo == null || sourceNo.isBlank()
                ? event.businessKey()
                : sourceNo;
    }

    private static void requireSource(
            InventoryEventEnvelope event,
            String expected) {
        if (!expected.equalsIgnoreCase(event.sourceSystem())) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "库存事件来源不合法: " + event.sourceSystem());
        }
    }

    private static String requiredAny(
            InventoryEventEnvelope event,
            String... names) {
        for (String name : names) {
            String value = event.optionalText(name);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        throw new BusinessException(
                ErrorCode.VALIDATION_FAILED,
                "库存事件载荷缺少字段: " + String.join("/", names));
    }

    private static BigDecimal decimalAny(
            InventoryEventEnvelope event,
            String... names) {
        for (String name : names) {
            Object value = event.payload().get(name);
            if (value != null) {
                try {
                    return new BigDecimal(value.toString());
                } catch (NumberFormatException exception) {
                    throw new BusinessException(
                            ErrorCode.VALIDATION_FAILED,
                            "库存事件数量字段错误: " + name);
                }
            }
        }
        throw new BusinessException(
                ErrorCode.VALIDATION_FAILED,
                "库存事件载荷缺少数量: " + String.join("/", names));
    }

    private static BigDecimal decimalOrZero(
            InventoryEventEnvelope event,
            String name) {
        Object value = event.payload().get(name);
        return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
    }

    private static boolean booleanValue(
            Map<String, Object> payload,
            String name) {
        Object value = payload.get(name);
        return value instanceof Boolean bool
                ? bool
                : value != null && Boolean.parseBoolean(value.toString());
    }
}
