package com.chaobo.scm.purchase.infrastructure.persistence.integration;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Mapper
public interface PurchaseExternalFactMapper {
    @Insert("""
            insert into purchase_supplier_quote_fact(
              event_code, quote_no, rfq_no, supplier_id, sku_code, quote_qty, quote_amount,
              currency, quote_status, payload_json, created_at, updated_at
            ) values (
              #{eventCode}, #{quoteNo}, #{rfqNo}, #{supplierId}, #{skuCode}, #{quoteQty}, #{quoteAmount},
              #{currency}, #{quoteStatus}, #{payloadJson}, now(3), now(3)
            ) on duplicate key update quote_qty = values(quote_qty), quote_amount = values(quote_amount),
              currency = values(currency), quote_status = values(quote_status), payload_json = values(payload_json),
              updated_at = now(3)
            """)
    void upsertQuote(@Param("eventCode") String eventCode, @Param("quoteNo") String quoteNo,
                     @Param("rfqNo") String rfqNo, @Param("supplierId") long supplierId,
                     @Param("skuCode") String skuCode, @Param("quoteQty") BigDecimal quoteQty,
                     @Param("quoteAmount") BigDecimal quoteAmount, @Param("currency") String currency,
                     @Param("quoteStatus") String quoteStatus, @Param("payloadJson") String payloadJson);

    @Insert("""
            insert into purchase_supplier_confirm_fact(
              event_code, order_no, supplier_id, confirm_status, reason, source_version,
              occurred_at, payload_json, created_at, updated_at
            ) values (
              #{eventCode}, #{orderNo}, #{supplierId}, #{confirmStatus}, #{reason}, #{sourceVersion},
              #{occurredAt}, #{payloadJson}, now(3), now(3)
            ) on duplicate key update confirm_status = values(confirm_status), reason = values(reason),
              source_version = values(source_version), occurred_at = values(occurred_at),
              payload_json = values(payload_json), updated_at = now(3)
            """)
    void upsertSupplierConfirm(@Param("eventCode") String eventCode, @Param("orderNo") String orderNo,
                               @Param("supplierId") long supplierId, @Param("confirmStatus") String confirmStatus,
                               @Param("reason") String reason, @Param("sourceVersion") int sourceVersion,
                               @Param("occurredAt") OffsetDateTime occurredAt,
                               @Param("payloadJson") String payloadJson);

    @Insert("""
            insert into purchase_wms_inbound_fact(
              event_code, inbound_no, order_no, asn_no, warehouse_code, event_type,
              received_qty, qualified_qty, unqualified_qty, putaway_qty, reason, occurred_at,
              payload_json, created_at, updated_at
            ) values (
              #{eventCode}, #{inboundNo}, #{orderNo}, #{asnNo}, #{warehouseCode}, #{eventType},
              #{receivedQty}, #{qualifiedQty}, #{unqualifiedQty}, #{putawayQty}, #{reason}, #{occurredAt},
              #{payloadJson}, now(3), now(3)
            ) on duplicate key update event_type = values(event_type), received_qty = values(received_qty),
              qualified_qty = values(qualified_qty), unqualified_qty = values(unqualified_qty),
              putaway_qty = values(putaway_qty), reason = values(reason), occurred_at = values(occurred_at),
              payload_json = values(payload_json), updated_at = now(3)
            """)
    void upsertWmsInbound(@Param("eventCode") String eventCode, @Param("inboundNo") String inboundNo,
                          @Param("orderNo") String orderNo, @Param("asnNo") String asnNo,
                          @Param("warehouseCode") String warehouseCode, @Param("eventType") String eventType,
                          @Param("receivedQty") BigDecimal receivedQty,
                          @Param("qualifiedQty") BigDecimal qualifiedQty,
                          @Param("unqualifiedQty") BigDecimal unqualifiedQty,
                          @Param("putawayQty") BigDecimal putawayQty, @Param("reason") String reason,
                          @Param("occurredAt") OffsetDateTime occurredAt, @Param("payloadJson") String payloadJson);

    @Insert("""
            insert into purchase_transport_fact(
              event_code, order_no, inbound_no, asn_no, shipment_id, waybill_no, carrier_code,
              transport_status, transport_node, exception_reason, occurred_at, payload_json, created_at, updated_at
            ) values (
              #{eventCode}, #{orderNo}, #{inboundNo}, #{asnNo}, #{shipmentId}, #{waybillNo}, #{carrierCode},
              #{transportStatus}, #{transportNode}, #{exceptionReason}, #{occurredAt}, #{payloadJson}, now(3), now(3)
            ) on duplicate key update shipment_id = values(shipment_id), waybill_no = values(waybill_no),
              carrier_code = values(carrier_code), transport_status = values(transport_status),
              transport_node = values(transport_node), exception_reason = values(exception_reason),
              occurred_at = values(occurred_at), payload_json = values(payload_json), updated_at = now(3)
            """)
    void upsertTransport(@Param("eventCode") String eventCode, @Param("orderNo") String orderNo,
                         @Param("inboundNo") String inboundNo, @Param("asnNo") String asnNo,
                         @Param("shipmentId") String shipmentId, @Param("waybillNo") String waybillNo,
                         @Param("carrierCode") String carrierCode, @Param("transportStatus") String transportStatus,
                         @Param("transportNode") String transportNode, @Param("exceptionReason") String exceptionReason,
                         @Param("occurredAt") OffsetDateTime occurredAt, @Param("payloadJson") String payloadJson);

    @Insert("""
            insert into purchase_bms_fact(
              event_code, order_no, supplier_id, event_type, currency, amount, source_version,
              payload_json, created_at, updated_at
            ) values (
              #{eventCode}, #{orderNo}, #{supplierId}, #{eventType}, #{currency}, #{amount}, #{sourceVersion},
              #{payloadJson}, now(3), now(3)
            ) on duplicate key update event_type = values(event_type), currency = values(currency),
              amount = values(amount), source_version = values(source_version),
              payload_json = values(payload_json), updated_at = now(3)
            """)
    void upsertBms(@Param("eventCode") String eventCode, @Param("orderNo") String orderNo,
                   @Param("supplierId") long supplierId, @Param("eventType") String eventType,
                   @Param("currency") String currency, @Param("amount") BigDecimal amount,
                   @Param("sourceVersion") int sourceVersion, @Param("payloadJson") String payloadJson);
}
