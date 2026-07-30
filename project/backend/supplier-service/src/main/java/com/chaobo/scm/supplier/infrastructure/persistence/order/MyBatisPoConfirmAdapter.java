package com.chaobo.scm.supplier.infrastructure.persistence.order;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.order.*;
import com.chaobo.scm.supplier.domain.order.*;
import org.springframework.stereotype.Repository;
import java.util.*;

/**
 * MyBatisPoConfirmAdapter。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisPoConfirmAdapter implements PoConfirmRepository, PoConfirmReadModelPort {

    /**
     * mapper（类型：{@code PoConfirmMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final PoConfirmMapper mapper;

    /**
     * 创建 MyBatisPoConfirmAdapter。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code PoConfirmMapper}
     */
    public MyBatisPoConfirmAdapter(PoConfirmMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<PoConfirmAggregate>}
     */
    public Optional<PoConfirmAggregate> findById(long id) {
        var head = mapper.find(id);
        return head == null ? Optional.empty() : Optional.of(aggregate(head));
    }

    /**
     * 查询并返回 {@code findByPurchaseOrderId}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<PoConfirmAggregate>}
     */
    public Optional<PoConfirmAggregate> findByPurchaseOrderId(long id) {
        var head = mapper.findByPo(id);
        return head == null ? Optional.empty() : Optional.of(aggregate(head));
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param aggregate 业务处理参数或成员，类型为 {@code PoConfirmAggregate}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    public void save(PoConfirmAggregate aggregate, long operator) {
        var head = head(aggregate);
        if (mapper.find(aggregate.confirmId()) == null) {
            mapper.insert(head, operator);
            aggregate.lines().forEach(value -> mapper.insertLine(line(aggregate.confirmId(), value), operator));
            return;
        }
        if (mapper.update(head, aggregate.version() - 1, operator) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "采购订单确认已被更新");
        }
        for (var value : aggregate.lines()) {
            var row = line(aggregate.confirmId(), value);
            if (mapper.updateLine(row, operator) == 0) {
                mapper.insertLine(row, operator);
            }
        }
        mapper.deleteMissingLines(aggregate.confirmId(), aggregate.lines().stream().map(PoConfirmLine::lineId).toList(), operator);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Optional<PoConfirmView>}
     */
    public Optional<PoConfirmView> detail(long id) {
        var head = mapper.find(id);
        return head == null ? Optional.empty() : Optional.of(view(head, true));
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<PoConfirmView>}
     */
    public PageResult<PoConfirmView> page(Long supplierId, Integer status, String keyword, int pageNo, int pageSize) {
        long total = mapper.count(supplierId, status, keyword);
        var records = mapper.page(supplierId, status, keyword, (pageNo - 1) * pageSize, pageSize).stream().map(head -> view(head, false)).toList();
        return new PageResult<>(pageNo, pageSize, total, records);
    }

    /**
     * 处理当前类型职责中的操作 {@code aggregate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param head 业务处理参数或成员，类型为 {@code PoConfirmMapper.Head}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PoConfirmAggregate}
     */
    private PoConfirmAggregate aggregate(PoConfirmMapper.Head head) {
        var lines = mapper.lines(head.orderId()).stream().map(value -> new PoConfirmLine(value.orderLineId(), value.skuCode(), value.orderQty(), value.requestedDeliveryDate(), value.confirmedQty(), value.confirmedDeliveryDate(), value.lineStatus(), value.diffReason())).toList();
        return PoConfirmAggregate.rehydrate(head.orderId(), head.confirmNo(), head.purchaseOrderId(), head.purchaseOrderNo(), head.supplierId(), head.confirmDeadline(), lines, PoConfirmStatus.fromCode(head.confirmStatus()), head.confirmedAt(), head.diffType(), head.reasonCode(), head.remark(), head.sourceVersion(), head.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code head}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code PoConfirmAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PoConfirmMapper.Head}
     */
    private PoConfirmMapper.Head head(PoConfirmAggregate aggregate) {
        return new PoConfirmMapper.Head(aggregate.confirmId(), aggregate.confirmNo(), aggregate.purchaseOrderId(), aggregate.purchaseOrderNo(), aggregate.supplierId(), aggregate.status().code(), aggregate.deadline(), aggregate.confirmedAt(), aggregate.diffType(), aggregate.reasonCode(), aggregate.remark(), aggregate.sourceVersion(), aggregate.version(), null);
    }

    /**
     * 处理当前类型职责中的操作 {@code line}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param orderId 业务或技术标识，类型为 {@code long}
     * @param value 业务处理参数或成员，类型为 {@code PoConfirmLine}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PoConfirmMapper.Line}
     */
    private PoConfirmMapper.Line line(long orderId, PoConfirmLine value) {
        return new PoConfirmMapper.Line(value.lineId(), orderId, value.skuCode(), value.orderQty(), value.confirmedQty(), value.requestedDate(), value.confirmedDate(), value.status(), value.diffReason());
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param head 业务处理参数或成员，类型为 {@code PoConfirmMapper.Head}
     * @param withLines 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PoConfirmView}
     */
    private PoConfirmView view(PoConfirmMapper.Head head, boolean withLines) {
        var status = PoConfirmStatus.fromCode(head.confirmStatus());
        var lines = withLines ? mapper.lines(head.orderId()).stream().map(value -> new PoConfirmView.Line(value.orderLineId(), value.skuCode(), value.orderQty(), value.confirmedQty(), value.requestedDeliveryDate(), value.confirmedDeliveryDate(), value.lineStatus(), value.diffReason())).toList() : List.<PoConfirmView.Line>of();
        return new PoConfirmView(head.orderId(), head.confirmNo(), head.purchaseOrderId(), head.purchaseOrderNo(), head.supplierId(), status.code(), status.label(), head.confirmDeadline(), head.confirmedAt(), head.diffType(), head.reasonCode(), head.remark(), head.version(), head.updatedAt(), lines);
    }
}
