package com.chaobo.scm.supplier.application.asn;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.domain.asn.AsnRepository;
import com.chaobo.scm.supplier.infrastructure.persistence.asn.AsnTransportTrailQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * AsnTransportQueryApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class AsnTransportQueryApplicationService {

    /**
     * asns（类型：{@code AsnRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AsnRepository asns;

    /**
     * mapper（类型：{@code AsnTransportTrailQueryMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final AsnTransportTrailQueryMapper mapper;

    /**
     * 创建 AsnTransportQueryApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param asns 业务处理参数或成员，类型为 {@code AsnRepository}
     * @param mapper 持久化访问依赖，类型为 {@code AsnTransportTrailQueryMapper}
     */
    public AsnTransportQueryApplicationService(AsnRepository asns, AsnTransportTrailQueryMapper mapper) {
        this.asns = asns;
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code trails}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Trail>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<Trail> trails(long asnId, Long scope) {
        var asn = asns.findById(asnId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "ASN不存在"));
        if (scope != null && scope != asn.supplierId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "ASN不存在");
        }
        return mapper.list(asnId).stream().map(r -> new Trail(r.shipmentId(), r.waybillNo(), r.status(), r.node(), r.occurredAt(), r.exceptionCode(), r.exceptionReason(), r.sourceVersion())).toList();
    }

    /**
     * Trail。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Trail(Long shipmentId, String waybillNo, int transportStatus, String trackNode, OffsetDateTime occurredAt, String exceptionCode, String exceptionReason, long sourceVersion) {
    }
}
