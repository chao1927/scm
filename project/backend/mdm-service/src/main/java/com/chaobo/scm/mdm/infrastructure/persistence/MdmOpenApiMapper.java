package com.chaobo.scm.mdm.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

/**
 * MdmOpenApiMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface MdmOpenApiMapper {

    /**
     * 处理当前类型职责中的操作 {@code claimEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code MdmPublicationMapper.EventInboxRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Insert("insert ignore into mdm_event_inbox(event_id,event_type,business_no,payload,event_status,error_message,created_at,updated_at) values(#{eventId},#{eventType},#{businessNo},#{payload},#{status},#{errorMessage},now(),now())")
    int claimEvent(MdmPublicationMapper.EventInboxRow row);

    /**
     * 执行命令 {@code updateEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code MdmPublicationMapper.EventInboxRow}
     */
    @Update("update mdm_event_inbox set event_status=#{status},error_message=#{errorMessage},updated_at=now() where event_id=#{eventId}")
    void updateEvent(MdmPublicationMapper.EventInboxRow row);

    /**
     * 查询并返回 {@code listInboxEvents}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmPublicationMapper.EventInboxRow>}
     */
    @Select("select event_id eventId,event_type eventType,business_no businessNo,payload,event_status status,error_message errorMessage from mdm_event_inbox order by updated_at desc")
    List<MdmPublicationMapper.EventInboxRow> listInboxEvents();

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
}
