package com.chaobo.scm.oms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * FulfillmentMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface FulfillmentMapper {

    /**
     * 查询并返回 {@code findFulfillment}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code FulfillmentRow}
     */
    @Select("select fulfillment_no fulfillmentNo,sales_order_no salesOrderNo,channel_code channelCode,customer_id customerId,warehouse_id warehouseId,warehouse_code warehouseCode,logistics_product_code logisticsProductCode,line_payload linePayload,fulfillment_status status,reservation_ref_no reservationRefNo,reservation_no reservationNo,outbound_order_no outboundNo,failure_reason failureReason,split_reason splitReason,version from oms_fulfillment where fulfillment_no=#{fulfillmentNo}")
    FulfillmentRow findFulfillment(@Param("fulfillmentNo") String fulfillmentNo);

    /**
     * 查询并返回 {@code findBySalesOrder}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code FulfillmentRow}
     */
    @Select("select fulfillment_no fulfillmentNo,sales_order_no salesOrderNo,channel_code channelCode,customer_id customerId,warehouse_id warehouseId,warehouse_code warehouseCode,logistics_product_code logisticsProductCode,line_payload linePayload,fulfillment_status status,reservation_ref_no reservationRefNo,reservation_no reservationNo,outbound_order_no outboundNo,failure_reason failureReason,split_reason splitReason,version from oms_fulfillment where sales_order_no=#{salesOrderNo} order by id limit 1")
    FulfillmentRow findBySalesOrder(@Param("salesOrderNo") String salesOrderNo);

    /**
     * 查询并返回 {@code listFulfillments}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<FulfillmentRow>}
     */
    @Select("select fulfillment_no fulfillmentNo,sales_order_no salesOrderNo,channel_code channelCode,customer_id customerId,warehouse_id warehouseId,warehouse_code warehouseCode,logistics_product_code logisticsProductCode,line_payload linePayload,fulfillment_status status,reservation_ref_no reservationRefNo,reservation_no reservationNo,outbound_order_no outboundNo,failure_reason failureReason,split_reason splitReason,version from oms_fulfillment order by id desc")
    List<FulfillmentRow> listFulfillments();

    /**
     * 处理当前类型职责中的操作 {@code insertFulfillment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code FulfillmentRow}
     */
    @Insert("insert into oms_fulfillment(fulfillment_no,sales_order_no,channel_code,customer_id,warehouse_id,warehouse_code,logistics_product_code,line_payload,fulfillment_status,reservation_ref_no,reservation_no,outbound_order_no,failure_reason,split_reason,version,created_at,updated_at) values(#{fulfillmentNo},#{salesOrderNo},#{channelCode},#{customerId},#{warehouseId},#{warehouseCode},#{logisticsProductCode},#{linePayload},#{status},#{reservationRefNo},#{reservationNo},#{outboundNo},#{failureReason},#{splitReason},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertFulfillment(FulfillmentRow row);

    /**
     * 执行命令 {@code updateFulfillment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code FulfillmentRow}
     */
    @Update("update oms_fulfillment set warehouse_id=#{warehouseId},warehouse_code=#{warehouseCode},line_payload=#{linePayload},fulfillment_status=#{status},reservation_ref_no=#{reservationRefNo},reservation_no=#{reservationNo},outbound_order_no=#{outboundNo},failure_reason=#{failureReason},split_reason=#{splitReason},version=#{version},updated_at=now() where fulfillment_no=#{fulfillmentNo}")
    void updateFulfillment(FulfillmentRow row);

    /**
     * 查询并返回 {@code findReservation}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param reservationRefNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ReservationRow}
     */
    @Select("select reservation_ref_no reservationRefNo,fulfillment_no fulfillmentNo,reservation_no reservationNo,reserve_qty reserveQty,reserved_qty reservedQty,reservation_status status,fail_reason failReason,version from oms_stock_reservation where reservation_ref_no=#{reservationRefNo}")
    ReservationRow findReservation(@Param("reservationRefNo") String reservationRefNo);

    /**
     * 查询并返回 {@code findReservationByFulfillment}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ReservationRow}
     */
    @Select("select reservation_ref_no reservationRefNo,fulfillment_no fulfillmentNo,reservation_no reservationNo,reserve_qty reserveQty,reserved_qty reservedQty,reservation_status status,fail_reason failReason,version from oms_stock_reservation where fulfillment_no=#{fulfillmentNo} order by id desc limit 1")
    ReservationRow findReservationByFulfillment(@Param("fulfillmentNo") String fulfillmentNo);

    /**
     * 处理当前类型职责中的操作 {@code insertReservation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ReservationRow}
     */
    @Insert("insert into oms_stock_reservation(reservation_ref_no,fulfillment_no,reservation_no,reserve_qty,reserved_qty,reservation_status,fail_reason,version,created_at,updated_at) values(#{reservationRefNo},#{fulfillmentNo},#{reservationNo},#{reserveQty},#{reservedQty},#{status},#{failReason},#{version},now(),now())")
    void insertReservation(ReservationRow row);

    /**
     * 执行命令 {@code updateReservation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ReservationRow}
     */
    @Update("update oms_stock_reservation set reservation_no=#{reservationNo},reserved_qty=#{reservedQty},reservation_status=#{status},fail_reason=#{failReason},version=#{version},updated_at=now() where reservation_ref_no=#{reservationRefNo}")
    void updateReservation(ReservationRow row);

    /**
     * 查询并返回 {@code findOutbound}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code OutboundRow}
     */
    @Select("select outbound_order_no outboundNo,fulfillment_no fulfillmentNo,sales_order_no salesOrderNo,warehouse_id warehouseId,warehouse_code warehouseCode,wms_order_no wmsOrderNo,outbound_status status,cancel_reason cancelReason,retry_count retryCount,version from oms_outbound where outbound_order_no=#{outboundNo}")
    OutboundRow findOutbound(@Param("outboundNo") String outboundNo);

    /**
     * 查询并返回 {@code findOutboundByFulfillment}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code OutboundRow}
     */
    @Select("select outbound_order_no outboundNo,fulfillment_no fulfillmentNo,sales_order_no salesOrderNo,warehouse_id warehouseId,warehouse_code warehouseCode,wms_order_no wmsOrderNo,outbound_status status,cancel_reason cancelReason,retry_count retryCount,version from oms_outbound where fulfillment_no=#{fulfillmentNo}")
    OutboundRow findOutboundByFulfillment(@Param("fulfillmentNo") String fulfillmentNo);

    /**
     * 查询并返回 {@code listOutbounds}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<OutboundRow>}
     */
    @Select("select outbound_order_no outboundNo,fulfillment_no fulfillmentNo,sales_order_no salesOrderNo,warehouse_id warehouseId,warehouse_code warehouseCode,wms_order_no wmsOrderNo,outbound_status status,cancel_reason cancelReason,retry_count retryCount,version from oms_outbound order by id desc")
    List<OutboundRow> listOutbounds();

    /**
     * 处理当前类型职责中的操作 {@code insertOutbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code OutboundRow}
     */
    @Insert("insert into oms_outbound(outbound_order_no,fulfillment_no,sales_order_no,warehouse_id,warehouse_code,wms_order_no,outbound_status,cancel_reason,retry_count,version,created_at,updated_at) values(#{outboundNo},#{fulfillmentNo},#{salesOrderNo},#{warehouseId},#{warehouseCode},#{wmsOrderNo},#{status},#{cancelReason},#{retryCount},#{version},now(),now())")
    void insertOutbound(OutboundRow row);

    /**
     * 执行命令 {@code updateOutbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code OutboundRow}
     */
    @Update("update oms_outbound set wms_order_no=#{wmsOrderNo},outbound_status=#{status},cancel_reason=#{cancelReason},retry_count=#{retryCount},version=#{version},updated_at=now() where outbound_order_no=#{outboundNo}")
    void updateOutbound(OutboundRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertIntegrationCommand}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code IntegrationCommandRow}
     */
    @Insert("insert into oms_integration_command(command_type,target_system,business_no,idempotency_key,payload,command_status,retry_count,created_at,updated_at) values(#{commandType},#{targetSystem},#{businessNo},#{idempotencyKey},#{payload},1,0,now(),now())")
    void insertIntegrationCommand(IntegrationCommandRow row);

    /**
     * 处理当前类型职责中的操作 {@code claimEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Insert("insert ignore into oms_event_inbox(event_id,event_type,business_no,payload,event_status,created_at) values(#{eventId},#{eventType},#{businessNo},#{payload},1,now())")
    int claimEvent(EventInboxRow row);

    /**
     * 执行命令 {@code updateEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
     */
    @Update("update oms_event_inbox set event_status=#{status},fail_reason=#{failReason},processed_at=now() where event_id=#{eventId}")
    void updateEvent(EventInboxRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertOutbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code OutboxRow}
     */
    @Insert("insert into oms_outbox_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(OutboxRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertOperationLog}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code OperationLogRow}
     */
    @Insert("insert into oms_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(OperationLogRow row);

    /**
     * FulfillmentRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record FulfillmentRow(String fulfillmentNo, String salesOrderNo, String channelCode, Long customerId, Long warehouseId, String warehouseCode, String logisticsProductCode, String linePayload, int status, String reservationRefNo, String reservationNo, String outboundNo, String failureReason, String splitReason, long version) {
    }

    /**
     * ReservationRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ReservationRow(String reservationRefNo, String fulfillmentNo, String reservationNo, BigDecimal reserveQty, BigDecimal reservedQty, int status, String failReason, long version) {
    }

    /**
     * OutboundRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record OutboundRow(String outboundNo, String fulfillmentNo, String salesOrderNo, Long warehouseId, String warehouseCode, String wmsOrderNo, int status, String cancelReason, int retryCount, long version) {
    }

    /**
     * IntegrationCommandRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record IntegrationCommandRow(String commandType, String targetSystem, String businessNo, String idempotencyKey, String payload) {
    }

    /**
     * EventInboxRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record EventInboxRow(String eventId, String eventType, String businessNo, String payload, int status, String failReason) {
    }

    /**
     * OutboxRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record OutboxRow(String eventType, String businessNo, String payload, LocalDateTime occurredAt) {
    }

    /**
     * OperationLogRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record OperationLogRow(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
    }
}
