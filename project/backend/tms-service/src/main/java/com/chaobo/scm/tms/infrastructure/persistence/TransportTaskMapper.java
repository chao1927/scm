package com.chaobo.scm.tms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TransportTaskMapper {
    @Select("select task_no taskNo,source_system sourceSystem,source_order_no sourceOrderNo,source_line_no sourceLineNo,scenario,shipper_id shipperId,warehouse_id warehouseId,origin_address originAddress,destination_address destinationAddress,package_payload packagePayload,task_status status,carrier_code carrierCode,carrier_name carrierName,logistics_product_code logisticsProductCode,fee_responsibility feeResponsibility,version from tms_transport_task where task_no=#{taskNo}")
    TaskRow findTask(@Param("taskNo") String taskNo);

    @Select("select task_no taskNo,source_system sourceSystem,source_order_no sourceOrderNo,source_line_no sourceLineNo,scenario,shipper_id shipperId,warehouse_id warehouseId,origin_address originAddress,destination_address destinationAddress,package_payload packagePayload,task_status status,carrier_code carrierCode,carrier_name carrierName,logistics_product_code logisticsProductCode,fee_responsibility feeResponsibility,version from tms_transport_task where source_system=#{sourceSystem} and source_order_no=#{sourceOrderNo} and scenario=#{scenario} and task_status<>3 order by id desc limit 1")
    TaskRow findActiveBySource(@Param("sourceSystem") String sourceSystem,
                               @Param("sourceOrderNo") String sourceOrderNo,
                               @Param("scenario") String scenario);

    @Select("""
            select task_no taskNo,source_system sourceSystem,source_order_no sourceOrderNo,source_line_no sourceLineNo,scenario,shipper_id shipperId,warehouse_id warehouseId,origin_address originAddress,destination_address destinationAddress,package_payload packagePayload,task_status status,carrier_code carrierCode,carrier_name carrierName,logistics_product_code logisticsProductCode,fee_responsibility feeResponsibility,version
            from tms_transport_task
            where (#{sourceSystem} is null or source_system=#{sourceSystem})
              and (#{scenario} is null or scenario=#{scenario})
              and (#{status} is null or task_status=#{status})
              and (#{warehouseId} is null or warehouse_id=#{warehouseId})
              and (#{carrierCode} is null or carrier_code=#{carrierCode})
            order by id desc limit #{limit} offset #{offset}
            """)
    List<TaskRow> listTasks(@Param("sourceSystem") String sourceSystem,
                            @Param("scenario") String scenario,
                            @Param("status") Integer status,
                            @Param("warehouseId") Long warehouseId,
                            @Param("carrierCode") String carrierCode,
                            @Param("limit") int limit,
                            @Param("offset") int offset);

    @Insert("insert into tms_transport_task(task_no,source_system,source_order_no,source_line_no,scenario,shipper_id,warehouse_id,origin_address,destination_address,package_payload,task_status,carrier_code,carrier_name,logistics_product_code,fee_responsibility,version,created_at,updated_at) values(#{taskNo},#{sourceSystem},#{sourceOrderNo},#{sourceLineNo},#{scenario},#{shipperId},#{warehouseId},#{originAddress},#{destinationAddress},#{packagePayload},#{status},#{carrierCode},#{carrierName},#{logisticsProductCode},#{feeResponsibility},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertTask(TaskRow row);

    @Update("update tms_transport_task set task_status=#{status},carrier_code=#{carrierCode},carrier_name=#{carrierName},logistics_product_code=#{logisticsProductCode},fee_responsibility=#{feeResponsibility},version=#{version},updated_at=now() where task_no=#{taskNo}")
    void updateTask(TaskRow row);

    @Insert("insert into tms_domain_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(OutboxRow row);

    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from tms_domain_event order by id desc")
    List<OutboxRow> listOutbox();

    @Insert("insert into tms_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(OperationLogRow row);

    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from tms_operation_log order by id desc")
    List<OperationLogRow> listOperationLogs();

    record TaskRow(Long id, String taskNo, String sourceSystem, String sourceOrderNo, String sourceLineNo,
                   String scenario, Long shipperId, Long warehouseId, String originAddress,
                   String destinationAddress, String packagePayload, int status, String carrierCode,
                   String carrierName, String logisticsProductCode, String feeResponsibility, long version) {}

    record OutboxRow(String eventType, String businessNo, String payload, int status, LocalDateTime occurredAt) {}
    record OperationLogRow(String operationType, String businessNo, Long operatorId, String idempotencyKey,
                           LocalDateTime createdAt) {}
}
