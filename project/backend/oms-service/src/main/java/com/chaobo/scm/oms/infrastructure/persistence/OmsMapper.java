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
 * OmsMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface OmsMapper {

    /**
     * 查询并返回 {@code findOrder}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code SalesOrderRow}
     */
    @Select("select id,organization_id organizationId,owner_id ownerId,order_no orderNo,channel_code channelCode,channel_order_no channelOrderNo,customer_id customerId,receiver_address receiverAddress,line_payload linePayload,total_amount totalAmount,order_status status,review_remark reviewRemark,version from oms_sales_order where order_no=#{orderNo}")
    SalesOrderRow findOrder(@Param("orderNo") String orderNo);

    /**
     * 查询并返回 {@code findByChannelOrder}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param channelCode 可追踪业务编码，类型为 {@code String}
     * @param channelOrderNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code SalesOrderRow}
     */
    @Select("select id,organization_id organizationId,owner_id ownerId,order_no orderNo,channel_code channelCode,channel_order_no channelOrderNo,customer_id customerId,receiver_address receiverAddress,line_payload linePayload,total_amount totalAmount,order_status status,review_remark reviewRemark,version from oms_sales_order where channel_code=#{channelCode} and channel_order_no=#{channelOrderNo}")
    SalesOrderRow findByChannelOrder(@Param("channelCode") String channelCode, @Param("channelOrderNo") String channelOrderNo);

    /**
     * 查询并返回 {@code listOrders}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<SalesOrderRow>}
     */
    @Select("select id,organization_id organizationId,owner_id ownerId,order_no orderNo,channel_code channelCode,channel_order_no channelOrderNo,customer_id customerId,receiver_address receiverAddress,line_payload linePayload,total_amount totalAmount,order_status status,review_remark reviewRemark,version from oms_sales_order order by id desc")
    List<SalesOrderRow> listOrders();

    /**
     * 处理当前类型职责中的操作 {@code insertOrder}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code SalesOrderRow}
     */
    @Insert("insert into oms_sales_order(organization_id,owner_id,order_no,channel_code,channel_order_no,customer_id,receiver_address,line_payload,total_amount,order_status,review_remark,version,created_at,updated_at) values(#{organizationId},#{ownerId},#{orderNo},#{channelCode},#{channelOrderNo},#{customerId},#{receiverAddress},#{linePayload},#{totalAmount},#{status},#{reviewRemark},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertOrder(SalesOrderRow row);

    /**
     * 执行命令 {@code updateOrder}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code SalesOrderRow}
     */
    @Update("update oms_sales_order set order_status=#{status},review_remark=#{reviewRemark},version=#{version},updated_at=now() where order_no=#{orderNo}")
    void updateOrder(SalesOrderRow row);

    /**
     * 查询并返回 {@code listChannelOrders}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<ChannelOrderRow>}
     */
    @Select("select channel_code channelCode,channel_order_no channelOrderNo,order_no orderNo,raw_payload rawPayload,created_at createdAt from oms_channel_order order by id desc")
    List<ChannelOrderRow> listChannelOrders();

    /**
     * 处理当前类型职责中的操作 {@code insertChannelOrder}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ChannelOrderRow}
     */
    @Insert("insert into oms_channel_order(channel_code,channel_order_no,order_no,raw_payload,created_at) values(#{channelCode},#{channelOrderNo},#{orderNo},#{rawPayload},now())")
    void insertChannelOrder(ChannelOrderRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertOutbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code OutboxRow}
     */
    @Insert("insert into oms_outbox_event(event_code,event_type,business_no,payload,event_status,occurred_at,created_at) values(concat('OMS-',replace(uuid(),'-','')),#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(OutboxRow row);

    /**
     * 查询并返回 {@code listOutbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<OutboxRow>}
     */
    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from oms_outbox_event order by id desc")
    List<OutboxRow> listOutbox();

    /**
     * 处理当前类型职责中的操作 {@code insertOperationLog}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code OperationLogRow}
     */
    @Insert("insert into oms_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(OperationLogRow row);

    /**
     * 查询并返回 {@code listOperationLogs}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<OperationLogRow>}
     */
    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from oms_operation_log order by id desc")
    List<OperationLogRow> listOperationLogs();

    /**
     * SalesOrderRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record SalesOrderRow(Long id, Long organizationId, Long ownerId, String orderNo,
                         String channelCode, String channelOrderNo, Long customerId,
                         String receiverAddress, String linePayload, BigDecimal totalAmount,
                         int status, String reviewRemark, long version) {

        public SalesOrderRow(Long id, String orderNo, String channelCode,
                             String channelOrderNo, Long customerId,
                             String receiverAddress, String linePayload,
                             BigDecimal totalAmount, int status,
                             String reviewRemark, long version) {
            this(id, null, null, orderNo, channelCode, channelOrderNo, customerId,
                    receiverAddress, linePayload, totalAmount, status, reviewRemark, version);
        }
    }

    /**
     * ChannelOrderRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ChannelOrderRow(String channelCode, String channelOrderNo, String orderNo, String rawPayload, LocalDateTime createdAt) {
    }

    /**
     * OutboxRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record OutboxRow(String eventType, String businessNo, String payload, int status, LocalDateTime occurredAt) {
    }

    /**
     * OperationLogRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record OperationLogRow(String operationType, String businessNo, Long operatorId, String idempotencyKey, LocalDateTime createdAt) {
    }
}
