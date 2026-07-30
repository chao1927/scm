package com.chaobo.scm.wms.application.inspection;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.inspection.InspectionAggregate;
import com.chaobo.scm.wms.infrastructure.persistence.inspection.InspectionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * InspectionApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class InspectionApplicationService {

    /**
     * mapper（类型：{@code InspectionMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final InspectionMapper mapper;

    /**
     * events（类型：{@code WmsEventPublisher}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final WmsEventPublisher events;

    /**
     * ids（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    /**
     * 创建 InspectionApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code InspectionMapper}
     * @param events 业务处理参数或成员，类型为 {@code WmsEventPublisher}
     */
    public InspectionApplicationService(InspectionMapper mapper, WmsEventPublisher events) {
        this.mapper = mapper;
        this.events = events;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param receipt 业务处理参数或成员，类型为 {@code long}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result create(String no, long receipt, BigDecimal qty, long operator) {
        var existed = mapper.find(no);
        if (existed != null) {
            return view(map(existed), true);
        }
        var inspection = new InspectionAggregate(ids.incrementAndGet(), no, receipt, qty);
        save(inspection, operator, true, 0);
        return view(inspection, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code result}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param qualified 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param unqualified 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result result(String no, int version, BigDecimal qualified, BigDecimal unqualified, long operator) {
        var inspection = load(no);
        if (inspection.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "质检单版本冲突");
        }
        inspection.submit(qualified, unqualified);
        save(inspection, operator, false, version);
        events.publish("WmsQualityInspectionCompleted", "INSPECTION", inspection.inspectionNo(), inspection.version(), payload(inspection));
        return view(inspection, false);
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code InspectionAggregate}
     */
    private InspectionAggregate load(String no) {
        var row = mapper.find(no);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "质检单不存在");
        }
        return map(row);
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param inspection 业务处理参数或成员，类型为 {@code InspectionAggregate}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param insert 业务处理参数或成员，类型为 {@code boolean}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     */
    private void save(InspectionAggregate inspection, long operator, boolean insert, int expectedVersion) {
        if (insert) {
            mapper.insert(inspection.id(), inspection.inspectionNo(), inspection.receiptId(), inspection.inspectQty(), inspection.qualifiedQty(), inspection.unqualifiedQty(), inspection.completed() ? 2 : 1, inspection.version(), operator);
            return;
        }
        int updated = mapper.update(inspection.id(), inspection.qualifiedQty(), inspection.unqualifiedQty(), inspection.completed() ? 2 : 1, inspection.version(), expectedVersion, operator);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "质检单版本冲突");
        }
    }

    /**
     * 转换数据模型 {@code map}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code InspectionMapper.Row}
     * @return 转换数据模型的结果，类型为 {@code InspectionAggregate}
     */
    private InspectionAggregate map(InspectionMapper.Row row) {
        return InspectionAggregate.rehydrate(row.id(), row.no(), row.receiptId(), row.qty(), row.qualified(), row.unqualified(), row.status() == 2, row.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param inspection 业务处理参数或成员，类型为 {@code InspectionAggregate}
     * @param duplicated 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    private static Result view(InspectionAggregate inspection, boolean duplicated) {
        return new Result(inspection.id(), inspection.inspectionNo(), inspection.qualifiedQty(), inspection.unqualifiedQty(), inspection.completed(), inspection.version(), duplicated);
    }

    /**
     * 处理当前类型职责中的操作 {@code payload}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param inspection 业务处理参数或成员，类型为 {@code InspectionAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String payload(InspectionAggregate inspection) {
        return """
            {"inspectionNo":"%s","qualifiedQty":%s,"unqualifiedQty":%s}
            """.formatted(inspection.inspectionNo(), inspection.qualifiedQty(), inspection.unqualifiedQty()).trim();
    }

    /**
     * Result。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Result(long id, String no, BigDecimal qualified, BigDecimal unqualified, boolean completed, int version, boolean duplicated) {
    }
}
