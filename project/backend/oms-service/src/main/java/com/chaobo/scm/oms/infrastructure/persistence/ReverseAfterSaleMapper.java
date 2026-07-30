package com.chaobo.scm.oms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ReverseAfterSaleMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface ReverseAfterSaleMapper {

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("select after_sale_no afterSaleNo,after_sale_type type,sales_order_no salesOrderNo,fulfillment_no fulfillmentNo,owner_id ownerId,sku_code sku,apply_qty applyQty,refund_amount refundAmount,return_warehouse_id returnWarehouseId,reason,rma_no rmaNo,received_qty receivedQty,accepted_qty acceptedQty,refunded_amount refundedAmount,after_sale_status status,version from oms_reverse_after_sale where after_sale_no=#{no}")
    Row find(String no);

    /**
     * 查询并返回 {@code findActive}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("select after_sale_no afterSaleNo,after_sale_type type,sales_order_no salesOrderNo,fulfillment_no fulfillmentNo,owner_id ownerId,sku_code sku,apply_qty applyQty,refund_amount refundAmount,return_warehouse_id returnWarehouseId,reason,rma_no rmaNo,received_qty receivedQty,accepted_qty acceptedQty,refunded_amount refundedAmount,after_sale_status status,version from oms_reverse_after_sale where sales_order_no=#{orderNo} and sku_code=#{sku} and after_sale_status not in (8,9) order by id desc limit 1")
    Row findActive(@Param("orderNo") String orderNo, @Param("sku") String sku);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code Row}
     */
    @Insert("insert into oms_reverse_after_sale(after_sale_no,after_sale_type,sales_order_no,fulfillment_no,owner_id,sku_code,apply_qty,refund_amount,return_warehouse_id,reason,rma_no,received_qty,accepted_qty,refunded_amount,after_sale_status,version,created_at,updated_at) values(#{afterSaleNo},#{type},#{salesOrderNo},#{fulfillmentNo},#{ownerId},#{sku},#{applyQty},#{refundAmount},#{returnWarehouseId},#{reason},#{rmaNo},#{receivedQty},#{acceptedQty},#{refundedAmount},#{status},#{version},now(),now())")
    void insert(Row row);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code Row}
     * @param oldVersion 乐观锁或契约版本，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("update oms_reverse_after_sale set rma_no=#{row.rmaNo},received_qty=#{row.receivedQty},accepted_qty=#{row.acceptedQty},refunded_amount=#{row.refundedAmount},after_sale_status=#{row.status},version=#{row.version},updated_at=now() where after_sale_no=#{row.afterSaleNo} and version=#{oldVersion}")
    int update(@Param("row") Row row, @Param("oldVersion") long oldVersion);

    /**
     * 处理当前类型职责中的操作 {@code insertCommand}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param target 业务处理参数或成员，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     */
    @Insert("insert into oms_integration_command(command_type,target_system,business_no,idempotency_key,payload,command_status,retry_count,created_at,updated_at) values(#{type},#{target},#{businessNo},#{key},#{payload},1,0,now(),now())")
    void insertCommand(@Param("type") String type, @Param("target") String target, @Param("businessNo") String businessNo, @Param("key") String key, @Param("payload") String payload);

    /**
     * 处理当前类型职责中的操作 {@code insertOutbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     * @param occurredAt 业务时间，类型为 {@code LocalDateTime}
     */
    @Insert("insert into oms_outbox_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{type},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(@Param("type") String type, @Param("businessNo") String businessNo, @Param("payload") String payload, @Param("occurredAt") LocalDateTime occurredAt);

    /**
     * 处理当前类型职责中的操作 {@code claimEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param eventId 业务或技术标识，类型为 {@code String}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Insert("insert ignore into oms_event_inbox(event_id,event_type,business_no,payload,event_status,created_at) values(#{eventId},#{eventType},#{businessNo},#{payload},1,now())")
    int claimEvent(@Param("eventId") String eventId, @Param("eventType") String eventType, @Param("businessNo") String businessNo, @Param("payload") String payload);

    /**
     * 处理当前类型职责中的操作 {@code finishEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param eventId 业务或技术标识，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param error 业务处理参数或成员，类型为 {@code String}
     */
    @Update("update oms_event_inbox set event_status=#{status},fail_reason=#{error},processed_at=now() where event_id=#{eventId}")
    void finishEvent(@Param("eventId") String eventId, @Param("status") int status, @Param("error") String error);

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(String afterSaleNo, String type, String salesOrderNo, String fulfillmentNo, long ownerId, String sku, BigDecimal applyQty, BigDecimal refundAmount, long returnWarehouseId, String reason, String rmaNo, BigDecimal receivedQty, BigDecimal acceptedQty, BigDecimal refundedAmount, int status, long version) {
    }
}
