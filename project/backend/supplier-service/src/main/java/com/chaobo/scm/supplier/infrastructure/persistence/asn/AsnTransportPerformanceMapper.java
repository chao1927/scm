package com.chaobo.scm.supplier.infrastructure.persistence.asn;

import org.apache.ibatis.annotations.*;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * AsnTransportPerformanceMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface AsnTransportPerformanceMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long asnId, OffsetDateTime shippedAt, OffsetDateTime arrivedAt, long transitMinutes) {
    }

    /**
     * 处理当前类型职责中的操作 {@code arrived}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param from 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param to 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Row>}
     */
    @Select("SELECT a.asn_id asnId,a.ship_at shippedAt,MIN(t.occurred_at) arrivedAt,TIMESTAMPDIFF(MINUTE,a.ship_at,MIN(t.occurred_at)) transitMinutes FROM sup_asn a JOIN sup_asn_transport_trail t ON t.asn_id=a.asn_id AND t.transport_status=3 WHERE a.supplier_id=#{supplierId} AND a.ship_at IS NOT NULL AND t.occurred_at>=#{from} AND t.occurred_at<#{to} GROUP BY a.asn_id,a.ship_at ORDER BY arrivedAt DESC")
    List<Row> arrived(@Param("supplierId") long supplierId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
}
