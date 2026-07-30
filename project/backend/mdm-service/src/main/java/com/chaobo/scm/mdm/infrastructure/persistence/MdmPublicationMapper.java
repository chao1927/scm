package com.chaobo.scm.mdm.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

/**
 * MdmPublicationMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface MdmPublicationMapper {

    /**
     * 查询并返回 {@code findSubscription}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param subscriptionNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code SubscriptionRow}
     */
    @Select("select subscription_no subscriptionNo,type_code typeCode,target_system targetSystem,event_topic eventTopic,filter_rule filterRule,subscription_status status,version from mdm_publication_subscription where subscription_no=#{subscriptionNo}")
    SubscriptionRow findSubscription(@Param("subscriptionNo") String subscriptionNo);

    /**
     * 查询并返回 {@code findActiveSubscription}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param targetSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventTopic 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code SubscriptionRow}
     */
    @Select("select subscription_no subscriptionNo,type_code typeCode,target_system targetSystem,event_topic eventTopic,filter_rule filterRule,subscription_status status,version from mdm_publication_subscription where type_code=#{typeCode} and target_system=#{targetSystem} and event_topic=#{eventTopic} and subscription_status=1 limit 1")
    SubscriptionRow findActiveSubscription(@Param("typeCode") String typeCode, @Param("targetSystem") String targetSystem, @Param("eventTopic") String eventTopic);

    /**
     * 查询并返回 {@code listActiveSubscriptions}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code List<SubscriptionRow>}
     */
    @Select("select subscription_no subscriptionNo,type_code typeCode,target_system targetSystem,event_topic eventTopic,filter_rule filterRule,subscription_status status,version from mdm_publication_subscription where type_code=#{typeCode} and subscription_status=1 order by id desc")
    List<SubscriptionRow> listActiveSubscriptions(@Param("typeCode") String typeCode);

    /**
     * 查询并返回 {@code listSubscriptions}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<SubscriptionRow>}
     */
    @Select("select subscription_no subscriptionNo,type_code typeCode,target_system targetSystem,event_topic eventTopic,filter_rule filterRule,subscription_status status,version from mdm_publication_subscription order by id desc")
    List<SubscriptionRow> listSubscriptions();

    /**
     * 处理当前类型职责中的操作 {@code insertSubscription}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code SubscriptionRow}
     */
    @Insert("insert into mdm_publication_subscription(subscription_no,type_code,target_system,event_topic,filter_rule,subscription_status,version,created_at,updated_at) values(#{subscriptionNo},#{typeCode},#{targetSystem},#{eventTopic},#{filterRule},#{status},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertSubscription(SubscriptionRow row);

    /**
     * 执行命令 {@code updateSubscription}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code SubscriptionRow}
     */
    @Update("update mdm_publication_subscription set subscription_status=#{status},version=#{version},updated_at=now() where subscription_no=#{subscriptionNo}")
    void updateSubscription(SubscriptionRow row);

    /**
     * 查询并返回 {@code findPublication}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param publicationNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code PublicationRow}
     */
    @Select("select publication_no publicationNo,version_no versionNo,type_code typeCode,data_code dataCode,target_system targetSystem,event_topic eventTopic,publish_status status,retry_count retryCount,failure_reason failureReason,version from mdm_publication_log where publication_no=#{publicationNo}")
    PublicationRow findPublication(@Param("publicationNo") String publicationNo);

    /**
     * 查询并返回 {@code listPublications}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<PublicationRow>}
     */
    @Select("select publication_no publicationNo,version_no versionNo,type_code typeCode,data_code dataCode,target_system targetSystem,event_topic eventTopic,publish_status status,retry_count retryCount,failure_reason failureReason,version from mdm_publication_log order by id desc")
    List<PublicationRow> listPublications();

    /**
     * 处理当前类型职责中的操作 {@code insertPublication}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code PublicationRow}
     */
    @Insert("insert into mdm_publication_log(publication_no,version_no,type_code,data_code,target_system,event_topic,publish_status,retry_count,failure_reason,version,created_at,updated_at) values(#{publicationNo},#{versionNo},#{typeCode},#{dataCode},#{targetSystem},#{eventTopic},#{status},#{retryCount},#{failureReason},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertPublication(PublicationRow row);

    /**
     * 执行命令 {@code updatePublication}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code PublicationRow}
     */
    @Update("update mdm_publication_log set publish_status=#{status},retry_count=#{retryCount},failure_reason=#{failureReason},version=#{version},updated_at=now() where publication_no=#{publicationNo}")
    void updatePublication(PublicationRow row);

    /**
     * 处理当前类型职责中的操作 {@code claimEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Insert("insert ignore into mdm_event_inbox(event_id,event_type,business_no,payload,event_status,error_message,created_at,updated_at) values(#{eventId},#{eventType},#{businessNo},#{payload},#{status},#{errorMessage},now(),now())")
    int claimEvent(EventInboxRow row);

    /**
     * 执行命令 {@code updateEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
     */
    @Update("update mdm_event_inbox set event_status=#{status},error_message=#{errorMessage},updated_at=now() where event_id=#{eventId}")
    void updateEvent(EventInboxRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertOutbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code MdmMapper.OutboxRow}
     */
    @Insert("insert into mdm_outbox_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(MdmMapper.OutboxRow row);

    /**
     * 查询并返回 {@code listOutbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmMapper.OutboxRow>}
     */
    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from mdm_outbox_event order by id desc")
    List<MdmMapper.OutboxRow> listOutbox();

    /**
     * 处理当前类型职责中的操作 {@code insertOperationLog}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code MdmMapper.OperationLogRow}
     */
    @Insert("insert into mdm_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(MdmMapper.OperationLogRow row);

    /**
     * 查询并返回 {@code listOperationLogs}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmMapper.OperationLogRow>}
     */
    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from mdm_operation_log order by id desc")
    List<MdmMapper.OperationLogRow> listOperationLogs();

    /**
     * SubscriptionRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record SubscriptionRow(Long id, String subscriptionNo, String typeCode, String targetSystem, String eventTopic, String filterRule, int status, long version) {
    }

    /**
     * PublicationRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record PublicationRow(Long id, String publicationNo, String versionNo, String typeCode, String dataCode, String targetSystem, String eventTopic, int status, int retryCount, String failureReason, long version) {
    }

    /**
     * EventInboxRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record EventInboxRow(String eventId, String eventType, String businessNo, String payload, int status, String errorMessage) {
    }
}
