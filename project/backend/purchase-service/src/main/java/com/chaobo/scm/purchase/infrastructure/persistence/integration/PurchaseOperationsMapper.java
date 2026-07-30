package com.chaobo.scm.purchase.infrastructure.persistence.integration;

import com.chaobo.scm.purchase.application.operations.PurchaseOperationsViews;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

/**
 * PurchaseOperationsMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface PurchaseOperationsMapper {

    /**
     * 处理当前类型职责中的操作 {@code failedInboundEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PurchaseOperationsViews.FailedEvent>}
     */
    @Select("""
        select inbox_id id, source_system sourceSystem, event_code eventCode, event_type eventType,
               consumer_name consumerName, retry_count retryCount, last_error reason, updated_at updatedAt
        from purchase_inbox_event
        where status = 3
        order by updated_at desc
        limit #{limit}
        """)
    List<PurchaseOperationsViews.FailedEvent> failedInboundEvents(int limit);

    /**
     * 处理当前类型职责中的操作 {@code failedCommands}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PurchaseOperationsViews.FailedCommand>}
     */
    @Select("select command_id commandId,command_type commandType,target_system targetSystem,business_no businessNo," + "retry_count retryCount,last_error reason,updated_at updatedAt from purchase_integration_command " + "where status=5 order by updated_at desc limit #{limit}")
    List<PurchaseOperationsViews.FailedCommand> failedCommands(int limit);

    /**
     * 执行命令 {@code replayCommand}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param commandId 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("update purchase_integration_command set status=1,retry_count=0,next_retry_at=null,last_error=null," + "updated_at=now(3) where command_id=#{commandId} and status=5")
    int replayCommand(long commandId);
}
