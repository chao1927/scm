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

@Mapper
public interface OmsMapper {
    @Select("select order_no orderNo,channel_code channelCode,channel_order_no channelOrderNo,customer_id customerId,receiver_address receiverAddress,line_payload linePayload,total_amount totalAmount,order_status status,review_remark reviewRemark,version from oms_sales_order where order_no=#{orderNo}")
    SalesOrderRow findOrder(@Param("orderNo") String orderNo);

    @Select("select order_no orderNo,channel_code channelCode,channel_order_no channelOrderNo,customer_id customerId,receiver_address receiverAddress,line_payload linePayload,total_amount totalAmount,order_status status,review_remark reviewRemark,version from oms_sales_order where channel_code=#{channelCode} and channel_order_no=#{channelOrderNo}")
    SalesOrderRow findByChannelOrder(@Param("channelCode") String channelCode, @Param("channelOrderNo") String channelOrderNo);

    @Select("select order_no orderNo,channel_code channelCode,channel_order_no channelOrderNo,customer_id customerId,receiver_address receiverAddress,line_payload linePayload,total_amount totalAmount,order_status status,review_remark reviewRemark,version from oms_sales_order order by id desc")
    List<SalesOrderRow> listOrders();

    @Insert("insert into oms_sales_order(order_no,channel_code,channel_order_no,customer_id,receiver_address,line_payload,total_amount,order_status,review_remark,version,created_at,updated_at) values(#{orderNo},#{channelCode},#{channelOrderNo},#{customerId},#{receiverAddress},#{linePayload},#{totalAmount},#{status},#{reviewRemark},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertOrder(SalesOrderRow row);

    @Update("update oms_sales_order set order_status=#{status},review_remark=#{reviewRemark},version=#{version},updated_at=now() where order_no=#{orderNo}")
    void updateOrder(SalesOrderRow row);

    @Select("select channel_code channelCode,channel_order_no channelOrderNo,order_no orderNo,raw_payload rawPayload,created_at createdAt from oms_channel_order order by id desc")
    List<ChannelOrderRow> listChannelOrders();

    @Insert("insert into oms_channel_order(channel_code,channel_order_no,order_no,raw_payload,created_at) values(#{channelCode},#{channelOrderNo},#{orderNo},#{rawPayload},now())")
    void insertChannelOrder(ChannelOrderRow row);

    @Insert("insert into oms_outbox_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(OutboxRow row);

    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from oms_outbox_event order by id desc")
    List<OutboxRow> listOutbox();

    @Insert("insert into oms_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(OperationLogRow row);

    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from oms_operation_log order by id desc")
    List<OperationLogRow> listOperationLogs();

    record SalesOrderRow(Long id, String orderNo, String channelCode, String channelOrderNo, Long customerId,
                         String receiverAddress, String linePayload, BigDecimal totalAmount, int status,
                         String reviewRemark, long version) {}
    record ChannelOrderRow(String channelCode, String channelOrderNo, String orderNo, String rawPayload, LocalDateTime createdAt) {}
    record OutboxRow(String eventType, String businessNo, String payload, int status, LocalDateTime occurredAt) {}
    record OperationLogRow(String operationType, String businessNo, Long operatorId, String idempotencyKey, LocalDateTime createdAt) {}
}
