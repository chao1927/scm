package com.chaobo.scm.oms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CancellationMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface CancellationMapper {

    /**
     * 查询并返回 {@code findCancel}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param cancellationNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code CancelRow}
     */
    @Select("select cancellation_no cancellationNo,sales_order_no salesOrderNo,fulfillment_no fulfillmentNo,outbound_no outboundNo,reservation_ref_no reservationRefNo,reason,cancel_status status,wms_cancelled wmsCancelled,stock_released stockReleased,version from oms_cancel_request where cancellation_no=#{cancellationNo}")
    CancelRow findCancel(@Param("cancellationNo") String cancellationNo);

    /**
     * 查询并返回 {@code findCancelByFulfillment}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code CancelRow}
     */
    @Select("select cancellation_no cancellationNo,sales_order_no salesOrderNo,fulfillment_no fulfillmentNo,outbound_no outboundNo,reservation_ref_no reservationRefNo,reason,cancel_status status,wms_cancelled wmsCancelled,stock_released stockReleased,version from oms_cancel_request where fulfillment_no=#{fulfillmentNo} order by id desc limit 1")
    CancelRow findCancelByFulfillment(@Param("fulfillmentNo") String fulfillmentNo);

    /**
     * 查询并返回 {@code findCancelByOutbound}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code CancelRow}
     */
    @Select("select cancellation_no cancellationNo,sales_order_no salesOrderNo,fulfillment_no fulfillmentNo,outbound_no outboundNo,reservation_ref_no reservationRefNo,reason,cancel_status status,wms_cancelled wmsCancelled,stock_released stockReleased,version from oms_cancel_request where outbound_no=#{outboundNo} order by id desc limit 1")
    CancelRow findCancelByOutbound(@Param("outboundNo") String outboundNo);

    /**
     * 查询并返回 {@code findCancelByReservation}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param reservationRefNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code CancelRow}
     */
    @Select("select cancellation_no cancellationNo,sales_order_no salesOrderNo,fulfillment_no fulfillmentNo,outbound_no outboundNo,reservation_ref_no reservationRefNo,reason,cancel_status status,wms_cancelled wmsCancelled,stock_released stockReleased,version from oms_cancel_request where reservation_ref_no=#{reservationRefNo} order by id desc limit 1")
    CancelRow findCancelByReservation(@Param("reservationRefNo") String reservationRefNo);

    /**
     * 处理当前类型职责中的操作 {@code insertCancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code CancelRow}
     */
    @Insert("insert into oms_cancel_request(cancellation_no,sales_order_no,fulfillment_no,outbound_no,reservation_ref_no,reason,cancel_status,wms_cancelled,stock_released,version,created_at,updated_at) values(#{cancellationNo},#{salesOrderNo},#{fulfillmentNo},#{outboundNo},#{reservationRefNo},#{reason},#{status},#{wmsCancelled},#{stockReleased},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertCancel(CancelRow row);

    /**
     * 执行命令 {@code updateCancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code CancelRow}
     */
    @Update("update oms_cancel_request set cancel_status=#{status},wms_cancelled=#{wmsCancelled},stock_released=#{stockReleased},version=#{version},updated_at=now() where cancellation_no=#{cancellationNo}")
    void updateCancel(CancelRow row);

    /**
     * 查询并返回 {@code findAfterSale}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param afterSaleNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code AfterSaleRow}
     */
    @Select("select after_sale_no afterSaleNo,sales_order_no salesOrderNo,fulfillment_no fulfillmentNo,refund_amount refundAmount,refunded_amount refundedAmount,reason,after_sale_status status,version from oms_after_sale where after_sale_no=#{afterSaleNo}")
    AfterSaleRow findAfterSale(@Param("afterSaleNo") String afterSaleNo);

    /**
     * 查询并返回 {@code findAfterSaleByOrder}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code AfterSaleRow}
     */
    @Select("select after_sale_no afterSaleNo,sales_order_no salesOrderNo,fulfillment_no fulfillmentNo,refund_amount refundAmount,refunded_amount refundedAmount,reason,after_sale_status status,version from oms_after_sale where sales_order_no=#{salesOrderNo} order by id desc limit 1")
    AfterSaleRow findAfterSaleByOrder(@Param("salesOrderNo") String salesOrderNo);

    /**
     * 处理当前类型职责中的操作 {@code insertAfterSale}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code AfterSaleRow}
     */
    @Insert("insert into oms_after_sale(after_sale_no,sales_order_no,fulfillment_no,refund_amount,refunded_amount,reason,after_sale_status,version,created_at,updated_at) values(#{afterSaleNo},#{salesOrderNo},#{fulfillmentNo},#{refundAmount},#{refundedAmount},#{reason},#{status},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertAfterSale(AfterSaleRow row);

    /**
     * 执行命令 {@code updateAfterSale}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code AfterSaleRow}
     */
    @Update("update oms_after_sale set refunded_amount=#{refundedAmount},after_sale_status=#{status},version=#{version},updated_at=now() where after_sale_no=#{afterSaleNo}")
    void updateAfterSale(AfterSaleRow row);

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
    @Insert("insert into oms_outbox_event(event_code,event_type,business_no,payload,event_status,occurred_at,created_at) values(concat('OMS-',replace(uuid(),'-','')),#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
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
     * CancelRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record CancelRow(String cancellationNo, String salesOrderNo, String fulfillmentNo, String outboundNo, String reservationRefNo, String reason, int status, boolean wmsCancelled, boolean stockReleased, long version) {
    }

    /**
     * AfterSaleRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record AfterSaleRow(String afterSaleNo, String salesOrderNo, String fulfillmentNo, BigDecimal refundAmount, BigDecimal refundedAmount, String reason, int status, long version) {
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
