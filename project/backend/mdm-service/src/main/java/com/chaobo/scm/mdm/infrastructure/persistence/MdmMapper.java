package com.chaobo.scm.mdm.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MdmMapper {
    @Select("select type_code typeCode,type_name typeName,domain_code domainCode,type_status status,version from mdm_master_data_type where type_code=#{typeCode}")
    TypeRow findType(@Param("typeCode") String typeCode);

    @Select("select type_code typeCode,type_name typeName,domain_code domainCode,type_status status,version from mdm_master_data_type order by id desc")
    List<TypeRow> listTypes();

    @Insert("insert into mdm_master_data_type(type_code,type_name,domain_code,type_status,version,created_at,updated_at) values(#{typeCode},#{typeName},#{domainCode},#{status},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertType(TypeRow row);

    @Update("update mdm_master_data_type set type_name=#{typeName},domain_code=#{domainCode},type_status=#{status},version=#{version},updated_at=now() where type_code=#{typeCode}")
    void updateType(TypeRow row);

    @Select("select template_code templateCode,type_code typeCode,field_payload fieldPayload,template_status status,version from mdm_field_template where template_code=#{templateCode}")
    TemplateRow findTemplate(@Param("templateCode") String templateCode);

    @Select("select template_code templateCode,type_code typeCode,field_payload fieldPayload,template_status status,version from mdm_field_template order by id desc")
    List<TemplateRow> listTemplates();

    @Insert("insert into mdm_field_template(template_code,type_code,field_payload,template_status,version,created_at,updated_at) values(#{templateCode},#{typeCode},#{fieldPayload},#{status},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertTemplate(TemplateRow row);

    @Update("update mdm_field_template set type_code=#{typeCode},field_payload=#{fieldPayload},template_status=#{status},version=#{version},updated_at=now() where template_code=#{templateCode}")
    void updateTemplate(TemplateRow row);

    @Select("select rule_code ruleCode,type_code typeCode,prefix,serial_length serialLength,rule_status status,current_serial currentSerial,version from mdm_code_rule where rule_code=#{ruleCode}")
    CodeRuleRow findCodeRule(@Param("ruleCode") String ruleCode);

    @Select("select rule_code ruleCode,type_code typeCode,prefix,serial_length serialLength,rule_status status,current_serial currentSerial,version from mdm_code_rule order by id desc")
    List<CodeRuleRow> listCodeRules();

    @Insert("insert into mdm_code_rule(rule_code,type_code,prefix,serial_length,rule_status,current_serial,version,created_at,updated_at) values(#{ruleCode},#{typeCode},#{prefix},#{serialLength},#{status},#{currentSerial},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertCodeRule(CodeRuleRow row);

    @Update("update mdm_code_rule set type_code=#{typeCode},prefix=#{prefix},serial_length=#{serialLength},rule_status=#{status},current_serial=#{currentSerial},version=#{version},updated_at=now() where rule_code=#{ruleCode}")
    void updateCodeRule(CodeRuleRow row);

    @Insert("insert into mdm_outbox_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(OutboxRow row);

    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from mdm_outbox_event order by id desc")
    List<OutboxRow> listOutbox();

    @Insert("insert into mdm_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(OperationLogRow row);

    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from mdm_operation_log order by id desc")
    List<OperationLogRow> listOperationLogs();

    record TypeRow(Long id, String typeCode, String typeName, String domainCode, int status, long version) {}
    record TemplateRow(Long id, String templateCode, String typeCode, String fieldPayload, int status, long version) {}
    record CodeRuleRow(Long id, String ruleCode, String typeCode, String prefix, int serialLength, int status, long currentSerial, long version) {}
    record OutboxRow(String eventType, String businessNo, String payload, int status, LocalDateTime occurredAt) {}
    record OperationLogRow(String operationType, String businessNo, Long operatorId, String idempotencyKey, LocalDateTime createdAt) {}
}
