package com.chaobo.scm.supplier.infrastructure.persistence.returning;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.returning.*;
import com.chaobo.scm.supplier.domain.returning.*;
import org.springframework.stereotype.Repository;
import java.util.*;

/**
 * MyBatisSupplierReturnAdapter。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisSupplierReturnAdapter implements SupplierReturnRepository, SupplierReturnReadModelPort {

    /**
     * mapper（类型：{@code SupplierReturnMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierReturnMapper mapper;

    /**
     * 创建 MyBatisSupplierReturnAdapter。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code SupplierReturnMapper}
     */
    public MyBatisSupplierReturnAdapter(SupplierReturnMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<SupplierReturnAggregate>}
     */
    public Optional<SupplierReturnAggregate> findById(long id) {
        var h = mapper.find(id);
        return h == null ? Optional.empty() : Optional.of(aggregate(h));
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param a 业务处理参数或成员，类型为 {@code SupplierReturnAggregate}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    public void save(SupplierReturnAggregate a, long operator) {
        var h = header(a);
        if (mapper.find(a.id()) == null) {
            mapper.insert(h, operator);
            for (var l : a.lines()) {
                mapper.insertLine(line(a.id(), l));
            }
        } else {
            if (mapper.update(h, a.version() - 1, operator) != 1) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "退供单已被更新");
            }
            for (var l : a.lines()) {
                mapper.updateLine(line(a.id(), l));
            }
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Optional<SupplierReturnView>}
     */
    public Optional<SupplierReturnView> detail(long id) {
        var h = mapper.find(id);
        return h == null ? Optional.empty() : Optional.of(view(h));
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param page 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<SupplierReturnView>}
     */
    public PageResult<SupplierReturnView> page(Long supplierId, Integer status, int page, int size) {
        return new PageResult<>(page, size, mapper.count(supplierId, status), mapper.page(supplierId, status, (page - 1) * size, size).stream().map(this::view).toList());
    }

    /**
     * 处理当前类型职责中的操作 {@code aggregate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param h 业务处理参数或成员，类型为 {@code SupplierReturnMapper.Header}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierReturnAggregate}
     */
    private SupplierReturnAggregate aggregate(SupplierReturnMapper.Header h) {
        return SupplierReturnAggregate.rehydrate(h.id(), h.no(), h.supplierId(), h.warehouseId(), h.qualityIssueId(), h.reason(), mapper.lines(h.id()).stream().map(l -> new SupplierReturnLine(l.id(), l.skuCode(), l.batchNo(), l.inventoryStatus(), l.requestedQty(), l.lockedQty(), l.outboundQty(), l.signedQty())).toList(), h.status(), h.inventoryLockNo(), h.supplierConfirmedAt(), h.outboundNo(), h.shipmentId(), h.waybillNo(), h.carrierCode(), h.settlementCompleted(), h.settlementRef(), h.offsetAmount(), h.claimAmount(), h.exceptionReason(), h.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code header}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code SupplierReturnAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierReturnMapper.Header}
     */
    private SupplierReturnMapper.Header header(SupplierReturnAggregate a) {
        return new SupplierReturnMapper.Header(a.id(), a.no(), a.supplierId(), a.warehouseId(), a.qualityIssueId(), a.reason(), a.status().code(), a.inventoryLockNo(), a.supplierConfirmedAt(), a.outboundNo(), a.shipmentId(), a.waybillNo(), a.carrierCode(), a.settlementCompleted(), a.settlementRef(), a.offsetAmount(), a.claimAmount(), a.exceptionReason(), a.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code line}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param returnId 业务或技术标识，类型为 {@code long}
     * @param l 业务处理参数或成员，类型为 {@code SupplierReturnLine}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierReturnMapper.Line}
     */
    private SupplierReturnMapper.Line line(long returnId, SupplierReturnLine l) {
        return new SupplierReturnMapper.Line(l.id(), returnId, l.skuCode(), l.batchNo(), l.inventoryStatus(), l.requestedQty(), l.lockedQty(), l.outboundQty(), l.signedQty());
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param h 业务处理参数或成员，类型为 {@code SupplierReturnMapper.Header}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierReturnView}
     */
    private SupplierReturnView view(SupplierReturnMapper.Header h) {
        var s = SupplierReturnStatus.fromCode(h.status());
        return new SupplierReturnView(h.id(), h.no(), h.supplierId(), h.warehouseId(), h.qualityIssueId(), h.reason(), s.code(), s.label(), h.inventoryLockNo(), h.supplierConfirmedAt(), h.outboundNo(), h.shipmentId(), h.waybillNo(), h.carrierCode(), h.settlementCompleted(), h.settlementRef(), h.offsetAmount(), h.claimAmount(), h.exceptionReason(), h.version(), mapper.lines(h.id()).stream().map(l -> new SupplierReturnView.Line(l.id(), l.skuCode(), l.batchNo(), l.inventoryStatus(), l.requestedQty(), l.lockedQty(), l.outboundQty(), l.signedQty())).toList());
    }
}
