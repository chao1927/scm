package com.chaobo.scm.mdm.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MdmPublicationMapper {
    @Select("select subscription_no subscriptionNo,type_code typeCode,target_system targetSystem,event_topic eventTopic,filter_rule filterRule,subscription_status status,version from mdm_publication_subscription where subscription_no=#{subscriptionNo}")
    SubscriptionRow findSubscription(@Param("subscriptionNo") String subscriptionNo);

    @Select("select subscription_no subscriptionNo,type_code typeCode,target_system targetSystem,event_topic eventTopic,filter_rule filterRule,subscription_status status,version from mdm_publication_subscription where type_code=#{typeCode} and target_system=#{targetSystem} and event_topic=#{eventTopic} and subscription_status=1 limit 1")
    SubscriptionRow findActiveSubscription(@Param("typeCode") String typeCode,
                                           @Param("targetSystem") String targetSystem,
                                           @Param("eventTopic") String eventTopic);

    @Select("select subscription_no subscriptionNo,type_code typeCode,target_system targetSystem,event_topic eventTopic,filter_rule filterRule,subscription_status status,version from mdm_publication_subscription where type_code=#{typeCode} and subscription_status=1 order by id desc")
    List<SubscriptionRow> listActiveSubscriptions(@Param("typeCode") String typeCode);

    @Select("select subscription_no subscriptionNo,type_code typeCode,target_system targetSystem,event_topic eventTopic,filter_rule filterRule,subscription_status status,version from mdm_publication_subscription order by id desc")
    List<SubscriptionRow> listSubscriptions();

    @Insert("insert into mdm_publication_subscription(subscription_no,type_code,target_system,event_topic,filter_rule,subscription_status,version,created_at,updated_at) values(#{subscriptionNo},#{typeCode},#{targetSystem},#{eventTopic},#{filterRule},#{status},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertSubscription(SubscriptionRow row);

    @Update("update mdm_publication_subscription set subscription_status=#{status},version=#{version},updated_at=now() where subscription_no=#{subscriptionNo}")
    void updateSubscription(SubscriptionRow row);

    @Select("select publication_no publicationNo,version_no versionNo,type_code typeCode,data_code dataCode,target_system targetSystem,event_topic eventTopic,publish_status status,retry_count retryCount,failure_reason failureReason,version from mdm_publication_log where publication_no=#{publicationNo}")
    PublicationRow findPublication(@Param("publicationNo") String publicationNo);

    @Select("select publication_no publicationNo,version_no versionNo,type_code typeCode,data_code dataCode,target_system targetSystem,event_topic eventTopic,publish_status status,retry_count retryCount,failure_reason failureReason,version from mdm_publication_log order by id desc")
    List<PublicationRow> listPublications();

    @Insert("insert into mdm_publication_log(publication_no,version_no,type_code,data_code,target_system,event_topic,publish_status,retry_count,failure_reason,version,created_at,updated_at) values(#{publicationNo},#{versionNo},#{typeCode},#{dataCode},#{targetSystem},#{eventTopic},#{status},#{retryCount},#{failureReason},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertPublication(PublicationRow row);

    @Update("update mdm_publication_log set publish_status=#{status},retry_count=#{retryCount},failure_reason=#{failureReason},version=#{version},updated_at=now() where publication_no=#{publicationNo}")
    void updatePublication(PublicationRow row);

    @Insert("insert ignore into mdm_event_inbox(event_id,event_type,business_no,payload,event_status,error_message,created_at,updated_at) values(#{eventId},#{eventType},#{businessNo},#{payload},#{status},#{errorMessage},now(),now())")
    int claimEvent(EventInboxRow row);

    @Update("update mdm_event_inbox set event_status=#{status},error_message=#{errorMessage},updated_at=now() where event_id=#{eventId}")
    void updateEvent(EventInboxRow row);

    @Insert("insert into mdm_outbox_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(MdmMapper.OutboxRow row);

    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from mdm_outbox_event order by id desc")
    List<MdmMapper.OutboxRow> listOutbox();

    @Insert("insert into mdm_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(MdmMapper.OperationLogRow row);

    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from mdm_operation_log order by id desc")
    List<MdmMapper.OperationLogRow> listOperationLogs();

    record SubscriptionRow(Long id, String subscriptionNo, String typeCode, String targetSystem, String eventTopic,
                           String filterRule, int status, long version) {}

    record PublicationRow(Long id, String publicationNo, String versionNo, String typeCode, String dataCode,
                          String targetSystem, String eventTopic, int status, int retryCount, String failureReason,
                          long version) {}

    record EventInboxRow(String eventId, String eventType, String businessNo, String payload, int status,
                         String errorMessage) {}
}
