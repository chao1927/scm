package com.chaobo.scm.supplier.infrastructure.persistence.asn;

import org.apache.ibatis.annotations.*;
import java.time.*;

/**
 * AsnTransportFactMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface AsnTransportFactMapper {

    /**
     * 处理当前类型职责中的操作 {@code upsert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @param shipmentId 业务或技术标识，类型为 {@code Long}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param node 业务处理参数或成员，类型为 {@code String}
     * @param occurredAt 业务时间，类型为 {@code OffsetDateTime}
     * @param exceptionCode 可追踪业务编码，类型为 {@code String}
     * @param exceptionReason 业务处理参数或成员，类型为 {@code String}
     * @param sourceVersion 乐观锁或契约版本，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_asn_transport_fact(asn_id,shipment_id,waybill_no,carrier_code,transport_status,last_node,last_track_at,exception_code,exception_reason,source_version) VALUES(#{asnId},#{shipmentId},#{waybillNo},#{carrierCode},#{status},#{node},#{occurredAt},#{exceptionCode},#{exceptionReason},#{sourceVersion}) ON DUPLICATE KEY UPDATE shipment_id=IF(VALUES(source_version)>source_version,VALUES(shipment_id),shipment_id),waybill_no=IF(VALUES(source_version)>source_version,VALUES(waybill_no),waybill_no),carrier_code=IF(VALUES(source_version)>source_version,VALUES(carrier_code),carrier_code),transport_status=IF(VALUES(source_version)>source_version,VALUES(transport_status),transport_status),last_node=IF(VALUES(source_version)>source_version,VALUES(last_node),last_node),last_track_at=IF(VALUES(source_version)>source_version,VALUES(last_track_at),last_track_at),exception_code=IF(VALUES(source_version)>source_version,VALUES(exception_code),exception_code),exception_reason=IF(VALUES(source_version)>source_version,VALUES(exception_reason),exception_reason),source_version=GREATEST(source_version,VALUES(source_version))")
    void upsert(@Param("asnId") long asnId, @Param("shipmentId") Long shipmentId, @Param("waybillNo") String waybillNo, @Param("carrierCode") String carrierCode, @Param("status") int status, @Param("node") String node, @Param("occurredAt") OffsetDateTime occurredAt, @Param("exceptionCode") String exceptionCode, @Param("exceptionReason") String exceptionReason, @Param("sourceVersion") long sourceVersion);
}
