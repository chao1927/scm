package com.chaobo.scm.supplier.infrastructure.persistence.rfq;

import org.apache.ibatis.annotations.*;
import java.time.*;

/**
 * SupplierQuoteTodoMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierQuoteTodoMapper {

    /**
     * TodoState。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record TodoState(int status, OffsetDateTime deadline) {
    }

    /**
     * 处理当前类型职责中的操作 {@code upsert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param rfqId 业务或技术标识，类型为 {@code long}
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param deadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param snapshot 业务处理参数或成员，类型为 {@code String}
     */
    @Insert("INSERT INTO sup_supplier_quote_todo(todo_id,rfq_id,rfq_no,supplier_id,quote_deadline,todo_status,rfq_snapshot_json) VALUES(#{id},#{rfqId},#{rfqNo},#{supplierId},#{deadline},1,CAST(#{snapshot} AS JSON)) ON DUPLICATE KEY UPDATE quote_deadline=VALUES(quote_deadline),rfq_snapshot_json=VALUES(rfq_snapshot_json),todo_status=IF(todo_status=3,3,1)")
    void upsert(@Param("id") long id, @Param("rfqId") long rfqId, @Param("rfqNo") String rfqNo, @Param("supplierId") long supplierId, @Param("deadline") OffsetDateTime deadline, @Param("snapshot") String snapshot);

    /**
     * 处理当前类型职责中的操作 {@code state}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param rfqId 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TodoState}
     */
    @Select("SELECT todo_status status,quote_deadline deadline FROM sup_supplier_quote_todo WHERE rfq_id=#{rfqId} AND supplier_id=#{supplierId}")
    TodoState state(@Param("rfqId") long rfqId, @Param("supplierId") long supplierId);

    /**
     * 处理当前类型职责中的操作 {@code markSubmitted}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param rfqId 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_supplier_quote_todo SET todo_status=2,version=version+1 WHERE rfq_id=#{rfqId} AND supplier_id=#{supplierId} AND todo_status=1")
    int markSubmitted(@Param("rfqId") long rfqId, @Param("supplierId") long supplierId);

    /**
     * 执行命令 {@code close}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param rfqId 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_supplier_quote_todo SET todo_status=3,version=version+1 WHERE rfq_id=#{rfqId} AND todo_status IN (1,2)")
    int close(@Param("rfqId") long rfqId);
}
