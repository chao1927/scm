package com.chaobo.scm.supplier.infrastructure.persistence.profile;

import com.chaobo.scm.supplier.application.profile.AdmissionRegistrationProjectionPort;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;

/**
 * MyBatisAdmissionRegistrationProjection。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisAdmissionRegistrationProjection implements AdmissionRegistrationProjectionPort {

    /**
     * mapper（类型：{@code AdmissionRegistrationProjectionMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final AdmissionRegistrationProjectionMapper mapper;

    /**
     * 创建 MyBatisAdmissionRegistrationProjection。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code AdmissionRegistrationProjectionMapper}
     */
    public MyBatisAdmissionRegistrationProjection(AdmissionRegistrationProjectionMapper mapper) {
        this.mapper = mapper;
    }

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
    public void registered(long admissionId, long supplierId, String supplierCode, String eventCode, long sourceVersion, OffsetDateTime occurredAt) {
        mapper.registered(admissionId, supplierId, supplierCode, eventCode, sourceVersion, occurredAt);
    }
}
