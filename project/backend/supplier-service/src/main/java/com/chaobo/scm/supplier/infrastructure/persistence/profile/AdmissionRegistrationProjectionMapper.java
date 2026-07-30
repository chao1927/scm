package com.chaobo.scm.supplier.infrastructure.persistence.profile;

import org.apache.ibatis.annotations.*;
import java.time.OffsetDateTime;

/**
 * AdmissionRegistrationProjectionMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface AdmissionRegistrationProjectionMapper {

    /**
     * 处理当前类型职责中的操作 {@code registered}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param admissionId 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param supplierCode 可追踪业务编码，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param sourceVersion 乐观锁或契约版本，类型为 {@code long}
     * @param occurredAt 业务时间，类型为 {@code OffsetDateTime}
     */
    @Insert("INSERT INTO sup_admission_registration(admission_id,supplier_id,supplier_code,registration_status,source_event_code,source_version,registered_at) VALUES(#{admissionId},#{supplierId},#{supplierCode},1,#{eventCode},#{sourceVersion},#{occurredAt}) ON DUPLICATE KEY UPDATE supplier_id=IF(source_version<=VALUES(source_version),VALUES(supplier_id),supplier_id),supplier_code=IF(source_version<=VALUES(source_version),VALUES(supplier_code),supplier_code),registration_status=IF(source_version<=VALUES(source_version),1,registration_status),source_version=GREATEST(source_version,VALUES(source_version)),registered_at=IF(source_version<=VALUES(source_version),VALUES(registered_at),registered_at)")
    void registered(@Param("admissionId") long admissionId, @Param("supplierId") long supplierId, @Param("supplierCode") String supplierCode, @Param("eventCode") String eventCode, @Param("sourceVersion") long sourceVersion, @Param("occurredAt") OffsetDateTime occurredAt);
}
