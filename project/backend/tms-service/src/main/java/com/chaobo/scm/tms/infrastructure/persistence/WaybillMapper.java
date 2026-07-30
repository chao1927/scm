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
 * WaybillMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface WaybillMapper {

    /**
     * 查询并返回 {@code findWaybill}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code WaybillRow}
     */
    @Select("select waybill_no waybillNo,task_no taskNo,carrier_code carrierCode,carrier_name carrierName,carrier_waybill_no carrierWaybillNo,logistics_product_code logisticsProductCode,receipt_payload receiptPayload,waybill_status status,void_reason voidReason,approval_no approvalNo,version from tms_waybill where waybill_no=#{waybillNo}")
    WaybillRow findWaybill(@Param("waybillNo") String waybillNo);

    /**
     * 查询并返回 {@code findActiveWaybillByTask}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code WaybillRow}
     */
    @Select("select waybill_no waybillNo,task_no taskNo,carrier_code carrierCode,carrier_name carrierName,carrier_waybill_no carrierWaybillNo,logistics_product_code logisticsProductCode,receipt_payload receiptPayload,waybill_status status,void_reason voidReason,approval_no approvalNo,version from tms_waybill where task_no=#{taskNo} and waybill_status<>2 order by id desc limit 1")
    WaybillRow findActiveWaybillByTask(@Param("taskNo") String taskNo);

    /**
     * 查询并返回 {@code listWaybills}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<WaybillRow>}
     */
    @Select("select waybill_no waybillNo,task_no taskNo,carrier_code carrierCode,carrier_name carrierName,carrier_waybill_no carrierWaybillNo,logistics_product_code logisticsProductCode,receipt_payload receiptPayload,waybill_status status,void_reason voidReason,approval_no approvalNo,version from tms_waybill order by id desc")
    List<WaybillRow> listWaybills();

    /**
     * 处理当前类型职责中的操作 {@code insertWaybill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code WaybillRow}
     */
    @Insert("insert into tms_waybill(waybill_no,task_no,carrier_code,carrier_name,carrier_waybill_no,logistics_product_code,receipt_payload,waybill_status,void_reason,approval_no,version,created_at,updated_at) values(#{waybillNo},#{taskNo},#{carrierCode},#{carrierName},#{carrierWaybillNo},#{logisticsProductCode},#{receiptPayload},#{status},#{voidReason},#{approvalNo},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertWaybill(WaybillRow row);

    /**
     * 执行命令 {@code updateWaybill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code WaybillRow}
     */
    @Update("update tms_waybill set waybill_status=#{status},void_reason=#{voidReason},approval_no=#{approvalNo},version=#{version},updated_at=now() where waybill_no=#{waybillNo}")
    void updateWaybill(WaybillRow row);

    /**
     * 查询并返回 {@code findLabel}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param labelNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code LabelRow}
     */
    @Select("select label_no labelNo,waybill_no waybillNo,package_no packageNo,template_version templateVersion,label_url labelUrl,label_status status,print_count printCount,last_print_device lastPrintDevice,void_reason voidReason,version from tms_shipping_label where label_no=#{labelNo}")
    LabelRow findLabel(@Param("labelNo") String labelNo);

    /**
     * 查询并返回 {@code findActiveLabel}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param packageNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code LabelRow}
     */
    @Select("select label_no labelNo,waybill_no waybillNo,package_no packageNo,template_version templateVersion,label_url labelUrl,label_status status,print_count printCount,last_print_device lastPrintDevice,void_reason voidReason,version from tms_shipping_label where waybill_no=#{waybillNo} and package_no=#{packageNo} and label_status<>3 order by id desc limit 1")
    LabelRow findActiveLabel(@Param("waybillNo") String waybillNo, @Param("packageNo") String packageNo);

    /**
     * 查询并返回 {@code listLabelsByWaybill}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code List<LabelRow>}
     */
    @Select("select label_no labelNo,waybill_no waybillNo,package_no packageNo,template_version templateVersion,label_url labelUrl,label_status status,print_count printCount,last_print_device lastPrintDevice,void_reason voidReason,version from tms_shipping_label where waybill_no=#{waybillNo} order by id desc")
    List<LabelRow> listLabelsByWaybill(@Param("waybillNo") String waybillNo);

    /**
     * 处理当前类型职责中的操作 {@code insertLabel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code LabelRow}
     */
    @Insert("insert into tms_shipping_label(label_no,waybill_no,package_no,template_version,label_url,label_status,print_count,last_print_device,void_reason,version,created_at,updated_at) values(#{labelNo},#{waybillNo},#{packageNo},#{templateVersion},#{labelUrl},#{status},#{printCount},#{lastPrintDevice},#{voidReason},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertLabel(LabelRow row);

    /**
     * 执行命令 {@code updateLabel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code LabelRow}
     */
    @Update("update tms_shipping_label set label_status=#{status},print_count=#{printCount},last_print_device=#{lastPrintDevice},void_reason=#{voidReason},version=#{version},updated_at=now() where label_no=#{labelNo}")
    void updateLabel(LabelRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertOutbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code TransportTaskMapper.OutboxRow}
     */
    @Insert("insert into tms_domain_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(TransportTaskMapper.OutboxRow row);

    /**
     * 查询并返回 {@code listOutbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<TransportTaskMapper.OutboxRow>}
     */
    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from tms_domain_event order by id desc")
    List<TransportTaskMapper.OutboxRow> listOutbox();

    /**
     * 处理当前类型职责中的操作 {@code insertOperationLog}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code TransportTaskMapper.OperationLogRow}
     */
    @Insert("insert into tms_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(TransportTaskMapper.OperationLogRow row);

    /**
     * 查询并返回 {@code listOperationLogs}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<TransportTaskMapper.OperationLogRow>}
     */
    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from tms_operation_log order by id desc")
    List<TransportTaskMapper.OperationLogRow> listOperationLogs();

    /**
     * WaybillRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record WaybillRow(Long id, String waybillNo, String taskNo, String carrierCode, String carrierName, String carrierWaybillNo, String logisticsProductCode, String receiptPayload, int status, String voidReason, String approvalNo, long version) {
    }

    /**
     * LabelRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record LabelRow(Long id, String labelNo, String waybillNo, String packageNo, String templateVersion, String labelUrl, int status, int printCount, String lastPrintDevice, String voidReason, long version) {
    }
}
