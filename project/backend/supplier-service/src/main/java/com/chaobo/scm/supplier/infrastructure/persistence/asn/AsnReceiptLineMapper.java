package com.chaobo.scm.supplier.infrastructure.persistence.asn;

import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;

/**
 * AsnReceiptLineMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface AsnReceiptLineMapper {

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @param asnLineId 业务或技术标识，类型为 {@code long}
     * @param received 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param rejected 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param qualityStatus 生命周期状态，类型为 {@code int}
     * @param qualityReason 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Insert("INSERT IGNORE INTO sup_asn_receipt_line(receipt_line_id,asn_id,asn_line_id,received_qty,rejected_qty,quality_status,quality_reason,source_event_code) VALUES(#{id},#{asnId},#{asnLineId},#{received},#{rejected},#{qualityStatus},#{qualityReason},#{eventCode})")
    int insert(@Param("id") long id, @Param("asnId") long asnId, @Param("asnLineId") long asnLineId, @Param("received") BigDecimal received, @Param("rejected") BigDecimal rejected, @Param("qualityStatus") int qualityStatus, @Param("qualityReason") String qualityReason, @Param("eventCode") String eventCode);
}
