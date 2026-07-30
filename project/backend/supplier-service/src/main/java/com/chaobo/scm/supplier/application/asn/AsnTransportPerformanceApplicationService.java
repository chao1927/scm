package com.chaobo.scm.supplier.application.asn;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.infrastructure.persistence.asn.AsnTransportPerformanceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

/**
 * AsnTransportPerformanceApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class AsnTransportPerformanceApplicationService {

    /**
     * mapper（类型：{@code AsnTransportPerformanceMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final AsnTransportPerformanceMapper mapper;

    /**
     * 创建 AsnTransportPerformanceApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code AsnTransportPerformanceMapper}
     */
    public AsnTransportPerformanceApplicationService(AsnTransportPerformanceMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code arrived}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param period 业务处理参数或成员，类型为 {@code YearMonth}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<View>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<View> arrived(long supplierId, YearMonth period, Long scope) {
        if (scope != null && scope != supplierId) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "运输绩效不存在");
        }
        var from = period.atDay(1).atStartOfDay().atOffset(ZoneOffset.ofHours(8));
        var to = period.plusMonths(1).atDay(1).atStartOfDay().atOffset(ZoneOffset.ofHours(8));
        return mapper.arrived(supplierId, from, to).stream().map(r -> new View(r.asnId(), r.shippedAt(), r.arrivedAt(), r.transitMinutes())).toList();
    }

    /**
     * View。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record View(long asnId, OffsetDateTime shippedAt, OffsetDateTime arrivedAt, long transitMinutes) {
    }
}
