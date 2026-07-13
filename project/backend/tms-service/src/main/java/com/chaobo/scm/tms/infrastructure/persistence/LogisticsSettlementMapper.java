package com.chaobo.scm.tms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface LogisticsSettlementMapper {
    @Select("select exception_no exceptionNo,waybill_no waybillNo,exception_type exceptionType,exception_level level,description,responsible_party responsibleParty,exception_status status,close_result closeResult,version from tms_logistics_exception where exception_no=#{exceptionNo}")
    ExceptionRow findException(@Param("exceptionNo") String exceptionNo);

    @Select("select exception_no exceptionNo,waybill_no waybillNo,exception_type exceptionType,exception_level level,description,responsible_party responsibleParty,exception_status status,close_result closeResult,version from tms_logistics_exception order by id desc")
    List<ExceptionRow> listExceptions();

    @Insert("insert into tms_logistics_exception(exception_no,waybill_no,exception_type,exception_level,description,responsible_party,exception_status,close_result,version,created_at,updated_at) values(#{exceptionNo},#{waybillNo},#{exceptionType},#{level},#{description},#{responsibleParty},#{status},#{closeResult},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertException(ExceptionRow row);

    @Update("update tms_logistics_exception set responsible_party=#{responsibleParty},exception_status=#{status},close_result=#{closeResult},version=#{version},updated_at=now() where exception_no=#{exceptionNo}")
    void updateException(ExceptionRow row);

    @Select("select fee_source_no feeSourceNo,waybill_no waybillNo,carrier_code carrierCode,logistics_product_code logisticsProductCode,fee_item_code feeItemCode,amount,currency,billing_period billingPeriod,responsible_party responsibleParty,push_status pushStatus,bms_receive_no bmsReceiveNo,failure_reason failureReason,version from tms_logistics_fee_source where fee_source_no=#{feeSourceNo}")
    FeeSourceRow findFeeSource(@Param("feeSourceNo") String feeSourceNo);

    @Select("select fee_source_no feeSourceNo,waybill_no waybillNo,carrier_code carrierCode,logistics_product_code logisticsProductCode,fee_item_code feeItemCode,amount,currency,billing_period billingPeriod,responsible_party responsibleParty,push_status pushStatus,bms_receive_no bmsReceiveNo,failure_reason failureReason,version from tms_logistics_fee_source where waybill_no=#{waybillNo} and fee_item_code=#{feeItemCode} order by id desc limit 1")
    FeeSourceRow findFeeSourceByWaybillAndItem(@Param("waybillNo") String waybillNo,
                                               @Param("feeItemCode") String feeItemCode);

    @Select("select fee_source_no feeSourceNo,waybill_no waybillNo,carrier_code carrierCode,logistics_product_code logisticsProductCode,fee_item_code feeItemCode,amount,currency,billing_period billingPeriod,responsible_party responsibleParty,push_status pushStatus,bms_receive_no bmsReceiveNo,failure_reason failureReason,version from tms_logistics_fee_source order by id desc")
    List<FeeSourceRow> listFeeSources();

    @Insert("insert into tms_logistics_fee_source(fee_source_no,waybill_no,carrier_code,logistics_product_code,fee_item_code,amount,currency,billing_period,responsible_party,push_status,bms_receive_no,failure_reason,version,created_at,updated_at) values(#{feeSourceNo},#{waybillNo},#{carrierCode},#{logisticsProductCode},#{feeItemCode},#{amount},#{currency},#{billingPeriod},#{responsibleParty},#{pushStatus},#{bmsReceiveNo},#{failureReason},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertFeeSource(FeeSourceRow row);

    @Update("update tms_logistics_fee_source set push_status=#{pushStatus},bms_receive_no=#{bmsReceiveNo},failure_reason=#{failureReason},version=#{version},updated_at=now() where fee_source_no=#{feeSourceNo}")
    void updateFeeSource(FeeSourceRow row);

    @Insert("insert into tms_domain_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(TransportTaskMapper.OutboxRow row);

    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from tms_domain_event order by id desc")
    List<TransportTaskMapper.OutboxRow> listOutbox();

    @Insert("insert into tms_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(TransportTaskMapper.OperationLogRow row);

    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from tms_operation_log order by id desc")
    List<TransportTaskMapper.OperationLogRow> listOperationLogs();

    record ExceptionRow(Long id, String exceptionNo, String waybillNo, String exceptionType, String level,
                        String description, String responsibleParty, int status, String closeResult, long version) {}

    record FeeSourceRow(Long id, String feeSourceNo, String waybillNo, String carrierCode,
                        String logisticsProductCode, String feeItemCode, BigDecimal amount, String currency,
                        String billingPeriod, String responsibleParty, int pushStatus, String bmsReceiveNo,
                        String failureReason, long version) {}
}
