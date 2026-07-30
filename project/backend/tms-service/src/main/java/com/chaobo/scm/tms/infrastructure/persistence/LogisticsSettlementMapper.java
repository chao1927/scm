package com.chaobo.scm.tms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;
import java.util.List;

/**
 * LogisticsSettlementMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface LogisticsSettlementMapper {

    /**
     * 查询并返回 {@code findException}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param exceptionNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ExceptionRow}
     */
    @Select("select exception_no exceptionNo,waybill_no waybillNo,exception_type exceptionType,exception_level level,description,responsible_party responsibleParty,exception_status status,close_result closeResult,version from tms_logistics_exception where exception_no=#{exceptionNo}")
    ExceptionRow findException(@Param("exceptionNo") String exceptionNo);

    /**
     * 查询并返回 {@code listExceptions}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<ExceptionRow>}
     */
    @Select("select exception_no exceptionNo,waybill_no waybillNo,exception_type exceptionType,exception_level level,description,responsible_party responsibleParty,exception_status status,close_result closeResult,version from tms_logistics_exception order by id desc")
    List<ExceptionRow> listExceptions();

    /**
     * 处理当前类型职责中的操作 {@code insertException}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ExceptionRow}
     */
    @Insert("insert into tms_logistics_exception(exception_no,waybill_no,exception_type,exception_level,description,responsible_party,exception_status,close_result,version,created_at,updated_at) values(#{exceptionNo},#{waybillNo},#{exceptionType},#{level},#{description},#{responsibleParty},#{status},#{closeResult},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertException(ExceptionRow row);

    /**
     * 执行命令 {@code updateException}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ExceptionRow}
     */
    @Update("update tms_logistics_exception set responsible_party=#{responsibleParty},exception_status=#{status},close_result=#{closeResult},version=#{version},updated_at=now() where exception_no=#{exceptionNo}")
    void updateException(ExceptionRow row);

    /**
     * 查询并返回 {@code findFeeSource}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param feeSourceNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code FeeSourceRow}
     */
    @Select("select fee_source_no feeSourceNo,waybill_no waybillNo,carrier_code carrierCode,logistics_product_code logisticsProductCode,fee_item_code feeItemCode,amount,currency,billing_period billingPeriod,responsible_party responsibleParty,push_status pushStatus,bms_receive_no bmsReceiveNo,failure_reason failureReason,version from tms_logistics_fee_source where fee_source_no=#{feeSourceNo}")
    FeeSourceRow findFeeSource(@Param("feeSourceNo") String feeSourceNo);

    /**
     * 查询并返回 {@code findFeeSourceByWaybillAndItem}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param feeItemCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code FeeSourceRow}
     */
    @Select("select fee_source_no feeSourceNo,waybill_no waybillNo,carrier_code carrierCode,logistics_product_code logisticsProductCode,fee_item_code feeItemCode,amount,currency,billing_period billingPeriod,responsible_party responsibleParty,push_status pushStatus,bms_receive_no bmsReceiveNo,failure_reason failureReason,version from tms_logistics_fee_source where waybill_no=#{waybillNo} and fee_item_code=#{feeItemCode} order by id desc limit 1")
    FeeSourceRow findFeeSourceByWaybillAndItem(@Param("waybillNo") String waybillNo, @Param("feeItemCode") String feeItemCode);

    /**
     * 查询并返回 {@code listFeeSources}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<FeeSourceRow>}
     */
    @Select("select fee_source_no feeSourceNo,waybill_no waybillNo,carrier_code carrierCode,logistics_product_code logisticsProductCode,fee_item_code feeItemCode,amount,currency,billing_period billingPeriod,responsible_party responsibleParty,push_status pushStatus,bms_receive_no bmsReceiveNo,failure_reason failureReason,version from tms_logistics_fee_source order by id desc")
    List<FeeSourceRow> listFeeSources();

    /**
     * 处理当前类型职责中的操作 {@code insertFeeSource}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code FeeSourceRow}
     */
    @Insert("insert into tms_logistics_fee_source(fee_source_no,waybill_no,carrier_code,logistics_product_code,fee_item_code,amount,currency,billing_period,responsible_party,push_status,bms_receive_no,failure_reason,version,created_at,updated_at) values(#{feeSourceNo},#{waybillNo},#{carrierCode},#{logisticsProductCode},#{feeItemCode},#{amount},#{currency},#{billingPeriod},#{responsibleParty},#{pushStatus},#{bmsReceiveNo},#{failureReason},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertFeeSource(FeeSourceRow row);

    /**
     * 执行命令 {@code updateFeeSource}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code FeeSourceRow}
     */
    @Update("update tms_logistics_fee_source set push_status=#{pushStatus},bms_receive_no=#{bmsReceiveNo},failure_reason=#{failureReason},version=#{version},updated_at=now() where fee_source_no=#{feeSourceNo}")
    void updateFeeSource(FeeSourceRow row);

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
     * ExceptionRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ExceptionRow(Long id, String exceptionNo, String waybillNo, String exceptionType, String level, String description, String responsibleParty, int status, String closeResult, long version) {
    }

    /**
     * FeeSourceRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record FeeSourceRow(Long id, String feeSourceNo, String waybillNo, String carrierCode, String logisticsProductCode, String feeItemCode, BigDecimal amount, String currency, String billingPeriod, String responsibleParty, int pushStatus, String bmsReceiveNo, String failureReason, long version) {
    }
}
