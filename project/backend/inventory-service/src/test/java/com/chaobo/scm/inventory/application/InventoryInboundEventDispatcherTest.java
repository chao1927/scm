package com.chaobo.scm.inventory.application;

import com.chaobo.scm.inventory.domain.StockTransferAggregate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 标准外部事实到库存命令的映射测试。
 *
 * @author SCM Team
 */
class InventoryInboundEventDispatcherTest {

    @Test
    void mapsWmsPutawayPayloadToInboundAndOutbox() {
        CountingInventory inventory = new CountingInventory();
        List<String> events = new ArrayList<>();
        InventoryInboundEventDispatcher dispatcher = new InventoryInboundEventDispatcher(
                inventory,
                null,
                null,
                (type, aggregateType, aggregateId, payload) -> events.add(type),
                new InventoryEventEnvelopeCodec(new tools.jackson.databind.ObjectMapper()),
                (stockId, expiryDate, sourceEvent, factAt) -> { });
        InventoryEventEnvelope event = new InventoryEventEnvelope(
                "WMS-E-1",
                "InboundOrderPutawayCompleted",
                "1.0",
                "WMS",
                "WMS",
                "InboundOrder",
                "IN-1",
                1L,
                "IN-1",
                "WMS:IN-1:1",
                "2026-07-30T10:00:00+08:00",
                "TRACE-1",
                Map.of(
                        "ownerId", 88,
                        "warehouseId", 99,
                        "skuCode", "SKU-1",
                        "batchNo", "B-1",
                        "putawayQty", new BigDecimal("3.5"),
                        "sourceNo", "IN-1",
                        "attributes", Map.of("qualityStatus", "QUALIFIED")));

        dispatcher.process(event);

        assertThat(inventory.inboundCalls).isEqualTo(1);
        assertThat(inventory.lastCommand.qty()).isEqualByComparingTo("3.5");
        assertThat(inventory.lastCommand.sku()).isEqualTo("SKU-1");
        assertThat(events).containsExactly("InventoryChanged");
    }

    @Test
    void transferReceiptDoesNotIncreaseAvailableStockBeforePutaway() {
        CountingInventory inventory = new CountingInventory();
        CountingTransfers transfers = new CountingTransfers();
        InventoryInboundEventDispatcher dispatcher = new InventoryInboundEventDispatcher(
                inventory,
                null,
                transfers,
                (type, aggregateType, aggregateId, payload) -> { },
                new InventoryEventEnvelopeCodec(new tools.jackson.databind.ObjectMapper()),
                (stockId, expiryDate, sourceEvent, factAt) -> { });
        InventoryEventEnvelope received = new InventoryEventEnvelope(
                "WMS-TRF-RECEIVED-1",
                "TransferReceived",
                "1.0",
                "WMS",
                "WMS",
                "StockTransfer",
                "TRF-1",
                1L,
                "TRF-1",
                "WMS:TRF-1:1",
                "2026-07-30T10:00:00+08:00",
                "TRACE-1",
                Map.of("transferNo", "TRF-1", "qty", new BigDecimal("5"), "finalReceipt", true));

        dispatcher.process(received);

        assertThat(transfers.receiptValidations).isEqualTo(1);
        assertThat(transfers.receiveCalls).isZero();
        assertThat(inventory.inboundCalls).isZero();
    }

    @Test
    void transferPutawayFactRecordsReceiptAndAvailabilityOnce() {
        CountingTransfers transfers = new CountingTransfers();
        InventoryInboundEventDispatcher dispatcher = new InventoryInboundEventDispatcher(
                new CountingInventory(),
                null,
                transfers,
                (type, aggregateType, aggregateId, payload) -> { },
                new InventoryEventEnvelopeCodec(new tools.jackson.databind.ObjectMapper()),
                (stockId, expiryDate, sourceEvent, factAt) -> { });
        InventoryEventEnvelope putaway = new InventoryEventEnvelope(
                "WMS-TRF-PUTAWAY-1",
                "TransferPutawayCompleted",
                "1.0",
                "WMS",
                "WMS",
                "StockTransfer",
                "TRF-1",
                2L,
                "TRF-1",
                "WMS:TRF-1:2",
                "2026-07-30T10:05:00+08:00",
                "TRACE-1",
                Map.of("transferNo", "TRF-1", "qty", new BigDecimal("5"), "finalReceipt", true));

        dispatcher.process(putaway);

        assertThat(transfers.receiveCalls).isEqualTo(1);
    }

    private static final class CountingInventory extends InventoryApplicationService {

        private int inboundCalls;
        private AccountCommand lastCommand;

        private CountingInventory() {
            super(null);
        }

        @Override
        public AccountResult inbound(AccountCommand command) {
            inboundCalls++;
            lastCommand = command;
            return new AccountResult(
                    1L,
                    command.ownerId(),
                    command.warehouseId(),
                    command.sku(),
                    command.batchNo(),
                    command.qty(),
                    command.qty(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0);
        }
    }

    private static final class CountingTransfers extends StockTransferApplicationService {

        private int receiptValidations;
        private int receiveCalls;

        private CountingTransfers() {
            super(null, null, null);
        }

        @Override
        public TransferResult validateReceiptFact(String transferNo, BigDecimal qty) {
            receiptValidations++;
            return null;
        }

        @Override
        public TransferResult detail(String transferNo) {
            return new TransferResult(
                    transferNo, 1L, 10L, 20L, "SKU-1", null,
                    new BigDecimal("5"), new BigDecimal("5"), new BigDecimal("5"),
                    BigDecimal.ZERO, BigDecimal.ZERO, null, null, null,
                    StockTransferAggregate.IN_TRANSIT, 0, false);
        }

        @Override
        public TransferResult receive(
                String transferNo,
                BigDecimal qty,
                boolean finalReceipt,
                int version) {
            receiveCalls++;
            return null;
        }
    }
}
