package com.chaobo.scm.tms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TransportTaskMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface TransportTaskMapper {

    /**
     * 查询并返回 {@code findTask}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code TaskRow}
     */
    @Select("select task_no taskNo,source_system sourceSystem,source_order_no sourceOrderNo,source_line_no sourceLineNo,scenario,shipper_id shipperId,warehouse_id warehouseId,origin_address originAddress,destination_address destinationAddress,package_payload packagePayload,task_status status,carrier_code carrierCode,carrier_name carrierName,logistics_product_code logisticsProductCode,fee_responsibility feeResponsibility,version from tms_transport_task where task_no=#{taskNo}")
    TaskRow findTask(@Param("taskNo") String taskNo);

    /**
     * 查询并返回 {@code findActiveBySource}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param sourceOrderNo 可追踪业务编码，类型为 {@code String}
     * @param scenario 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code TaskRow}
     */
    @Select("select task_no taskNo,source_system sourceSystem,source_order_no sourceOrderNo,source_line_no sourceLineNo,scenario,shipper_id shipperId,warehouse_id warehouseId,origin_address originAddress,destination_address destinationAddress,package_payload packagePayload,task_status status,carrier_code carrierCode,carrier_name carrierName,logistics_product_code logisticsProductCode,fee_responsibility feeResponsibility,version from tms_transport_task where source_system=#{sourceSystem} and source_order_no=#{sourceOrderNo} and scenario=#{scenario} and task_status<>3 order by id desc limit 1")
    TaskRow findActiveBySource(@Param("sourceSystem") String sourceSystem, @Param("sourceOrderNo") String sourceOrderNo, @Param("scenario") String scenario);

    /**
     * 查询并返回 {@code listTasks}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param scenario 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param warehouseId 业务或技术标识，类型为 {@code Long}
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @return 查询并返回的结果，类型为 {@code List<TaskRow>}
     */
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
    List<TaskRow> listTasks(@Param("sourceSystem") String sourceSystem, @Param("scenario") String scenario, @Param("status") Integer status, @Param("warehouseId") Long warehouseId, @Param("carrierCode") String carrierCode, @Param("limit") int limit, @Param("offset") int offset);

    /**
     * 处理当前类型职责中的操作 {@code insertTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code TaskRow}
     */
    @Insert("insert into tms_transport_task(task_no,source_system,source_order_no,source_line_no,scenario,shipper_id,warehouse_id,origin_address,destination_address,package_payload,task_status,carrier_code,carrier_name,logistics_product_code,fee_responsibility,version,created_at,updated_at) values(#{taskNo},#{sourceSystem},#{sourceOrderNo},#{sourceLineNo},#{scenario},#{shipperId},#{warehouseId},#{originAddress},#{destinationAddress},#{packagePayload},#{status},#{carrierCode},#{carrierName},#{logisticsProductCode},#{feeResponsibility},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertTask(TaskRow row);

    /**
     * 执行命令 {@code updateTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code TaskRow}
     */
    @Update("update tms_transport_task set task_status=#{status},carrier_code=#{carrierCode},carrier_name=#{carrierName},logistics_product_code=#{logisticsProductCode},fee_responsibility=#{feeResponsibility},version=#{version},updated_at=now() where task_no=#{taskNo}")
    void updateTask(TaskRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertOutbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code OutboxRow}
     */
    @Insert("insert into tms_domain_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(OutboxRow row);

    /**
     * 查询并返回 {@code listOutbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<OutboxRow>}
     */
    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from tms_domain_event order by id desc")
    List<OutboxRow> listOutbox();

    /**
     * 处理当前类型职责中的操作 {@code insertOperationLog}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code OperationLogRow}
     */
    @Insert("insert into tms_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(OperationLogRow row);

    /**
     * 查询并返回 {@code listOperationLogs}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<OperationLogRow>}
     */
    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from tms_operation_log order by id desc")
    List<OperationLogRow> listOperationLogs();

    /**
     * TaskRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record TaskRow(Long id, String taskNo, String sourceSystem, String sourceOrderNo, String sourceLineNo, String scenario, Long shipperId, Long warehouseId, String originAddress, String destinationAddress, String packagePayload, int status, String carrierCode, String carrierName, String logisticsProductCode, String feeResponsibility, long version) {
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
