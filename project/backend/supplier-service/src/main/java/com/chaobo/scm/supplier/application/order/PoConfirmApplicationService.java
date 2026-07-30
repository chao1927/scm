package com.chaobo.scm.supplier.application.order;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.common.integration.WmsCollaborationApi;
import com.chaobo.scm.supplier.application.integration.IntegrationCommandEnqueuer;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.asn.*;
import com.chaobo.scm.supplier.domain.order.*;
import com.chaobo.scm.supplier.domain.shared.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * PoConfirmApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class PoConfirmApplicationService {

    /**
     * repo（类型：{@code PoConfirmRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PoConfirmRepository repo;

    /**
     * read（类型：{@code PoConfirmReadModelPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PoConfirmReadModelPort read;

    /**
     * outbox（类型：{@code OutboxRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final OutboxRepository outbox;

    /**
     * audit（类型：{@code AuditLogRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AuditLogRepository audit;

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids;

    /**
     * executor（类型：{@code TransactionalCommandExecutor}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final TransactionalCommandExecutor executor;

    /**
     * asns（类型：{@code AsnRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AsnRepository asns;

    /**
     * integrations（类型：{@code IntegrationCommandEnqueuer}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final IntegrationCommandEnqueuer integrations;

    /**
     * 创建 PoConfirmApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repo 业务处理参数或成员，类型为 {@code PoConfirmRepository}
     * @param read 业务处理参数或成员，类型为 {@code PoConfirmReadModelPort}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param executor 业务处理参数或成员，类型为 {@code TransactionalCommandExecutor}
     * @param asns 业务处理参数或成员，类型为 {@code AsnRepository}
     * @param integrations 业务处理参数或成员，类型为 {@code IntegrationCommandEnqueuer}
     */
    public PoConfirmApplicationService(PoConfirmRepository repo, PoConfirmReadModelPort read, OutboxRepository outbox, AuditLogRepository audit, IdentifierGenerator ids, TransactionalCommandExecutor executor, AsnRepository asns, IntegrationCommandEnqueuer integrations) {
        this.repo = repo;
        this.read = read;
        this.outbox = outbox;
        this.audit = audit;
        this.ids = ids;
        this.executor = executor;
        this.asns = asns;
        this.integrations = integrations;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<PoConfirmView>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<PoConfirmView> page(Long supplierId, Long scope, Integer status, String keyword, int pageNo, int pageSize) {
        if (pageNo < 1 || pageSize < 1 || pageSize > PAGE_VALUE_100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        return read.page(scope == null ? supplierId : scope, status, keyword, pageNo, pageSize);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PoConfirmView}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PoConfirmView detail(long id, Long scope) {
        var v = read.detail(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购订单确认不存在"));
        if (scope != null && scope != v.supplierId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "采购订单确认不存在");
        }
        return v;
    }

    /**
     * 处理当前类型职责中的操作 {@code detailByPurchaseOrder}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param purchaseOrderId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public CommandResult detailByPurchaseOrder(long purchaseOrderId) {
        var aggregate = repo.findByPurchaseOrderId(purchaseOrderId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购订单确认不存在"));
        return result(aggregate, null, true);
    }

    /**
     * 处理当前类型职责中的操作 {@code receive}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param poId 业务或技术标识，类型为 {@code long}
     * @param poNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param deadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param lines 业务处理参数或成员，类型为 {@code List<PoConfirmAggregate.NewLine>}
     * @param sourceVersion 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult receive(long poId, String poNo, long supplierId, OffsetDateTime deadline, List<PoConfirmAggregate.NewLine> lines, int sourceVersion, CommandContext c) {
        c.requirePermission("supplier:openapi:purchase_order:receive");
        var request =
                new ReceiveRequest(poId, poNo, supplierId, deadline, lines, sourceVersion);
        return executor.execute("supplier:po", c, request, () -> repo.findByPurchaseOrderId(poId).map(aggregate -> {
            if (sourceVersion <= aggregate.sourceVersion()) {
                return result(aggregate, null, true);
            }
            if (aggregate.supplierId() != supplierId) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "订单变更不能更换供应商");
            }
            String before = snapshot(aggregate);
            aggregate.applyOrderChange(deadline, lines, sourceVersion, c.operatorId(), ids);
            return persist(aggregate, c, "CHANGE_PURCHASE_ORDER", before);
        }).orElseGet(() -> persist(PoConfirmAggregate.receive(poId, poNo, supplierId, deadline, lines, sourceVersion, c.operatorId(), ids), c, "RECEIVE_PURCHASE_ORDER", null)));
    }

    /**
     * 处理当前类型职责中的操作 {@code receive}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param poId 业务或技术标识，类型为 {@code long}
     * @param poNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param deadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param lines 业务处理参数或成员，类型为 {@code List<PoConfirmAggregate.NewLine>}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult receive(long poId, String poNo, long supplierId, OffsetDateTime deadline, List<PoConfirmAggregate.NewLine> lines, CommandContext c) {
        return receive(poId, poNo, supplierId, deadline, lines, 1, c);
    }

    /**
     * 执行命令 {@code cancelByPurchase}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param poId 业务或技术标识，类型为 {@code long}
     * @param sourceVersion 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult cancelByPurchase(long poId, int sourceVersion, String reason, CommandContext c) {
        c.requirePermission("supplier:openapi:purchase_order:receive");
        var aggregate = repo.findByPurchaseOrderId(poId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购订单确认不存在"));
        if (sourceVersion <= aggregate.sourceVersion()) {
            return result(aggregate, null, true);
        }
        String before = snapshot(aggregate);
        aggregate.cancel(sourceVersion, reason, c.operatorId(), ids);
        return persist(aggregate, c, "CANCEL_PURCHASE_ORDER", before);
    }

    /**
     * 执行命令 {@code closeByPurchase}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param poId 业务或技术标识，类型为 {@code long}
     * @param sourceVersion 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult closeByPurchase(long poId, int sourceVersion, CommandContext c) {
        c.requirePermission("supplier:openapi:purchase_order:receive");
        var aggregate = repo.findByPurchaseOrderId(poId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购订单确认不存在"));
        if (sourceVersion <= aggregate.sourceVersion()) {
            return result(aggregate, null, true);
        }
        String before = snapshot(aggregate);
        aggregate.close(sourceVersion, c.operatorId(), ids);
        return persist(aggregate, c, "CLOSE_PURCHASE_ORDER", before);
    }

    /**
     * 执行命令 {@code confirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param lines 业务处理参数或成员，类型为 {@code List<PoConfirmAggregate.LineDecision>}
     * @param remark 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult confirm(long id, int version, List<PoConfirmAggregate.LineDecision> lines, String remark, CommandContext c) {
        c.requirePermission("supplier:purchase_order:confirm");
        return change(id, version, new ConfirmR(id, version, lines, remark), c, "CONFIRM_PURCHASE_ORDER", a -> a.confirm(lines, remark, c.operatorId(), ids));
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code int}
     * @param remark 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult reject(long id, int version, int reason, String remark, CommandContext c) {
        c.requirePermission("supplier:purchase_order:reject");
        return change(id, version, new RejectR(id, version, reason, remark), c, "REJECT_PURCHASE_ORDER", a -> a.reject(reason, remark, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code difference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param type 业务处理参数或成员，类型为 {@code int}
     * @param lines 业务处理参数或成员，类型为 {@code List<PoConfirmAggregate.LineDifference>}
     * @param remark 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult difference(long id, int version, int type, List<PoConfirmAggregate.LineDifference> lines, String remark, CommandContext c) {
        c.requirePermission("supplier:purchase_order:feedback_diff");
        return change(id, version, new DiffR(id, version, type, lines, remark), c, "FEEDBACK_PURCHASE_ORDER_DIFF", a -> a.feedbackDifference(type, lines, remark, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code changeDelivery}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param lineId 业务或技术标识，类型为 {@code long}
     * @param date 业务时间，类型为 {@code LocalDate}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult changeDelivery(long id, int version, long lineId, LocalDate date, String reason, CommandContext c) {
        c.requirePermission("supplier:purchase_order:change_delivery");
        return change(id, version, new DeliveryR(id, version, lineId, date, reason), c, "CHANGE_PROMISED_DELIVERY", a -> a.changeDelivery(lineId, date, reason, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code Object}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @param op 业务处理参数或成员，类型为 {@code String}
     * @param action 业务处理参数或成员，类型为 {@code Consumer<PoConfirmAggregate>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult change(long id, int version, Object request, CommandContext c, String op, Consumer<PoConfirmAggregate> action) {
        return executor.execute("supplier:po", c, request, () -> {
            var a = repo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购订单确认不存在"));
            c.requireSupplierScope(a.supplierId());
            if (a.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "采购订单确认版本已变化");
            }
            String before = snapshot(a);
            action.accept(a);
            return persist(a, c, op, before);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code persist}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code PoConfirmAggregate}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @param op 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult persist(PoConfirmAggregate a, CommandContext c, String op, String before) {
        repo.save(a, c.operatorId());
        var events = a.pullEvents();
        outbox.saveAll(events);
        if (CHANGE_PURCHASE_ORDER.equals(op) || CANCEL_PURCHASE_ORDER.equals(op)) {
            coordinateAsns(a, c, op);
        }
        audit.save(c, op, "PO_CONFIRM", a.confirmId(), a.confirmNo(), before, snapshot(a));
        String event = events.isEmpty() ? null : events.get(events.size() - 1).eventCode();
        return result(a, event, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code coordinateAsns}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param order 业务处理参数或成员，类型为 {@code PoConfirmAggregate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     */
    private void coordinateAsns(PoConfirmAggregate order, CommandContext context, String operation) {
        String reason = "CHANGE_PURCHASE_ORDER".equals(operation) ? "采购订单变更，原ASN需重建" : "采购订单已取消";
        for (var asn : asns.findByPurchaseOrderId(order.purchaseOrderId())) {
            if (asn.status() == AsnStatus.CANCELLED || asn.status() == AsnStatus.CLOSED) {
                continue;
            }
            if (asn.status() == AsnStatus.SHIPPED || asn.status() == AsnStatus.ARRIVED || asn.status() == AsnStatus.RECEIVED) {
                throw new BusinessException(ErrorCode.STATE_CONFLICT, "采购订单已有发货或到仓ASN，需人工处理: " + asn.asnNo());
            }
            var previous = asn.status();
            String before = snapshotAsn(asn);
            asn.cancel(reason, context.operatorId(), ids);
            asns.save(asn, context.operatorId());
            outbox.saveAll(asn.pullEvents());
            audit.save(context, "CANCEL_ASN_BY_PURCHASE_ORDER", "ASN", asn.asnId(), asn.asnNo(), before, snapshotAsn(asn));
            if (previous == AsnStatus.SUBMITTED || previous == AsnStatus.APPOINTED) {
                integrations.enqueue("WMS_CANCEL_APPOINTMENT", "ASN", asn.asnId(), asn.version(), "WMS", new WmsCollaborationApi.CancelAppointmentCommand("ASN-CANCEL-" + asn.asnId() + "-" + asn.version(), asn.asnId(), reason));
            }
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshotAsn}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code AsnAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshotAsn(AsnAggregate a) {
        return "{\"asnNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(a.asnNo(), a.status().code(), a.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code result}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code PoConfirmAggregate}
     * @param event 业务处理参数或成员，类型为 {@code String}
     * @param hit 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult result(PoConfirmAggregate a, String event, boolean hit) {
        return new CommandResult(a.confirmId(), a.confirmNo(), a.status().code(), a.status().label(), a.version(), event, hit);
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code PoConfirmAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(PoConfirmAggregate a) {
        return "{\"confirmNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(a.confirmNo(), a.status().code(), a.version());
    }

    /**
     * 接收采购订单或订单变更时使用的幂等请求快照。
     *
     * <p>携带采购订单事实源版本，确保供应商上下文能够识别重复事件和拒绝旧版本覆盖。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record ReceiveRequest(
            long poId,
            String poNo,
            long supplierId,
            OffsetDateTime deadline,
            List<PoConfirmAggregate.NewLine> lines,
            int sourceVersion) {
    }

    /**
     * ConfirmR。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record ConfirmR(long id, int version, List<PoConfirmAggregate.LineDecision> lines, String remark) {
    }

    /**
     * RejectR。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record RejectR(long id, int version, int reason, String remark) {
    }

    /**
     * DiffR。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record DiffR(long id, int version, int type, List<PoConfirmAggregate.LineDifference> lines, String remark) {
    }

    /**
     * DeliveryR。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record DeliveryR(long id, int version, long lineId, LocalDate date, String reason) {
    }

    /**
     * 业务常量 {@code CANCEL_PURCHASE_ORDER}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String CANCEL_PURCHASE_ORDER = "CANCEL_PURCHASE_ORDER";

    /**
     * 业务常量 {@code CHANGE_PURCHASE_ORDER}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String CHANGE_PURCHASE_ORDER = "CHANGE_PURCHASE_ORDER";

    /**
     * 业务常量 {@code PAGE_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PAGE_VALUE_100 = 100;
}
