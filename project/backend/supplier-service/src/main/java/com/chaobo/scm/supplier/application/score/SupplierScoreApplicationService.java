package com.chaobo.scm.supplier.application.score;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.shared.*;
import com.chaobo.scm.supplier.infrastructure.persistence.score.SupplierScoreMapper;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;

/**
 * SupplierScoreApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierScoreApplicationService {

    /**
     * mapper（类型：{@code SupplierScoreMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierScoreMapper mapper;

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids;

    /**
     * json（类型：{@code ObjectMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ObjectMapper json;

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
     * 创建 SupplierScoreApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code SupplierScoreMapper}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param json 业务处理参数或成员，类型为 {@code ObjectMapper}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     */
    public SupplierScoreApplicationService(SupplierScoreMapper mapper, IdentifierGenerator ids, ObjectMapper json, OutboxRepository outbox, AuditLogRepository audit) {
        this.mapper = mapper;
        this.ids = ids;
        this.json = json;
        this.outbox = outbox;
        this.audit = audit;
    }

    /**
     * 处理当前类型职责中的操作 {@code collectFact}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param e 业务处理参数或成员，类型为 {@code PerformanceFactEvent}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean collectFact(PerformanceFactEvent e) {
        var metric = metric(e.eventType());
        if (e.supplierId() <= 0 || e.metricValue() == null || e.occurredAt() == null) {
            throw rule("绩效事实数据不完整");
        }
        return mapper.insertFact(ids.nextId(), e.eventCode(), e.supplierId(), metric.dimension(), metric.metric(), e.metricValue(), e.occurredAt(), e.sourceSystem(), e.sourceNo(), write(e.payload() == null ? Map.of() : e.payload())) == 1;
    }

    /**
     * 执行命令 {@code createRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param dimension 业务处理参数或成员，类型为 {@code String}
     * @param metric 业务处理参数或成员，类型为 {@code String}
     * @param weight 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param target 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param direction 业务处理参数或成员，类型为 {@code int}
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code long}
     */
    @Transactional(rollbackFor = Exception.class)
    public long createRule(String name, String dimension, String metric, BigDecimal weight, BigDecimal target, int direction, LocalDate from, LocalDate to, CommandContext c) {
        c.requirePermission("supplier:score-rule:create");
        boolean nameMissing = name == null || name.isBlank();
        boolean dimensionMissing = dimension == null || dimension.isBlank();
        boolean metricMissing = metric == null || metric.isBlank();
        if (nameMissing || dimensionMissing || metricMissing) {
            throw rule("评分规则不合法");
        }
        boolean weightInvalid = weight == null || weight.signum() <= 0 || weight.compareTo(BigDecimal.ONE) > 0;
        boolean targetInvalid = target == null || target.signum() <= 0;
        if (weightInvalid || targetInvalid) {
            throw rule("评分规则不合法");
        }
        boolean directionInvalid = direction < 1 || direction > PROCESS_RISK_VALUE_2;
        boolean periodInvalid = from == null || (to != null && to.isBefore(from));
        if (directionInvalid || periodInvalid) {
            throw rule("评分规则不合法");
        }
        long id = ids.nextId();
        mapper.insertRule(id, name, dimension, metric, weight, target, direction, from, to, c.operatorId());
        audit.save(c, "CREATE_SCORE_RULE", "SCORE_RULE", id, name, null, "{\"status\":1}");
        return id;
    }

    /**
     * 执行命令 {@code publishRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishRule(long id, int version, CommandContext c) {
        c.requirePermission("supplier:score-rule:publish");
        if (mapper.publishRule(id, version, c.operatorId()) != 1) {
            throw conflict();
        }
        audit.save(c, "PUBLISH_SCORE_RULE", "SCORE_RULE", id, String.valueOf(id), null, "{\"status\":2}");
    }

    /**
     * 执行命令 {@code disableRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void disableRule(long id, int version, CommandContext c) {
        c.requirePermission("supplier:score-rule:disable");
        if (mapper.disableRule(id, version, c.operatorId()) != 1) {
            throw conflict();
        }
        audit.save(c, "DISABLE_SCORE_RULE", "SCORE_RULE", id, String.valueOf(id), null, "{\"status\":3}");
    }

    /**
     * 处理当前类型职责中的操作 {@code rules}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<ScoreViews.Rule>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<ScoreViews.Rule> rules() {
        return mapper.rules();
    }

    /** 查询当前供应商数据范围内的评分结果。 */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<ScoreViews.Result> results(Long scope) {
        return mapper.results(scope, 100);
    }

    /**
     * 处理当前类型职责中的操作 {@code calculate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param period 业务处理参数或成员，类型为 {@code YearMonth}
     * @param adjustment 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ScoreViews.Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public ScoreViews.Result calculate(long supplierId, YearMonth period, BigDecimal adjustment, String reason, CommandContext c) {
        c.requirePermission("supplier:score:calculate");
        c.requireSupplierScope(supplierId);
        adjustment = adjustment == null ? BigDecimal.ZERO : adjustment;
        boolean adjustmentReasonMissing = reason == null || reason.isBlank();
        if (adjustment.signum() != 0 && adjustmentReasonMissing) {
            throw rule("人工修正必须说明原因");
        }
        OffsetDateTime from = period.atDay(1).atStartOfDay().atOffset(ZoneOffset.ofHours(8)), to = period.plusMonths(1).atDay(1).atStartOfDay().atOffset(ZoneOffset.ofHours(8));
        var ruleRows = mapper.effectiveRules(period.atEndOfMonth());
        var facts = mapper.facts(supplierId, from, to);
        var result = SupplierScoringDomainService.calculate(ruleRows.stream().map(r -> new SupplierScoringDomainService.Rule(r.dimensionCode(), r.metricCode(), r.weight(), r.targetValue(), r.direction())).toList(), facts.stream().map(f -> new SupplierScoringDomainService.Fact(f.metricCode(), f.metricValue())).toList(), adjustment);
        String periodCode = period.toString();
        mapper.upsertResult(ids.nextId(), supplierId, periodCode, result.total(), write(result.dimensions()), write(Map.of("factCount", facts.size(), "eventCodes", facts.stream().map(ScoreViews.Fact::eventCode).toList())), adjustment, reason);
        var saved = mapper.resultByPeriod(supplierId, periodCode);
        audit.save(c, "CALCULATE_SUPPLIER_SCORE", "SCORE_RESULT", saved.id(), periodCode, null, "{\"totalScore\":" + saved.totalScore() + "}");
        return saved;
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code ScoreViews.Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public ScoreViews.Result publish(long id, int version, CommandContext c) {
        c.requirePermission("supplier:score:publish");
        var row = mapper.result(id);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评分结果不存在");
        }
        c.requireSupplierScope(row.supplierId());
        if (mapper.publishResult(id, version) != 1) {
            throw conflict();
        }
        if (row.totalScore().compareTo(new BigDecimal(PUBLISH_TEXT_75)) < 0) {
            boolean high = row.totalScore().compareTo(new BigDecimal("60")) < 0;
            mapper.insertRisk(ids.nextId(), row.supplierId(), id, high ? 3 : 2, high ? 3 : 1, "供应商周期评分" + row.totalScore() + "分，建议" + (high ? "进行冻结审批" : "发起整改"));
        }
        long eventId = ids.nextId();
        outbox.saveAll(List.of(new DomainEvent(eventId, "SUP-" + eventId, "SupplierScorePublished", "供应商评分已发布", "SUPPLIER_SCORE", id, row.periodCode(), version + 1, c.operatorId(), OffsetDateTime.now(), Map.of("supplierId", row.supplierId(), "periodCode", row.periodCode(), "totalScore", row.totalScore()))));
        audit.save(c, "PUBLISH_SUPPLIER_SCORE", "SCORE_RESULT", id, row.periodCode(), "{\"status\":1}", "{\"status\":2}");
        return mapper.result(id);
    }

    /**
     * 处理当前类型职责中的操作 {@code result}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param period 业务处理参数或成员，类型为 {@code String}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ScoreViews.Result}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ScoreViews.Result result(long supplierId, String period, Long scope) {
        if (scope != null && scope != supplierId) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评分结果不存在");
        }
        var r = mapper.resultByPeriod(supplierId, period);
        if (r == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评分结果不存在");
        }
        return r;
    }

    /**
     * 处理当前类型职责中的操作 {@code risks}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<ScoreViews.Risk>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<ScoreViews.Risk> risks(long supplierId, Long scope) {
        if (scope != null && scope != supplierId) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "风险建议不存在");
        }
        return mapper.risks(supplierId);
    }

    /**
     * 处理当前类型职责中的操作 {@code processRisk}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param accepted 业务处理参数或成员，类型为 {@code boolean}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void processRisk(long id, int version, boolean accepted, CommandContext c) {
        c.requirePermission("supplier:risk:process");
        if (mapper.processRisk(id, version, accepted ? PROCESS_RISK_VALUE_2 : PROCESS_RISK_VALUE_3) != 1) {
            throw conflict();
        }
        audit.save(c, accepted ? "ACCEPT_RISK_RECOMMENDATION" : "REJECT_RISK_RECOMMENDATION", "RISK_RECOMMENDATION", id, String.valueOf(id), null, "{\"status\":" + (accepted ? 2 : 3) + "}");
    }

    /**
     * 处理当前类型职责中的操作 {@code metric}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Metric}
     */
    private Metric metric(String type) {
        return switch(type) {
            case "WmsQualityInspectionCompleted" ->
                new Metric("QUALITY", "PASS_RATE");
            case "TmsShipmentDelivered" ->
                new Metric("DELIVERY", "ON_TIME_RATE");
            case "TmsShipmentDelayed" ->
                new Metric("DELIVERY", "DELAY_RATE");
            case "PurchasePriceVarianceCalculated" ->
                new Metric("PRICE", "PRICE_VARIANCE_RATE");
            case "PurchaseOrderConfirmed" ->
                new Metric("RESPONSE", "CONFIRM_ON_TIME_RATE");
            case "SupplierReturnClosed" ->
                new Metric("QUALITY", "RETURN_RATE");
            case "BmsReconciliationClosed" ->
                new Metric("FINANCE", "RECONCILIATION_ACCURACY");
            default ->
                throw rule("不支持的绩效事实类型: " + type);
        };
    }

    /**
     * 处理当前类型职责中的操作 {@code write}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param v 业务处理参数或成员，类型为 {@code Object}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String write(Object v) {
        try {
            return json.writeValueAsString(v);
        } catch (JacksonException e) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "评分数据序列化失败");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code rule}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param m 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private static BusinessException rule(String m) {
        return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, m);
    }

    /**
     * 处理当前类型职责中的操作 {@code conflict}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private static BusinessException conflict() {
        return new BusinessException(ErrorCode.VERSION_CONFLICT, "评分数据状态或版本已变更");
    }

    /**
     * Metric。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record Metric(String dimension, String metric) {
    }

    /**
     * 业务常量 {@code PROCESS_RISK_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PROCESS_RISK_VALUE_2 = 2;

    /**
     * 业务常量 {@code PROCESS_RISK_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PROCESS_RISK_VALUE_3 = 3;

    /**
     * 业务常量 {@code PUBLISH_TEXT_75}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String PUBLISH_TEXT_75 = "75";
}
