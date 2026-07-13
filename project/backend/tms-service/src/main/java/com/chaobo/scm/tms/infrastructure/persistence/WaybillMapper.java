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
public interface WaybillMapper {
    @Select("select waybill_no waybillNo,task_no taskNo,carrier_code carrierCode,carrier_name carrierName,carrier_waybill_no carrierWaybillNo,logistics_product_code logisticsProductCode,receipt_payload receiptPayload,waybill_status status,void_reason voidReason,approval_no approvalNo,version from tms_waybill where waybill_no=#{waybillNo}")
    WaybillRow findWaybill(@Param("waybillNo") String waybillNo);

    @Select("select waybill_no waybillNo,task_no taskNo,carrier_code carrierCode,carrier_name carrierName,carrier_waybill_no carrierWaybillNo,logistics_product_code logisticsProductCode,receipt_payload receiptPayload,waybill_status status,void_reason voidReason,approval_no approvalNo,version from tms_waybill where task_no=#{taskNo} and waybill_status<>2 order by id desc limit 1")
    WaybillRow findActiveWaybillByTask(@Param("taskNo") String taskNo);

    @Select("select waybill_no waybillNo,task_no taskNo,carrier_code carrierCode,carrier_name carrierName,carrier_waybill_no carrierWaybillNo,logistics_product_code logisticsProductCode,receipt_payload receiptPayload,waybill_status status,void_reason voidReason,approval_no approvalNo,version from tms_waybill order by id desc")
    List<WaybillRow> listWaybills();

    @Insert("insert into tms_waybill(waybill_no,task_no,carrier_code,carrier_name,carrier_waybill_no,logistics_product_code,receipt_payload,waybill_status,void_reason,approval_no,version,created_at,updated_at) values(#{waybillNo},#{taskNo},#{carrierCode},#{carrierName},#{carrierWaybillNo},#{logisticsProductCode},#{receiptPayload},#{status},#{voidReason},#{approvalNo},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertWaybill(WaybillRow row);

    @Update("update tms_waybill set waybill_status=#{status},void_reason=#{voidReason},approval_no=#{approvalNo},version=#{version},updated_at=now() where waybill_no=#{waybillNo}")
    void updateWaybill(WaybillRow row);

    @Select("select label_no labelNo,waybill_no waybillNo,package_no packageNo,template_version templateVersion,label_url labelUrl,label_status status,print_count printCount,last_print_device lastPrintDevice,void_reason voidReason,version from tms_shipping_label where label_no=#{labelNo}")
    LabelRow findLabel(@Param("labelNo") String labelNo);

    @Select("select label_no labelNo,waybill_no waybillNo,package_no packageNo,template_version templateVersion,label_url labelUrl,label_status status,print_count printCount,last_print_device lastPrintDevice,void_reason voidReason,version from tms_shipping_label where waybill_no=#{waybillNo} and package_no=#{packageNo} and label_status<>3 order by id desc limit 1")
    LabelRow findActiveLabel(@Param("waybillNo") String waybillNo, @Param("packageNo") String packageNo);

    @Select("select label_no labelNo,waybill_no waybillNo,package_no packageNo,template_version templateVersion,label_url labelUrl,label_status status,print_count printCount,last_print_device lastPrintDevice,void_reason voidReason,version from tms_shipping_label where waybill_no=#{waybillNo} order by id desc")
    List<LabelRow> listLabelsByWaybill(@Param("waybillNo") String waybillNo);

    @Insert("insert into tms_shipping_label(label_no,waybill_no,package_no,template_version,label_url,label_status,print_count,last_print_device,void_reason,version,created_at,updated_at) values(#{labelNo},#{waybillNo},#{packageNo},#{templateVersion},#{labelUrl},#{status},#{printCount},#{lastPrintDevice},#{voidReason},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertLabel(LabelRow row);

    @Update("update tms_shipping_label set label_status=#{status},print_count=#{printCount},last_print_device=#{lastPrintDevice},void_reason=#{voidReason},version=#{version},updated_at=now() where label_no=#{labelNo}")
    void updateLabel(LabelRow row);

    @Insert("insert into tms_domain_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(TransportTaskMapper.OutboxRow row);

    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from tms_domain_event order by id desc")
    List<TransportTaskMapper.OutboxRow> listOutbox();

    @Insert("insert into tms_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(TransportTaskMapper.OperationLogRow row);

    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from tms_operation_log order by id desc")
    List<TransportTaskMapper.OperationLogRow> listOperationLogs();

    record WaybillRow(Long id, String waybillNo, String taskNo, String carrierCode, String carrierName,
                      String carrierWaybillNo, String logisticsProductCode, String receiptPayload, int status,
                      String voidReason, String approvalNo, long version) {}

    record LabelRow(Long id, String labelNo, String waybillNo, String packageNo, String templateVersion,
                    String labelUrl, int status, int printCount, String lastPrintDevice, String voidReason,
                    long version) {}
}
