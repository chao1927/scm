package com.chaobo.scm.bms.application;

import com.chaobo.scm.bms.domain.BmsDomain;
import com.chaobo.scm.bms.infrastructure.persistence.BmsMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * BmsApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class BmsApplicationService {

    /**
     * mapper（类型：{@code BmsMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final BmsMapper mapper;

    /** 用于把 RocketMQ 业务载荷转换为费用采集命令。 */
    private final ObjectMapper objectMapper;

    /**
     * objectSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong objectSequence = new AtomicLong(100000);

    /**
     * ruleSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong ruleSequence = new AtomicLong(200000);

    /**
     * sourceSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong sourceSequence = new AtomicLong(300000);

    /**
     * chargeSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong chargeSequence = new AtomicLong(400000);

    /**
     * adjustmentSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong adjustmentSequence = new AtomicLong(500000);

    /**
     * reconciliationSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong reconciliationSequence = new AtomicLong(600000);

    /**
     * billSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong billSequence = new AtomicLong(700000);

    /**
     * invoiceSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong invoiceSequence = new AtomicLong(800000);

    /**
     * financeSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong financeSequence = new AtomicLong(900000);

    /**
     * refundSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong refundSequence = new AtomicLong(1000000);

    /**
     * eventSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong eventSequence = new AtomicLong(1100000);

    /**
     * inboxSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong inboxSequence = new AtomicLong(1200000);

    /**
     * 创建 BmsApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code BmsMapper}
     */
    public BmsApplicationService(BmsMapper mapper) {
        this(mapper, new ObjectMapper());
    }

    /**
     * 生产环境构造器，复用 Spring 统一配置的 JSON 映射规则。
     *
     * @param mapper BMS 持久化端口
     * @param objectMapper JSON 映射器
     */
    @Autowired
    public BmsApplicationService(BmsMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行命令 {@code createBillingObject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateBillingObjectCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.BillingObjectRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.BillingObjectRow createBillingObject(CreateBillingObjectCommand command) {
        String objectCode = command.objectCode() == null || command.objectCode().isBlank() ? "BO" + objectSequence.incrementAndGet() : command.objectCode();
        BmsDomain.BillingObjectAggregate aggregate = BmsDomain.BillingObjectAggregate.create(objectCode, command.objectName(), command.objectType(), command.direction(), command.currency());
        BmsMapper.BillingObjectRow row = toRow(aggregate);
        mapper.insertBillingObject(row);
        outbox("BillingObjectCreated", row.objectCode(), row.objectCode(), "{}");
        log("CREATE_BILLING_OBJECT", row.objectCode(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 查询并返回 {@code listBillingObjects}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code List<BmsMapper.BillingObjectRow>}
     */
    public List<BmsMapper.BillingObjectRow> listBillingObjects(Integer status) {
        return mapper.listBillingObjects(status);
    }

    /**
     * 执行命令 {@code enableBillingObject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param objectCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code VersionCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.BillingObjectRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.BillingObjectRow enableBillingObject(String objectCode, VersionCommand command) {
        BmsDomain.BillingObjectAggregate aggregate = loadBillingObject(objectCode);
        aggregate.enable(command.expectedVersion());
        mapper.updateBillingObject(toRow(aggregate));
        outbox("BillingObjectEnabled", objectCode, objectCode, "{}");
        log("ENABLE_BILLING_OBJECT", objectCode, command.operatorId(), command.idempotencyKey());
        return mapper.findBillingObject(objectCode);
    }

    /**
     * 执行命令 {@code disableBillingObject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param objectCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code VersionCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.BillingObjectRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.BillingObjectRow disableBillingObject(String objectCode, VersionCommand command) {
        BmsDomain.BillingObjectAggregate aggregate = loadBillingObject(objectCode);
        aggregate.disable(command.expectedVersion());
        mapper.updateBillingObject(toRow(aggregate));
        outbox("BillingObjectDisabled", objectCode, objectCode, "{}");
        log("DISABLE_BILLING_OBJECT", objectCode, command.operatorId(), command.idempotencyKey());
        return mapper.findBillingObject(objectCode);
    }

    /**
     * 执行命令 {@code createBillingRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateBillingRuleCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.BillingRuleRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.BillingRuleRow createBillingRule(CreateBillingRuleCommand command) {
        BmsDomain.BillingObjectAggregate object = loadBillingObject(command.objectCode());
        object.ensureEnabled();
        BmsDomain.BillingRuleAggregate aggregate = BmsDomain.BillingRuleAggregate.create("BR" + ruleSequence.incrementAndGet(), command.objectCode(), command.feeType(), command.unitPrice(), command.taxRate(), command.effectiveFrom(), command.effectiveTo());
        BmsMapper.BillingRuleRow row = toRow(aggregate);
        mapper.insertBillingRule(row);
        log("CREATE_BILLING_RULE", row.ruleNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 执行命令 {@code publishBillingRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ruleNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code VersionCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.BillingRuleRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.BillingRuleRow publishBillingRule(String ruleNo, VersionCommand command) {
        BmsDomain.BillingRuleAggregate aggregate = loadBillingRule(ruleNo);
        if (mapper.countPublishedRuleOverlap(aggregate.objectCode(), aggregate.feeType(), aggregate.effectiveFrom(), aggregate.effectiveTo()) > 0) {
            throw new IllegalStateException("published billing rule effective range overlaps");
        }
        aggregate.publish(command.expectedVersion());
        mapper.updateBillingRule(toRow(aggregate));
        outbox("BillingRulePublished", ruleNo, aggregate.objectCode(), "{\"ruleNo\":\"" + ruleNo + "\"}");
        log("PUBLISH_BILLING_RULE", ruleNo, command.operatorId(), command.idempotencyKey());
        return mapper.findBillingRule(ruleNo);
    }

    /**
     * 查询并返回 {@code listBillingRules}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param objectCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code List<BmsMapper.BillingRuleRow>}
     */
    public List<BmsMapper.BillingRuleRow> listBillingRules(String objectCode) {
        return mapper.listBillingRules(objectCode);
    }

    /**
     * 处理当前类型职责中的操作 {@code collectChargeSource}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CollectChargeSourceCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.ChargeSourceRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.ChargeSourceRow collectChargeSource(CollectChargeSourceCommand command) {
        BmsMapper.ChargeSourceRow existing = mapper.findChargeSourceByIdempotency(command.sourceSystem(), command.idempotencyKey());
        if (existing != null) {
            return existing;
        }
        BmsDomain.ChargeSourceAggregate source = BmsDomain.ChargeSourceAggregate.create("CS" + sourceSequence.incrementAndGet(), command.sourceSystem(), command.sourceEventId(), command.billingObjectCode(), command.feeType(), command.quantity());
        BmsMapper.ChargeSourceRow row = toRow(source, command.idempotencyKey(), command.billingPeriod(), command.payload());
        try {
            calculateSource(source, command.billingPeriod());
            row = toRow(source, command.idempotencyKey(), command.billingPeriod(), command.payload());
        } catch (RuntimeException ex) {
            source.fail(ex.getMessage());
            row = toRow(source, command.idempotencyKey(), command.billingPeriod(), command.payload());
            mapper.insertChargeSource(row);
            outbox("ChargeSourceFailed", row.sourceNo(), row.billingObjectCode(), "{\"reason\":\"" + sanitize(ex.getMessage()) + "\"}");
            log("COLLECT_CHARGE_SOURCE_FAILED", row.sourceNo(), command.operatorId(), command.idempotencyKey());
            return row;
        }
        mapper.insertChargeSource(row);
        BmsMapper.ChargeDetailRow detail = createChargeDetail(source, command.billingPeriod());
        mapper.insertChargeDetail(detail);
        outbox("ChargeSourceAccepted", row.sourceNo(), row.billingObjectCode(), "{}");
        outbox("ChargeCalculated", detail.chargeNo(), detail.objectCode(), "{}");
        log("COLLECT_CHARGE_SOURCE", row.sourceNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 查询并返回 {@code listChargeSources}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code List<BmsMapper.ChargeSourceRow>}
     */
    public List<BmsMapper.ChargeSourceRow> listChargeSources(Integer status) {
        return mapper.listChargeSources(status);
    }

    /**
     * 执行命令 {@code replayChargeSource}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ReplayCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.ChargeSourceRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.ChargeSourceRow replayChargeSource(String sourceNo, ReplayCommand command) {
        BmsMapper.ChargeSourceRow row = require(mapper.findChargeSource(sourceNo), "charge source not found");
        BmsDomain.ChargeSourceAggregate source = BmsDomain.ChargeSourceAggregate.restore(row.sourceNo(), row.sourceSystem(), row.sourceEventId(), row.billingObjectCode(), row.feeType(), row.quantity(), row.status(), row.failureReason(), row.version());
        source.replay();
        try {
            calculateSource(source, row.billingPeriod());
            mapper.updateChargeSource(toRow(source, row.idempotencyKey(), row.billingPeriod(), row.payload()));
            if (mapper.findChargeBySource(source.sourceNo()) == null) {
                mapper.insertChargeDetail(createChargeDetail(source, row.billingPeriod()));
            }
            outbox("ChargeSourceAccepted", source.sourceNo(), source.billingObjectCode(), "{}");
        } catch (RuntimeException ex) {
            source.fail(ex.getMessage());
            mapper.updateChargeSource(toRow(source, row.idempotencyKey(), row.billingPeriod(), row.payload()));
            outbox("ChargeSourceFailed", source.sourceNo(), source.billingObjectCode(), "{\"reason\":\"" + sanitize(ex.getMessage()) + "\"}");
        }
        log("REPLAY_CHARGE_SOURCE", sourceNo, command.operatorId(), command.idempotencyKey());
        return mapper.findChargeSource(sourceNo);
    }

    /**
     * 查询并返回 {@code listCharges}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param objectCode 可追踪业务编码，类型为 {@code String}
     * @param billingPeriod 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code List<BmsMapper.ChargeDetailRow>}
     */
    public List<BmsMapper.ChargeDetailRow> listCharges(String objectCode, String billingPeriod, Integer status) {
        return mapper.listCharges(objectCode, billingPeriod, status);
    }

    /**
     * 处理当前类型职责中的操作 {@code recalculateCharge}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param chargeNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code RecalculateChargeCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.ChargeDetailRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.ChargeDetailRow recalculateCharge(String chargeNo, RecalculateChargeCommand command) {
        BmsMapper.ChargeDetailRow row = require(mapper.findChargeDetail(chargeNo), "charge detail not found");
        BmsMapper.BillingRuleRow rule = require(mapper.findBillingRule(row.ruleNo()), "billing rule not found");
        BmsDomain.BillingRuleAggregate ruleAggregate = restoreRule(rule);
        BigDecimal quantity = command.quantity() == null ? row.quantity() : command.quantity();
        BmsDomain.ChargeDetailAggregate aggregate = restoreCharge(row);
        aggregate.recalculate(quantity, rule.unitPrice(), ruleAggregate.calculate(quantity), command.expectedVersion());
        mapper.updateChargeDetail(toRow(aggregate, row.billingPeriod()));
        outbox("ChargeRecalculated", chargeNo, row.objectCode(), "{}");
        log("RECALCULATE_CHARGE", chargeNo, command.operatorId(), command.idempotencyKey());
        return mapper.findChargeDetail(chargeNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code voidCharge}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param chargeNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code VersionCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.ChargeDetailRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.ChargeDetailRow voidCharge(String chargeNo, VersionCommand command) {
        BmsMapper.ChargeDetailRow row = require(mapper.findChargeDetail(chargeNo), "charge detail not found");
        BmsDomain.ChargeDetailAggregate aggregate = restoreCharge(row);
        aggregate.voidCharge(command.expectedVersion());
        mapper.updateChargeDetail(toRow(aggregate, row.billingPeriod()));
        outbox("ChargeVoided", chargeNo, row.objectCode(), "{}");
        log("VOID_CHARGE", chargeNo, command.operatorId(), command.idempotencyKey());
        return mapper.findChargeDetail(chargeNo);
    }

    /**
     * 执行命令 {@code createAdjustment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateAdjustmentCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.AdjustmentRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.AdjustmentRow createAdjustment(CreateAdjustmentCommand command) {
        BmsMapper.ChargeDetailRow original = require(mapper.findChargeDetail(command.originalChargeNo()), "original charge detail not found");
        BmsDomain.ChargeAdjustmentAggregate aggregate = BmsDomain.ChargeAdjustmentAggregate.create("BA" + adjustmentSequence.incrementAndGet(), original.chargeNo(), command.adjustAmount(), command.approved());
        BmsMapper.AdjustmentRow row = toRow(aggregate, command.reason());
        mapper.insertAdjustment(row);
        log("CREATE_CHARGE_ADJUSTMENT", row.adjustmentNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code executeAdjustment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param adjustmentNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code VersionCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.AdjustmentRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.AdjustmentRow executeAdjustment(String adjustmentNo, VersionCommand command) {
        BmsMapper.AdjustmentRow row = require(mapper.findAdjustment(adjustmentNo), "adjustment not found");
        BmsDomain.ChargeAdjustmentAggregate aggregate = BmsDomain.ChargeAdjustmentAggregate.restore(row.adjustmentNo(), row.originalChargeNo(), row.adjustAmount(), row.status(), row.version());
        aggregate.execute(command.expectedVersion());
        mapper.updateAdjustment(toRow(aggregate, row.reason()));
        BmsMapper.ChargeDetailRow original = require(mapper.findChargeDetail(row.originalChargeNo()), "original charge detail not found");
        BmsMapper.ChargeDetailRow adjustmentCharge = new BmsMapper.ChargeDetailRow(null, "CD" + chargeSequence.incrementAndGet(), "ADJ-" + adjustmentNo, original.objectCode(), original.feeType(), original.ruleNo(), BigDecimal.ONE, row.adjustAmount(), row.adjustAmount(), BigDecimal.ZERO.setScale(2), row.adjustAmount(), original.billingPeriod(), BmsDomain.ChargeDetailAggregate.PENDING_RECONCILIATION, 1);
        mapper.insertChargeDetail(adjustmentCharge);
        outbox("ChargeAdjusted", adjustmentNo, original.objectCode(), "{}");
        log("EXECUTE_CHARGE_ADJUSTMENT", adjustmentNo, command.operatorId(), command.idempotencyKey());
        return mapper.findAdjustment(adjustmentNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code generateReconciliation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code GenerateReconciliationCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.ReconciliationRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.ReconciliationRow generateReconciliation(GenerateReconciliationCommand command) {
        List<BmsMapper.ChargeDetailRow> charges = mapper.listCharges(command.objectCode(), command.billingPeriod(), BmsDomain.ChargeDetailAggregate.PENDING_RECONCILIATION);
        BigDecimal total = charges.stream().map(BmsMapper.ChargeDetailRow::totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (charges.isEmpty() || total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("no billable charges for reconciliation");
        }
        BmsDomain.ReconciliationAggregate aggregate = BmsDomain.ReconciliationAggregate.create("RC" + reconciliationSequence.incrementAndGet(), command.objectCode(), command.billingPeriod(), total);
        BmsMapper.ReconciliationRow row = toRow(aggregate);
        mapper.insertReconciliation(row);
        outbox("ReconciliationIssued", row.reconciliationNo(), row.objectCode(), "{}");
        log("GENERATE_RECONCILIATION", row.reconciliationNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code raiseReconciliationDifference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reconciliationNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code DifferenceCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.ReconciliationRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.ReconciliationRow raiseReconciliationDifference(String reconciliationNo, DifferenceCommand command) {
        BmsMapper.ReconciliationRow row = require(mapper.findReconciliation(reconciliationNo), "reconciliation not found");
        BmsDomain.ReconciliationAggregate aggregate = restoreReconciliation(row);
        aggregate.raiseDifference(command.peerAmount(), command.expectedVersion());
        mapper.updateReconciliation(toRow(aggregate));
        outbox("ReconciliationDifferenceRaised", reconciliationNo, row.objectCode(), "{}");
        log("RAISE_RECONCILIATION_DIFFERENCE", reconciliationNo, command.operatorId(), command.idempotencyKey());
        return mapper.findReconciliation(reconciliationNo);
    }

    /**
     * 执行命令 {@code confirmReconciliation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reconciliationNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ConfirmAmountCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.ReconciliationRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.ReconciliationRow confirmReconciliation(String reconciliationNo, ConfirmAmountCommand command) {
        BmsMapper.ReconciliationRow row = require(mapper.findReconciliation(reconciliationNo), "reconciliation not found");
        BmsDomain.ReconciliationAggregate aggregate = restoreReconciliation(row);
        aggregate.confirm(command.confirmedAmount(), command.expectedVersion());
        mapper.updateReconciliation(toRow(aggregate));
        mapper.markChargesConfirmed(row.objectCode(), row.billingPeriod());
        outbox("ReconciliationConfirmed", reconciliationNo, row.objectCode(), "{}");
        log("CONFIRM_RECONCILIATION", reconciliationNo, command.operatorId(), command.idempotencyKey());
        return mapper.findReconciliation(reconciliationNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code generateBill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code GenerateBillCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.BillRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.BillRow generateBill(GenerateBillCommand command) {
        BmsMapper.ReconciliationRow reconciliation = require(mapper.findReconciliation(command.reconciliationNo()), "reconciliation not found");
        BmsDomain.ReconciliationAggregate reconciliationAggregate = restoreReconciliation(reconciliation);
        reconciliationAggregate.markBilled();
        mapper.updateReconciliation(toRow(reconciliationAggregate));
        BmsDomain.BillAggregate bill = BmsDomain.BillAggregate.create("BL" + billSequence.incrementAndGet(), reconciliation.reconciliationNo(), reconciliation.objectCode(), reconciliation.totalAmount());
        BmsMapper.BillRow row = toRow(bill);
        mapper.insertBill(row);
        outbox("BillGenerated", row.billNo(), row.objectCode(), "{}");
        log("GENERATE_BILL", row.billNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 执行命令 {@code confirmBill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param billNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code VersionCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.BillRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.BillRow confirmBill(String billNo, VersionCommand command) {
        BmsMapper.BillRow row = require(mapper.findBill(billNo), "bill not found");
        BmsDomain.BillAggregate aggregate = restoreBill(row);
        aggregate.confirm(command.expectedVersion());
        mapper.updateBill(toRow(aggregate));
        outbox("BillConfirmed", billNo, row.objectCode(), "{}");
        log("CONFIRM_BILL", billNo, command.operatorId(), command.idempotencyKey());
        return mapper.findBill(billNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code requestInvoice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code RequestInvoiceCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.InvoiceRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.InvoiceRow requestInvoice(RequestInvoiceCommand command) {
        BmsMapper.BillRow bill = require(mapper.findBill(command.billNo()), "bill not found");
        BmsDomain.InvoiceAggregate invoice = BmsDomain.InvoiceAggregate.request("IV" + invoiceSequence.incrementAndGet(), bill.billNo(), command.invoiceAmount(), bill.totalAmount());
        BmsMapper.InvoiceRow row = toRow(invoice);
        mapper.insertInvoice(row);
        outbox("InvoiceRequested", row.invoiceNo(), row.billNo(), "{}");
        log("REQUEST_INVOICE", row.invoiceNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code issueInvoice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param invoiceNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code VersionCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.InvoiceRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.InvoiceRow issueInvoice(String invoiceNo, VersionCommand command) {
        BmsMapper.InvoiceRow row = require(mapper.findInvoice(invoiceNo), "invoice not found");
        BmsDomain.InvoiceAggregate invoice = BmsDomain.InvoiceAggregate.restore(row.invoiceNo(), row.billNo(), row.invoiceAmount(), row.status(), row.version());
        invoice.issue(command.expectedVersion());
        mapper.updateInvoice(toRow(invoice));
        BmsMapper.BillRow billRow = require(mapper.findBill(row.billNo()), "bill not found");
        BmsDomain.BillAggregate bill = restoreBill(billRow);
        bill.markInvoiced(billRow.version());
        mapper.updateBill(toRow(bill));
        outbox("InvoiceIssued", invoiceNo, row.billNo(), "{}");
        log("ISSUE_INVOICE", invoiceNo, command.operatorId(), command.idempotencyKey());
        return mapper.findInvoice(invoiceNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code requestFinanceHandover}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code RequestFinanceCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.FinanceHandoverRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.FinanceHandoverRow requestFinanceHandover(RequestFinanceCommand command) {
        BmsMapper.BillRow bill = require(mapper.findBill(command.billNo()), "bill not found");
        BmsDomain.FinanceHandoverAggregate aggregate = BmsDomain.FinanceHandoverAggregate.request("FH" + financeSequence.incrementAndGet(), bill.billNo());
        BmsMapper.FinanceHandoverRow row = toRow(aggregate);
        mapper.insertFinanceHandover(row);
        outbox("FinanceHandoverRequested", row.handoverNo(), row.billNo(), "{}");
        log("REQUEST_FINANCE_HANDOVER", row.handoverNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code postFinanceHandover}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param handoverNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code PostFinanceCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.FinanceHandoverRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.FinanceHandoverRow postFinanceHandover(String handoverNo, PostFinanceCommand command) {
        BmsMapper.FinanceHandoverRow row = require(mapper.findFinanceHandover(handoverNo), "finance handover not found");
        BmsDomain.FinanceHandoverAggregate aggregate = restoreFinance(row);
        aggregate.post(command.voucherNo(), command.expectedVersion());
        mapper.updateFinanceHandover(toRow(aggregate));
        BmsMapper.BillRow billRow = require(mapper.findBill(row.billNo()), "bill not found");
        BmsDomain.BillAggregate bill = restoreBill(billRow);
        bill.markPosted(billRow.version());
        mapper.updateBill(toRow(bill));
        outbox("FinancialPosted", handoverNo, row.billNo(), "{}");
        log("POST_FINANCE_HANDOVER", handoverNo, command.operatorId(), command.idempotencyKey());
        return mapper.findFinanceHandover(handoverNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code failFinanceHandover}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param handoverNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code FailCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.FinanceHandoverRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.FinanceHandoverRow failFinanceHandover(String handoverNo, FailCommand command) {
        BmsMapper.FinanceHandoverRow row = require(mapper.findFinanceHandover(handoverNo), "finance handover not found");
        BmsDomain.FinanceHandoverAggregate aggregate = restoreFinance(row);
        aggregate.fail(command.reason(), command.expectedVersion());
        mapper.updateFinanceHandover(toRow(aggregate));
        outbox("FinancialPostFailed", handoverNo, row.billNo(), "{}");
        log("FAIL_FINANCE_HANDOVER", handoverNo, command.operatorId(), command.idempotencyKey());
        return mapper.findFinanceHandover(handoverNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code requestRefundSettlement}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code RequestRefundCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.RefundSettlementRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.RefundSettlementRow requestRefundSettlement(RequestRefundCommand command) {
        requireText(command.idempotencyKey(), "refund idempotency key is required");
        BmsMapper.RefundSettlementRow existing = mapper.findRefundByIdempotencyKey(command.idempotencyKey());
        String requestDigest = refundRequestDigest(command);
        if (existing != null) {
            if (!existing.requestDigest().equals(requestDigest)) {
                throw new IllegalStateException("refund idempotency key conflicts with another request");
            }
            return existing;
        }
        BmsMapper.BillRow bill = require(mapper.lockBill(command.billNo()), "bill not found");
        existing = mapper.findRefundByIdempotencyKey(command.idempotencyKey());
        if (existing != null) {
            if (!existing.requestDigest().equals(requestDigest)) {
                throw new IllegalStateException("refund idempotency key conflicts with another request");
            }
            return existing;
        }
        BmsMapper.BillingObjectRow billingObject = require(mapper.findBillingObject(bill.objectCode()),
            "billing object not found");
        String currency = command.currency() == null ? billingObject.currency() : command.currency();
        if (!billingObject.currency().equals(currency)) {
            throw new IllegalArgumentException("refund currency must match bill currency");
        }
        String merchantNo = command.merchantNo() == null ? bill.objectCode() : command.merchantNo();
        BigDecimal occupied = mapper.occupiedRefundAmount(command.billNo());
        BigDecimal refundable = bill.totalAmount().subtract(occupied == null ? BigDecimal.ZERO : occupied);
        BmsDomain.RefundSettlementAggregate aggregate = BmsDomain.RefundSettlementAggregate.request("RF" + refundSequence.incrementAndGet(), bill.billNo(), command.refundAmount(), refundable);
        BmsMapper.RefundSettlementRow row = new BmsMapper.RefundSettlementRow(null,
            aggregate.refundNo(), aggregate.billNo(), command.afterSaleNo(), command.paymentNo(),
            aggregate.refundAmount(), currency, merchantNo, command.idempotencyKey(), requestDigest,
            1, aggregate.status(), aggregate.failureReason(), null, null, aggregate.version());
        mapper.insertRefundSettlement(row);
        outbox("RefundSettlementRequested", row.refundNo(), row.billNo(), "{}");
        log("REQUEST_REFUND_SETTLEMENT", row.refundNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code finishRefundSettlement}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param refundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code VersionCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.RefundSettlementRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.RefundSettlementRow finishRefundSettlement(String refundNo, VersionCommand command) {
        BmsMapper.RefundSettlementRow row = require(mapper.findRefundSettlement(refundNo), "refund settlement not found");
        BmsDomain.RefundSettlementAggregate aggregate = restoreRefund(row);
        aggregate.finish(command.expectedVersion());
        updateRefund(aggregate, row, row.evidenceRef(), row.reviewerId(), row.attemptNo());
        outbox("RefundSettlementFinished", refundNo, row.billNo(), "{}");
        log("FINISH_REFUND_SETTLEMENT", refundNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRefundSettlement(refundNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code failRefundSettlement}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param refundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code FailCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.RefundSettlementRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.RefundSettlementRow failRefundSettlement(String refundNo, FailCommand command) {
        BmsMapper.RefundSettlementRow row = require(mapper.findRefundSettlement(refundNo), "refund settlement not found");
        BmsDomain.RefundSettlementAggregate aggregate = restoreRefund(row);
        aggregate.fail(command.reason(), command.expectedVersion());
        updateRefund(aggregate, row, row.evidenceRef(), row.reviewerId(), row.attemptNo());
        outbox("RefundSettlementFailed", refundNo, row.billNo(), "{}");
        log("FAIL_REFUND_SETTLEMENT", refundNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRefundSettlement(refundNo);
    }

    /**
     * 执行命令 {@code consumeRefundReceipt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param refundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code RefundReceiptCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.RefundSettlementRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.RefundSettlementRow consumeRefundReceipt(String refundNo, RefundReceiptCommand command) {
        BmsMapper.RefundSettlementRow row = require(mapper.findRefundSettlement(refundNo), "refund settlement not found");
        requireText(command.receiptNo(), "payment receipt no is required");
        BmsMapper.RefundReceiptRow existingReceipt = mapper.findRefundReceipt(command.receiptNo());
        if (existingReceipt != null) {
            if (sameReceipt(existingReceipt, refundNo, command)) {
                return row;
            }
            mapper.insertRefundException(new BmsMapper.RefundExceptionRow(refundNo,
                command.receiptNo(), "RECEIPT_CONFLICT",
                "receipt already belongs to another refund or carries different facts", command.payload()));
            return row;
        }
        if (!validateReceipt(row, command)) {
            return row;
        }
        if (row.status() == BmsDomain.RefundSettlementAggregate.FINISHED
                || row.status() == BmsDomain.RefundSettlementAggregate.FAILED
                || row.status() == BmsDomain.RefundSettlementAggregate.CLOSED) {
            mapper.insertRefundException(new BmsMapper.RefundExceptionRow(refundNo,
                command.receiptNo(), "LATE_RECEIPT", "terminal refund cannot consume a new receipt",
                command.payload()));
            return row;
        }
        if (mapper.claimRefundReceipt(command.receiptNo(), refundNo,
                command.success() ? SUCCESS : FAILED, command.refundAmount(), command.currency(),
                command.merchantNo(), command.paymentTxnNo(), command.failureReason(), command.payload()) == 0) {
            mapper.insertRefundException(new BmsMapper.RefundExceptionRow(refundNo,
                command.receiptNo(), "RECEIPT_CONFLICT", "receipt was claimed concurrently",
                command.payload()));
            return row;
        }
        BmsDomain.RefundSettlementAggregate aggregate = restoreRefund(row);
        if (command.success()) {
            aggregate.finish(row.version());
        } else {
            aggregate.fail(command.failureReason(), row.version());
        }
        updateRefund(aggregate, row, row.evidenceRef(), row.reviewerId(), row.attemptNo());
        outbox(command.success() ? "RefundCompleted" : "RefundFailed", refundNo, row.billNo(), command.payload());
        return mapper.findRefundSettlement(refundNo);
    }

    /**
     * 执行命令 {@code retryRefundSettlement}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param refundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code VersionCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.RefundSettlementRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.RefundSettlementRow retryRefundSettlement(String refundNo, VersionCommand command) {
        BmsMapper.RefundSettlementRow row = require(mapper.findRefundSettlement(refundNo), "refund settlement not found");
        BmsMapper.BillRow bill = require(mapper.lockBill(row.billNo()), "bill not found");
        BigDecimal occupied = mapper.occupiedRefundAmount(row.billNo());
        BigDecimal refundable = bill.totalAmount().subtract(occupied == null ? BigDecimal.ZERO : occupied);
        if (row.refundAmount().compareTo(refundable) > 0) {
            throw new IllegalArgumentException("refund amount cannot exceed refundable amount when retrying");
        }
        BmsDomain.RefundSettlementAggregate aggregate = restoreRefund(row);
        aggregate.retry(command.expectedVersion());
        updateRefund(aggregate, row, null, null, row.attemptNo() + 1);
        outbox("RefundRetryRequested", refundNo, row.billNo(), "{}");
        log("RETRY_REFUND_SETTLEMENT", refundNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRefundSettlement(refundNo);
    }

    /** 支付超时或返回结果未知时，保留退款额度并进入查单状态。 */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.RefundSettlementRow markRefundConfirmationPending(
            String refundNo, ConfirmationPendingCommand command) {
        BmsMapper.RefundSettlementRow row = require(mapper.findRefundSettlement(refundNo),
            "refund settlement not found");
        BmsDomain.RefundSettlementAggregate aggregate = restoreRefund(row);
        aggregate.markConfirmationPending(command.reason(), command.expectedVersion());
        updateRefund(aggregate, row, row.evidenceRef(), row.reviewerId(), row.attemptNo());
        outbox("RefundConfirmationPending", refundNo, row.billNo(), "{}");
        log("MARK_REFUND_CONFIRMATION_PENDING", refundNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRefundSettlement(refundNo);
    }

    /** 人工确认渠道未退款后关闭待确认记录；必须凭证齐全且复核人不同于操作人。 */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.RefundSettlementRow closeRefundManually(
            String refundNo, ManualRefundResolutionCommand command) {
        validateManualResolution(command);
        BmsMapper.RefundSettlementRow row = require(mapper.findRefundSettlement(refundNo),
            "refund settlement not found");
        BmsDomain.RefundSettlementAggregate aggregate = restoreRefund(row);
        aggregate.closeManually(command.reason(), command.expectedVersion());
        updateRefund(aggregate, row, command.evidenceRef(), command.reviewerId(), row.attemptNo());
        outbox("RefundManuallyClosed", refundNo, row.billNo(), "{}");
        log("CLOSE_REFUND_MANUALLY", refundNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRefundSettlement(refundNo);
    }

    /** 人工凭证确认已退款时完成待确认记录。 */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.RefundSettlementRow completeRefundManually(
            String refundNo, ManualRefundResolutionCommand command) {
        validateManualResolution(command);
        BmsMapper.RefundSettlementRow row = require(mapper.findRefundSettlement(refundNo),
            "refund settlement not found");
        if (row.status() != BmsDomain.RefundSettlementAggregate.CONFIRMATION_PENDING) {
            throw new IllegalStateException("only confirmation pending refund can complete manually");
        }
        BmsDomain.RefundSettlementAggregate aggregate = restoreRefund(row);
        aggregate.finish(command.expectedVersion());
        updateRefund(aggregate, row, command.evidenceRef(), command.reviewerId(), row.attemptNo());
        outbox("RefundCompleted", refundNo, row.billNo(), "{}");
        log("COMPLETE_REFUND_MANUALLY", refundNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRefundSettlement(refundNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code settlementSummary}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param from 业务处理参数或成员，类型为 {@code LocalDateTime}
     * @param to 业务处理参数或成员，类型为 {@code LocalDateTime}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.SettlementSummaryRow}
     */
    public BmsMapper.SettlementSummaryRow settlementSummary(LocalDateTime from, LocalDateTime to) {
        return mapper.settlementSummary(from, to);
    }

    /**
     * 执行命令 {@code consumeEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code ConsumeEventCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.InboxEventRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsMapper.InboxEventRow consumeEvent(ConsumeEventCommand command) {
        BmsMapper.InboxEventRow existing = mapper.findInboxEvent(command.sourceSystem(), command.sourceEventId());
        if (existing != null) {
            return existing;
        }
        BmsMapper.InboxEventRow row = new BmsMapper.InboxEventRow(
            "BI" + inboxSequence.incrementAndGet(), command.sourceSystem(),
            command.sourceEventId(), command.eventType(), command.businessNo(),
            command.payload(), 1, null);
        mapper.insertInboxEvent(row);
        if (!isChargeSourceEvent(command.eventType())) {
            BmsMapper.InboxEventRow ignored = changeInboxStatus(
                row, 4, "event does not produce a BMS charge source");
            outbox("BmsExternalEventIgnored", row.inboxNo(), row.businessNo(),
                "{\"eventType\":\"" + sanitize(command.eventType()) + "\"}");
            return ignored;
        }

        CollectChargeSourceCommand collectCommand = toChargeSourceCommand(command);
        BmsMapper.ChargeSourceRow existingSource = mapper.findChargeSourceByIdempotency(
            collectCommand.sourceSystem(), collectCommand.idempotencyKey());
        BmsMapper.ChargeSourceRow source = existingSource == null
            ? collectChargeSource(collectCommand) : existingSource;
        int status = source.status() == BmsDomain.ChargeSourceAggregate.FAILED ? 3 : 2;
        BmsMapper.InboxEventRow processed = changeInboxStatus(
            row, status, source.failureReason());
        if (status == 2 && existingSource == null) {
            outbox("BmsFeeSourceCollected", source.sourceNo(), command.businessNo(),
                "{\"sourceEventId\":\"" + sanitize(command.sourceEventId())
                    + "\",\"feeSourceNo\":\"" + sanitize(command.businessNo())
                    + "\",\"bmsReceiveNo\":\"" + sanitize(source.sourceNo()) + "\"}");
        }
        return processed;
    }

    /**
     * 将外部费用事实转换为当前限界上下文的费用采集命令。
     */
    private CollectChargeSourceCommand toChargeSourceCommand(ConsumeEventCommand command) {
        try {
            JsonNode payload = objectMapper.readTree(command.payload());
            if (payload == null || !payload.isObject()) {
                throw new IllegalArgumentException("charge source payload must be a JSON object");
            }
            String billingObjectCode = requiredPayloadText(payload, "billingObjectCode");
            String feeType = requiredPayloadText(payload, "feeType");
            String billingPeriod = requiredPayloadText(payload, "billingPeriod");
            JsonNode quantityNode = payload.get("quantity");
            if (quantityNode == null || !quantityNode.isNumber()
                    || quantityNode.decimalValue().signum() <= 0) {
                throw new IllegalArgumentException("charge source quantity must be positive");
            }
            String idempotencyKey = isTmsFeeSourceEvent(command)
                ? command.sourceSystem() + ":FEE_SOURCE:" + command.businessNo()
                : command.sourceSystem() + ':' + command.sourceEventId() + ':'
                    + command.eventType() + ':' + command.businessNo();
            return new CollectChargeSourceCommand(
                command.sourceSystem(), command.sourceEventId(), idempotencyKey,
                billingObjectCode, feeType, quantityNode.decimalValue(), billingPeriod,
                command.payload(), null);
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            throw new IllegalArgumentException("charge source payload is invalid JSON", exception);
        }
    }

    private BmsMapper.InboxEventRow changeInboxStatus(
            BmsMapper.InboxEventRow row, int status, String failureReason) {
        BmsMapper.InboxEventRow changed = new BmsMapper.InboxEventRow(
            row.inboxNo(), row.sourceSystem(), row.sourceEventId(), row.eventType(),
            row.businessNo(), row.payload(), status, failureReason);
        mapper.updateInboxEvent(changed);
        return changed;
    }

    private static String requiredPayloadText(JsonNode payload, String field) {
        JsonNode value = payload.get(field);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            throw new IllegalArgumentException("charge source payload field is required: " + field);
        }
        return value.asText();
    }

    private static boolean isChargeSourceEvent(String eventType) {
        return "LogisticsFeeSourceGenerated".equals(eventType)
            || "LogisticsFeeSourcePushed".equals(eventType)
            || "GoodsReceived".equals(eventType)
            || "GoodsPutawayCompleted".equals(eventType)
            || "OutboundOrderShipped".equals(eventType);
    }

    /**
     * 判断事件是否为 TMS 同一费用源生命周期中的事实。生成和推送事件共享费用源业务号，
     * 因而必须共享幂等键，不能因事件类型或消息编号不同而重复计费。
     */
    private static boolean isTmsFeeSourceEvent(ConsumeEventCommand command) {
        return "TMS".equalsIgnoreCase(command.sourceSystem())
            && ("LogisticsFeeSourceGenerated".equals(command.eventType())
                || "LogisticsFeeSourcePushed".equals(command.eventType()));
    }

    /**
     * 处理当前类型职责中的操作 {@code calculateSource}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param source 业务处理参数或成员，类型为 {@code BmsDomain.ChargeSourceAggregate}
     * @param billingPeriod 业务处理参数或成员，类型为 {@code String}
     */
    private void calculateSource(BmsDomain.ChargeSourceAggregate source, String billingPeriod) {
        BmsDomain.BillingObjectAggregate object = loadBillingObject(source.billingObjectCode());
        object.ensureEnabled();
        BmsMapper.BillingRuleRow rule = require(mapper.findPublishedRule(source.billingObjectCode(), source.feeType()), "published billing rule not found");
        BmsDomain.BillingRuleAggregate ruleAggregate = restoreRule(rule);
        LocalDate date = periodStart(billingPeriod);
        if (!ruleAggregate.effectiveOn(date)) {
            throw new IllegalStateException("billing rule is not effective for period");
        }
        source.accept();
    }

    /**
     * 执行命令 {@code createChargeDetail}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param source 业务处理参数或成员，类型为 {@code BmsDomain.ChargeSourceAggregate}
     * @param billingPeriod 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code BmsMapper.ChargeDetailRow}
     */
    private BmsMapper.ChargeDetailRow createChargeDetail(BmsDomain.ChargeSourceAggregate source, String billingPeriod) {
        BmsMapper.BillingRuleRow rule = require(mapper.findPublishedRule(source.billingObjectCode(), source.feeType()), "published billing rule not found");
        BmsDomain.BillingRuleAggregate ruleAggregate = restoreRule(rule);
        BmsDomain.ChargeDetailAggregate charge = BmsDomain.ChargeDetailAggregate.create("CD" + chargeSequence.incrementAndGet(), source.sourceNo(), source.billingObjectCode(), source.feeType(), rule.ruleNo(), source.quantity(), rule.unitPrice(), ruleAggregate.calculate(source.quantity()));
        return toRow(charge, billingPeriod);
    }

    /**
     * 查询并返回 {@code loadBillingObject}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param objectCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code BmsDomain.BillingObjectAggregate}
     */
    private BmsDomain.BillingObjectAggregate loadBillingObject(String objectCode) {
        BmsMapper.BillingObjectRow row = require(mapper.findBillingObject(objectCode), "billing object not found");
        return BmsDomain.BillingObjectAggregate.restore(row.objectCode(), row.objectName(), row.objectType(), row.direction(), row.currency(), row.status(), row.version());
    }

    /**
     * 查询并返回 {@code loadBillingRule}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param ruleNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code BmsDomain.BillingRuleAggregate}
     */
    private BmsDomain.BillingRuleAggregate loadBillingRule(String ruleNo) {
        return restoreRule(require(mapper.findBillingRule(ruleNo), "billing rule not found"));
    }

    /**
     * 处理当前类型职责中的操作 {@code restoreRule}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code BmsMapper.BillingRuleRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsDomain.BillingRuleAggregate}
     */
    private BmsDomain.BillingRuleAggregate restoreRule(BmsMapper.BillingRuleRow row) {
        return BmsDomain.BillingRuleAggregate.restore(row.ruleNo(), row.objectCode(), row.feeType(), row.unitPrice(), row.taxRate(), row.effectiveFrom(), row.effectiveTo(), row.status(), row.ruleVersion(), row.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code restoreCharge}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code BmsMapper.ChargeDetailRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsDomain.ChargeDetailAggregate}
     */
    private BmsDomain.ChargeDetailAggregate restoreCharge(BmsMapper.ChargeDetailRow row) {
        return BmsDomain.ChargeDetailAggregate.restore(row.chargeNo(), row.sourceNo(), row.objectCode(), row.feeType(), row.ruleNo(), row.quantity(), row.unitPrice(), row.amount(), row.taxAmount(), row.totalAmount(), row.status(), row.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code restoreReconciliation}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code BmsMapper.ReconciliationRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsDomain.ReconciliationAggregate}
     */
    private BmsDomain.ReconciliationAggregate restoreReconciliation(BmsMapper.ReconciliationRow row) {
        return BmsDomain.ReconciliationAggregate.restore(row.reconciliationNo(), row.objectCode(), row.billingPeriod(), row.totalAmount(), row.status(), row.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code restoreBill}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code BmsMapper.BillRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsDomain.BillAggregate}
     */
    private BmsDomain.BillAggregate restoreBill(BmsMapper.BillRow row) {
        return BmsDomain.BillAggregate.restore(row.billNo(), row.reconciliationNo(), row.objectCode(), row.totalAmount(), row.status(), row.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code restoreFinance}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code BmsMapper.FinanceHandoverRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsDomain.FinanceHandoverAggregate}
     */
    private BmsDomain.FinanceHandoverAggregate restoreFinance(BmsMapper.FinanceHandoverRow row) {
        return BmsDomain.FinanceHandoverAggregate.restore(row.handoverNo(), row.billNo(), row.status(), row.voucherNo(), row.failureReason(), row.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code restoreRefund}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code BmsMapper.RefundSettlementRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsDomain.RefundSettlementAggregate}
     */
    private BmsDomain.RefundSettlementAggregate restoreRefund(BmsMapper.RefundSettlementRow row) {
        return BmsDomain.RefundSettlementAggregate.restore(row.refundNo(), row.billNo(), row.refundAmount(), row.status(), row.failureReason(), row.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code BmsDomain.BillingObjectAggregate}
     * @return 转换数据模型的结果，类型为 {@code BmsMapper.BillingObjectRow}
     */
    private BmsMapper.BillingObjectRow toRow(BmsDomain.BillingObjectAggregate aggregate) {
        return new BmsMapper.BillingObjectRow(null, aggregate.objectCode(), aggregate.objectName(), aggregate.objectType(), aggregate.direction(), aggregate.currency(), aggregate.status(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code BmsDomain.BillingRuleAggregate}
     * @return 转换数据模型的结果，类型为 {@code BmsMapper.BillingRuleRow}
     */
    private BmsMapper.BillingRuleRow toRow(BmsDomain.BillingRuleAggregate aggregate) {
        return new BmsMapper.BillingRuleRow(null, aggregate.ruleNo(), aggregate.objectCode(), aggregate.feeType(), aggregate.unitPrice(), aggregate.taxRate(), aggregate.effectiveFrom(), aggregate.effectiveTo(), aggregate.status(), aggregate.ruleVersion(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code BmsDomain.ChargeSourceAggregate}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @param billingPeriod 业务处理参数或成员，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     * @return 转换数据模型的结果，类型为 {@code BmsMapper.ChargeSourceRow}
     */
    private BmsMapper.ChargeSourceRow toRow(BmsDomain.ChargeSourceAggregate aggregate, String idempotencyKey, String billingPeriod, String payload) {
        return new BmsMapper.ChargeSourceRow(null, aggregate.sourceNo(), aggregate.sourceSystem(), aggregate.sourceEventId(), idempotencyKey, aggregate.billingObjectCode(), aggregate.feeType(), aggregate.quantity(), billingPeriod, payload, aggregate.status(), aggregate.failureReason(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code BmsDomain.ChargeDetailAggregate}
     * @param billingPeriod 业务处理参数或成员，类型为 {@code String}
     * @return 转换数据模型的结果，类型为 {@code BmsMapper.ChargeDetailRow}
     */
    private BmsMapper.ChargeDetailRow toRow(BmsDomain.ChargeDetailAggregate aggregate, String billingPeriod) {
        return new BmsMapper.ChargeDetailRow(null, aggregate.chargeNo(), aggregate.sourceNo(), aggregate.objectCode(), aggregate.feeType(), aggregate.ruleNo(), aggregate.quantity(), aggregate.unitPrice(), aggregate.amount(), aggregate.taxAmount(), aggregate.totalAmount(), billingPeriod, aggregate.status(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code BmsDomain.ChargeAdjustmentAggregate}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 转换数据模型的结果，类型为 {@code BmsMapper.AdjustmentRow}
     */
    private BmsMapper.AdjustmentRow toRow(BmsDomain.ChargeAdjustmentAggregate aggregate, String reason) {
        return new BmsMapper.AdjustmentRow(null, aggregate.adjustmentNo(), aggregate.originalChargeNo(), aggregate.adjustAmount(), reason, aggregate.status(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code BmsDomain.ReconciliationAggregate}
     * @return 转换数据模型的结果，类型为 {@code BmsMapper.ReconciliationRow}
     */
    private BmsMapper.ReconciliationRow toRow(BmsDomain.ReconciliationAggregate aggregate) {
        return new BmsMapper.ReconciliationRow(null, aggregate.reconciliationNo(), aggregate.objectCode(), aggregate.period(), aggregate.totalAmount(), aggregate.status(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code BmsDomain.BillAggregate}
     * @return 转换数据模型的结果，类型为 {@code BmsMapper.BillRow}
     */
    private BmsMapper.BillRow toRow(BmsDomain.BillAggregate aggregate) {
        return new BmsMapper.BillRow(null, aggregate.billNo(), aggregate.reconciliationNo(), aggregate.objectCode(), aggregate.totalAmount(), aggregate.status(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code BmsDomain.InvoiceAggregate}
     * @return 转换数据模型的结果，类型为 {@code BmsMapper.InvoiceRow}
     */
    private BmsMapper.InvoiceRow toRow(BmsDomain.InvoiceAggregate aggregate) {
        return new BmsMapper.InvoiceRow(null, aggregate.invoiceNo(), aggregate.billNo(), aggregate.invoiceAmount(), aggregate.status(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code BmsDomain.FinanceHandoverAggregate}
     * @return 转换数据模型的结果，类型为 {@code BmsMapper.FinanceHandoverRow}
     */
    private BmsMapper.FinanceHandoverRow toRow(BmsDomain.FinanceHandoverAggregate aggregate) {
        return new BmsMapper.FinanceHandoverRow(null, aggregate.handoverNo(), aggregate.billNo(), aggregate.status(), aggregate.voucherNo(), aggregate.failureReason(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code BmsDomain.RefundSettlementAggregate}
     * @return 转换数据模型的结果，类型为 {@code BmsMapper.RefundSettlementRow}
     */
    private BmsMapper.RefundSettlementRow toRow(BmsDomain.RefundSettlementAggregate aggregate) {
        return new BmsMapper.RefundSettlementRow(null, aggregate.refundNo(), aggregate.billNo(),
            aggregate.refundAmount(), aggregate.status(), aggregate.failureReason(), aggregate.version());
    }

    /** 按原持久化快照保留不可变退款事实，并使用版本条件更新。 */
    private void updateRefund(BmsDomain.RefundSettlementAggregate aggregate,
                              BmsMapper.RefundSettlementRow source, String evidenceRef,
                              Long reviewerId, int attemptNo) {
        BmsMapper.RefundSettlementRow target = new BmsMapper.RefundSettlementRow(source.id(),
            aggregate.refundNo(), aggregate.billNo(), source.afterSaleNo(), source.paymentNo(),
            aggregate.refundAmount(), source.currency(), source.merchantNo(),
            source.requestIdempotencyKey(), source.requestDigest(), attemptNo, aggregate.status(),
            aggregate.failureReason(), evidenceRef, reviewerId, aggregate.version());
        if (mapper.updateRefundSettlement(target) != 1) {
            throw new IllegalStateException("refund settlement version conflict");
        }
    }

    /** 生成用于识别幂等键误复用的稳定业务摘要。 */
    private String refundRequestDigest(RequestRefundCommand command) {
        return command.billNo() + "|" + command.refundAmount().stripTrailingZeros().toPlainString()
            + "|" + safe(command.currency()) + "|" + safe(command.merchantNo())
            + "|" + safe(command.afterSaleNo()) + "|" + safe(command.paymentNo());
    }

    /** 比较回执已持久事实，只有全部一致才视为幂等重放。 */
    private boolean sameReceipt(BmsMapper.RefundReceiptRow receipt, String refundNo,
                                RefundReceiptCommand command) {
        return receipt.refundNo().equals(refundNo)
            && receipt.status().equals(command.success() ? SUCCESS : FAILED)
            && equalAmount(receipt.refundAmount(), command.refundAmount())
            && java.util.Objects.equals(receipt.currency(), command.currency())
            && java.util.Objects.equals(receipt.merchantNo(), command.merchantNo())
            && java.util.Objects.equals(receipt.paymentTxnNo(), command.paymentTxnNo())
            && java.util.Objects.equals(receipt.failureReason(), command.failureReason());
    }

    /** 回执必须与退款聚合的金额、币种和商户事实一致。 */
    private boolean validateReceipt(BmsMapper.RefundSettlementRow row, RefundReceiptCommand command) {
        if (!equalAmount(row.refundAmount(), command.refundAmount())) {
            return rejectReceipt(row, command, "REFUND_AMOUNT_MISMATCH", "receipt amount mismatch");
        }
        if (!java.util.Objects.equals(row.currency(), command.currency())) {
            return rejectReceipt(row, command, "CURRENCY_MISMATCH", "receipt currency mismatch");
        }
        if (!java.util.Objects.equals(row.merchantNo(), command.merchantNo())) {
            return rejectReceipt(row, command, "MERCHANT_MISMATCH", "receipt merchant mismatch");
        }
        if (!command.success() && (command.failureReason() == null
                || command.failureReason().isBlank())) {
            return rejectReceipt(row, command, "FAILURE_REASON_MISSING",
                "failed receipt reason is required");
        }
        return true;
    }

    private boolean rejectReceipt(BmsMapper.RefundSettlementRow row,
                                  RefundReceiptCommand command, String type, String detail) {
        mapper.insertRefundException(new BmsMapper.RefundExceptionRow(row.refundNo(),
            command.receiptNo(), type, detail, command.payload()));
        return false;
    }

    private boolean equalAmount(BigDecimal left, BigDecimal right) {
        return left != null && right != null && left.compareTo(right) == 0;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private void validateManualResolution(ManualRefundResolutionCommand command) {
        requireText(command.reason(), "manual resolution reason is required");
        requireText(command.evidenceRef(), "manual resolution evidence is required");
        requireText(command.idempotencyKey(), "manual resolution idempotency key is required");
        if (command.operatorId() == null || command.reviewerId() == null) {
            throw new IllegalArgumentException("operator and reviewer are required");
        }
        if (command.operatorId().equals(command.reviewerId())) {
            throw new IllegalArgumentException("manual resolution requires two different persons");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code outbox}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param aggregateNo 可追踪业务编码，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     */
    private void outbox(String eventType, String aggregateNo, String businessNo, String payload) {
        mapper.insertOutboxEvent(new BmsMapper.OutboxEventRow("BE" + eventSequence.incrementAndGet(), eventType, aggregateNo, businessNo, payload, 1));
    }

    /**
     * 处理当前类型职责中的操作 {@code log}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param operationType 业务处理参数或成员，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code Long}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     */
    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new BmsMapper.OperationLogRow(operationType, businessNo, operatorId, idempotencyKey, LocalDateTime.now()));
    }

    /**
     * 处理当前类型职责中的操作 {@code periodStart}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param billingPeriod 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LocalDate}
     */
    private LocalDate periodStart(String billingPeriod) {
        if (billingPeriod == null || !billingPeriod.matches(PERIOD_START_PATTERN)) {
            throw new IllegalArgumentException("billing period must be yyyy-MM");
        }
        return LocalDate.parse(billingPeriod + "-01");
    }

    /**
     * 处理当前类型职责中的操作 {@code sanitize}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String sanitize(String value) {
        return value == null ? "" : value.replace("\"", "'");
    }

    /**
     * 查询并返回 {@code require}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param value 业务处理参数或成员，类型为 {@code T}
     * @param message 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code T}
     */
    private <T> T require(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    /**
     * CreateBillingObjectCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateBillingObjectCommand(String objectCode, String objectName, String objectType, String direction, String currency, Long operatorId, String idempotencyKey) {
    }

    /**
     * CreateBillingRuleCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateBillingRuleCommand(String objectCode, String feeType, BigDecimal unitPrice, BigDecimal taxRate, LocalDate effectiveFrom, LocalDate effectiveTo, Long operatorId, String idempotencyKey) {
    }

    /**
     * CollectChargeSourceCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CollectChargeSourceCommand(String sourceSystem, String sourceEventId, String idempotencyKey, String billingObjectCode, String feeType, BigDecimal quantity, String billingPeriod, String payload, Long operatorId) {
    }

    /**
     * RecalculateChargeCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RecalculateChargeCommand(BigDecimal quantity, String reason, long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * CreateAdjustmentCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateAdjustmentCommand(String originalChargeNo, BigDecimal adjustAmount, String reason, boolean approved, Long operatorId, String idempotencyKey) {
    }

    /**
     * GenerateReconciliationCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record GenerateReconciliationCommand(String objectCode, String billingPeriod, Long operatorId, String idempotencyKey) {
    }

    /**
     * DifferenceCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record DifferenceCommand(BigDecimal peerAmount, String reason, long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * ConfirmAmountCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ConfirmAmountCommand(BigDecimal confirmedAmount, long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * GenerateBillCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record GenerateBillCommand(String reconciliationNo, Long operatorId, String idempotencyKey) {
    }

    /**
     * RequestInvoiceCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RequestInvoiceCommand(String billNo, BigDecimal invoiceAmount, Long operatorId, String idempotencyKey) {
    }

    /**
     * RequestFinanceCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RequestFinanceCommand(String billNo, Long operatorId, String idempotencyKey) {
    }

    /**
     * PostFinanceCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record PostFinanceCommand(String voucherNo, long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * RequestRefundCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RequestRefundCommand(String billNo, String afterSaleNo, String paymentNo,
                                       BigDecimal refundAmount, String currency, String merchantNo,
                                       Long operatorId, String idempotencyKey) {

        /** 兼容已有内部调用；币种和商户号由账单计费对象补全。 */
        public RequestRefundCommand(String billNo, BigDecimal refundAmount, Long operatorId,
                                    String idempotencyKey) {
            this(billNo, null, null, refundAmount, null, null, operatorId, idempotencyKey);
        }
    }

    /**
     * RefundReceiptCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RefundReceiptCommand(String receiptNo, boolean success, String failureReason,
                                       BigDecimal refundAmount, String currency, String merchantNo,
                                       String paymentTxnNo, String payload) {
    }

    /** 退款进入结果未知状态的命令。 */
    public record ConfirmationPendingCommand(String reason, long expectedVersion, Long operatorId,
                                             String idempotencyKey) {
    }

    /** 高风险人工退款处置命令，显式携带凭证和第二复核人。 */
    public record ManualRefundResolutionCommand(String reason, String evidenceRef,
                                                long expectedVersion, Long operatorId,
                                                Long reviewerId, String idempotencyKey) {
    }

    /**
     * ConsumeEventCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ConsumeEventCommand(String sourceSystem, String sourceEventId, String eventType, String businessNo, String payload) {
    }

    /**
     * VersionCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record VersionCommand(long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * ReplayCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ReplayCommand(Long operatorId, String idempotencyKey) {
    }

    /**
     * FailCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record FailCommand(String reason, long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * 业务常量 {@code PERIOD_START_PATTERN}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String PERIOD_START_PATTERN = "\\d{4}-\\d{2}";

    /**
     * 业务常量 {@code FAILED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String FAILED = "FAILED";

    /**
     * 业务常量 {@code SUCCESS}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String SUCCESS = "SUCCESS";
}
