package com.chaobo.scm.supplier.infrastructure.persistence.asn;

import org.apache.ibatis.annotations.*;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * AsnTransportTrailQueryMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface AsnTransportTrailQueryMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long asnId, Long shipmentId, String waybillNo, int status, String node, OffsetDateTime occurredAt, String exceptionCode, String exceptionReason, long sourceVersion) {
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code List<Row>}
     */
    @Select("SELECT asn_id asnId,shipment_id shipmentId,waybill_no waybillNo,transport_status status,track_node node,occurred_at occurredAt,exception_code exceptionCode,exception_reason exceptionReason,source_version sourceVersion FROM sup_asn_transport_trail WHERE asn_id=#{asnId} ORDER BY occurred_at,trail_id")
    List<Row> list(long asnId);
}
