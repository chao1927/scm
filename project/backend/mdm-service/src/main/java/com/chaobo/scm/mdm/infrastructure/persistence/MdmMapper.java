package com.chaobo.scm.mdm.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MdmMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface MdmMapper {

    /**
     * 查询并返回 {@code findType}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code TypeRow}
     */
    @Select("select type_code typeCode,type_name typeName,domain_code domainCode,type_status status,version from mdm_master_data_type where type_code=#{typeCode}")
    TypeRow findType(@Param("typeCode") String typeCode);

    /**
     * 查询并返回 {@code listTypes}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<TypeRow>}
     */
    @Select("select type_code typeCode,type_name typeName,domain_code domainCode,type_status status,version from mdm_master_data_type order by id desc")
    List<TypeRow> listTypes();

    /**
     * 处理当前类型职责中的操作 {@code insertType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code TypeRow}
     */
    @Insert("insert into mdm_master_data_type(type_code,type_name,domain_code,type_status,version,created_at,updated_at) values(#{typeCode},#{typeName},#{domainCode},#{status},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertType(TypeRow row);

    /**
     * 执行命令 {@code updateType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code TypeRow}
     */
    @Update("update mdm_master_data_type set type_name=#{typeName},domain_code=#{domainCode},type_status=#{status},version=#{version},updated_at=now() where type_code=#{typeCode}")
    void updateType(TypeRow row);

    /**
     * 查询并返回 {@code findTemplate}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param templateCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code TemplateRow}
     */
    @Select("select template_code templateCode,type_code typeCode,field_payload fieldPayload,template_status status,version from mdm_field_template where template_code=#{templateCode}")
    TemplateRow findTemplate(@Param("templateCode") String templateCode);

    /**
     * 查询并返回 {@code listTemplates}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<TemplateRow>}
     */
    @Select("select template_code templateCode,type_code typeCode,field_payload fieldPayload,template_status status,version from mdm_field_template order by id desc")
    List<TemplateRow> listTemplates();

    /**
     * 处理当前类型职责中的操作 {@code insertTemplate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code TemplateRow}
     */
    @Insert("insert into mdm_field_template(template_code,type_code,field_payload,template_status,version,created_at,updated_at) values(#{templateCode},#{typeCode},#{fieldPayload},#{status},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertTemplate(TemplateRow row);

    /**
     * 执行命令 {@code updateTemplate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code TemplateRow}
     */
    @Update("update mdm_field_template set type_code=#{typeCode},field_payload=#{fieldPayload},template_status=#{status},version=#{version},updated_at=now() where template_code=#{templateCode}")
    void updateTemplate(TemplateRow row);

    /**
     * 查询并返回 {@code findCodeRule}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param ruleCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code CodeRuleRow}
     */
    @Select("select rule_code ruleCode,type_code typeCode,prefix,serial_length serialLength,rule_status status,current_serial currentSerial,version from mdm_code_rule where rule_code=#{ruleCode}")
    CodeRuleRow findCodeRule(@Param("ruleCode") String ruleCode);

    /**
     * 查询并返回 {@code listCodeRules}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<CodeRuleRow>}
     */
    @Select("select rule_code ruleCode,type_code typeCode,prefix,serial_length serialLength,rule_status status,current_serial currentSerial,version from mdm_code_rule order by id desc")
    List<CodeRuleRow> listCodeRules();

    /**
     * 处理当前类型职责中的操作 {@code insertCodeRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code CodeRuleRow}
     */
    @Insert("insert into mdm_code_rule(rule_code,type_code,prefix,serial_length,rule_status,current_serial,version,created_at,updated_at) values(#{ruleCode},#{typeCode},#{prefix},#{serialLength},#{status},#{currentSerial},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertCodeRule(CodeRuleRow row);

    /**
     * 执行命令 {@code updateCodeRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code CodeRuleRow}
     */
    @Update("update mdm_code_rule set type_code=#{typeCode},prefix=#{prefix},serial_length=#{serialLength},rule_status=#{status},current_serial=#{currentSerial},version=#{version},updated_at=now() where rule_code=#{ruleCode}")
    void updateCodeRule(CodeRuleRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertOutbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code OutboxRow}
     */
    @Insert("insert into mdm_outbox_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(OutboxRow row);

    /**
     * 查询并返回 {@code listOutbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<OutboxRow>}
     */
    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from mdm_outbox_event order by id desc")
    List<OutboxRow> listOutbox();

    /**
     * 处理当前类型职责中的操作 {@code insertOperationLog}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code OperationLogRow}
     */
    @Insert("insert into mdm_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(OperationLogRow row);

    /**
     * 查询并返回 {@code listOperationLogs}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<OperationLogRow>}
     */
    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from mdm_operation_log order by id desc")
    List<OperationLogRow> listOperationLogs();

    /**
     * TypeRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record TypeRow(Long id, String typeCode, String typeName, String domainCode, int status, long version) {
    }

    /**
     * TemplateRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record TemplateRow(Long id, String templateCode, String typeCode, String fieldPayload, int status, long version) {
    }

    /**
     * CodeRuleRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record CodeRuleRow(Long id, String ruleCode, String typeCode, String prefix, int serialLength, int status, long currentSerial, long version) {
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
