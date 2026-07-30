package com.chaobo.scm.purchase.infrastructure.persistence.integration;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * PurchaseExternalFactMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface PurchaseExternalFactMapper {

    /**
     * 处理当前类型职责中的操作 {@code upsertQuote}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param quoteNo 可追踪业务编码，类型为 {@code String}
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param quoteQty 数量值，类型为 {@code BigDecimal}
     * @param quoteAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param quoteStatus 生命周期状态，类型为 {@code String}
     * @param payloadJson 业务处理参数或成员，类型为 {@code String}
     */
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
    void upsertQuote(@Param("eventCode") String eventCode, @Param("quoteNo") String quoteNo, @Param("rfqNo") String rfqNo, @Param("supplierId") long supplierId, @Param("skuCode") String skuCode, @Param("quoteQty") BigDecimal quoteQty, @Param("quoteAmount") BigDecimal quoteAmount, @Param("currency") String currency, @Param("quoteStatus") String quoteStatus, @Param("payloadJson") String payloadJson);

    /**
     * 处理当前类型职责中的操作 {@code upsertSupplierConfirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param confirmStatus 生命周期状态，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param sourceVersion 乐观锁或契约版本，类型为 {@code int}
     * @param occurredAt 业务时间，类型为 {@code OffsetDateTime}
     * @param payloadJson 业务处理参数或成员，类型为 {@code String}
     */
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
    void upsertSupplierConfirm(@Param("eventCode") String eventCode, @Param("orderNo") String orderNo, @Param("supplierId") long supplierId, @Param("confirmStatus") String confirmStatus, @Param("reason") String reason, @Param("sourceVersion") int sourceVersion, @Param("occurredAt") OffsetDateTime occurredAt, @Param("payloadJson") String payloadJson);

    /**
     * 处理当前类型职责中的操作 {@code upsertWmsInbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param inboundNo 可追踪业务编码，类型为 {@code String}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param asnNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param receivedQty 数量值，类型为 {@code BigDecimal}
     * @param qualifiedQty 数量值，类型为 {@code BigDecimal}
     * @param unqualifiedQty 数量值，类型为 {@code BigDecimal}
     * @param putawayQty 数量值，类型为 {@code BigDecimal}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param occurredAt 业务时间，类型为 {@code OffsetDateTime}
     * @param payloadJson 业务处理参数或成员，类型为 {@code String}
     */
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
    void upsertWmsInbound(@Param("eventCode") String eventCode, @Param("inboundNo") String inboundNo, @Param("orderNo") String orderNo, @Param("asnNo") String asnNo, @Param("warehouseCode") String warehouseCode, @Param("eventType") String eventType, @Param("receivedQty") BigDecimal receivedQty, @Param("qualifiedQty") BigDecimal qualifiedQty, @Param("unqualifiedQty") BigDecimal unqualifiedQty, @Param("putawayQty") BigDecimal putawayQty, @Param("reason") String reason, @Param("occurredAt") OffsetDateTime occurredAt, @Param("payloadJson") String payloadJson);

    /**
     * 处理当前类型职责中的操作 {@code upsertTransport}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param inboundNo 可追踪业务编码，类型为 {@code String}
     * @param asnNo 可追踪业务编码，类型为 {@code String}
     * @param shipmentId 业务或技术标识，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param transportStatus 生命周期状态，类型为 {@code String}
     * @param transportNode 应用或外部协作依赖，类型为 {@code String}
     * @param exceptionReason 业务处理参数或成员，类型为 {@code String}
     * @param occurredAt 业务时间，类型为 {@code OffsetDateTime}
     * @param payloadJson 业务处理参数或成员，类型为 {@code String}
     */
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
    void upsertTransport(@Param("eventCode") String eventCode, @Param("orderNo") String orderNo, @Param("inboundNo") String inboundNo, @Param("asnNo") String asnNo, @Param("shipmentId") String shipmentId, @Param("waybillNo") String waybillNo, @Param("carrierCode") String carrierCode, @Param("transportStatus") String transportStatus, @Param("transportNode") String transportNode, @Param("exceptionReason") String exceptionReason, @Param("occurredAt") OffsetDateTime occurredAt, @Param("payloadJson") String payloadJson);

    /**
     * 处理当前类型职责中的操作 {@code upsertBms}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param amount 金额或计费值，类型为 {@code BigDecimal}
     * @param sourceVersion 乐观锁或契约版本，类型为 {@code int}
     * @param payloadJson 业务处理参数或成员，类型为 {@code String}
     */
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
    void upsertBms(@Param("eventCode") String eventCode, @Param("orderNo") String orderNo, @Param("supplierId") long supplierId, @Param("eventType") String eventType, @Param("currency") String currency, @Param("amount") BigDecimal amount, @Param("sourceVersion") int sourceVersion, @Param("payloadJson") String payloadJson);
}
