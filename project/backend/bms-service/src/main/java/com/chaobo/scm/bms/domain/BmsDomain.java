package com.chaobo.scm.bms.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * BmsDomain。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class BmsDomain {

    /**
     * 创建 BmsDomain。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     */
    private BmsDomain() {
    }

    /**
     * BillingObjectAggregate。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public static final class BillingObjectAggregate {

        /**
         * ENABLED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int ENABLED = 1;

        /**
         * DISABLED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int DISABLED = 2;

        /**
         * objectCode（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String objectCode;

        /**
         * objectName（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final String objectName;

        /**
         * objectType（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final String objectType;

        /**
         * direction（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final String direction;

        /**
         * currency（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final String currency;

        /**
         * status（类型：{@code int}）。
         *
         * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
         */
        private int status;

        /**
         * version（类型：{@code long}）。
         *
         * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
         */
        private long version;

        /**
         * 创建 BillingObjectAggregate。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param objectName 业务处理参数或成员，类型为 {@code String}
         * @param objectType 业务处理参数或成员，类型为 {@code String}
         * @param direction 业务处理参数或成员，类型为 {@code String}
         * @param currency 业务处理参数或成员，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         */
        private BillingObjectAggregate(String objectCode, String objectName, String objectType, String direction, String currency, int status, long version) {
            require(objectCode, "billing object code is required");
            require(objectName, "billing object name is required");
            require(objectType, "billing object type is required");
            require(direction, "billing direction is required");
            require(currency, "currency is required");
            this.objectCode = objectCode;
            this.objectName = objectName;
            this.objectType = objectType;
            this.direction = direction;
            this.currency = currency;
            this.status = status;
            this.version = version;
        }

        /**
         * 执行命令 {@code create}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param objectName 业务处理参数或成员，类型为 {@code String}
         * @param objectType 业务处理参数或成员，类型为 {@code String}
         * @param direction 业务处理参数或成员，类型为 {@code String}
         * @param currency 业务处理参数或成员，类型为 {@code String}
         * @return 执行命令的结果，类型为 {@code BillingObjectAggregate}
         */
        public static BillingObjectAggregate create(String objectCode, String objectName, String objectType, String direction, String currency) {
            return new BillingObjectAggregate(objectCode, objectName, objectType, direction, currency, ENABLED, 1);
        }

        /**
         * 处理当前类型职责中的操作 {@code restore}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param objectName 业务处理参数或成员，类型为 {@code String}
         * @param objectType 业务处理参数或成员，类型为 {@code String}
         * @param direction 业务处理参数或成员，类型为 {@code String}
         * @param currency 业务处理参数或成员，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BillingObjectAggregate}
         */
        public static BillingObjectAggregate restore(String objectCode, String objectName, String objectType, String direction, String currency, int status, long version) {
            return new BillingObjectAggregate(objectCode, objectName, objectType, direction, currency, status, version);
        }

        /**
         * 执行命令 {@code enable}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void enable(long expectedVersion) {
            ensureVersion(expectedVersion);
            status = ENABLED;
            version++;
        }

        /**
         * 执行命令 {@code disable}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void disable(long expectedVersion) {
            ensureVersion(expectedVersion);
            status = DISABLED;
            version++;
        }

        /**
         * 校验业务约束 {@code ensureEnabled}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         */
        public void ensureEnabled() {
            if (status != ENABLED) {
                throw new IllegalStateException("disabled billing object cannot generate charge");
            }
        }

        /**
         * 处理当前类型职责中的操作 {@code objectCode}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String objectCode() {
            return objectCode;
        }

        /**
         * 处理当前类型职责中的操作 {@code objectName}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String objectName() {
            return objectName;
        }

        /**
         * 处理当前类型职责中的操作 {@code objectType}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String objectType() {
            return objectType;
        }

        /**
         * 处理当前类型职责中的操作 {@code direction}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String direction() {
            return direction;
        }

        /**
         * 处理当前类型职责中的操作 {@code currency}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String currency() {
            return currency;
        }

        /**
         * 处理当前类型职责中的操作 {@code status}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        public int status() {
            return status;
        }

        /**
         * 处理当前类型职责中的操作 {@code version}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        public long version() {
            return version;
        }

        /**
         * 校验业务约束 {@code ensureVersion}。
         *
         * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("billing object version conflict");
            }
        }
    }

    /**
     * BillingRuleAggregate。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public static final class BillingRuleAggregate {

        /**
         * DRAFT（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int DRAFT = 1;

        /**
         * PUBLISHED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int PUBLISHED = 2;

        /**
         * DISABLED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int DISABLED = 3;

        /**
         * ruleNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String ruleNo;

        /**
         * objectCode（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String objectCode;

        /**
         * feeType（类型：{@code String}）。
         *
         * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
         */
        private final String feeType;

        /**
         * unitPrice（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
         */
        private final BigDecimal unitPrice;

        /**
         * taxRate（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final BigDecimal taxRate;

        /**
         * effectiveFrom（类型：{@code LocalDate}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final LocalDate effectiveFrom;

        /**
         * effectiveTo（类型：{@code LocalDate}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final LocalDate effectiveTo;

        /**
         * status（类型：{@code int}）。
         *
         * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
         */
        private int status;

        /**
         * ruleVersion（类型：{@code int}）。
         *
         * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
         */
        private int ruleVersion;

        /**
         * version（类型：{@code long}）。
         *
         * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
         */
        private long version;

        /**
         * 创建 BillingRuleAggregate。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param ruleNo 可追踪业务编码，类型为 {@code String}
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param feeType 金额或计费值，类型为 {@code String}
         * @param unitPrice 金额或计费值，类型为 {@code BigDecimal}
         * @param taxRate 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param effectiveFrom 业务处理参数或成员，类型为 {@code LocalDate}
         * @param effectiveTo 业务处理参数或成员，类型为 {@code LocalDate}
         * @param status 生命周期状态，类型为 {@code int}
         * @param ruleVersion 乐观锁或契约版本，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         */
        private BillingRuleAggregate(String ruleNo, String objectCode, String feeType, BigDecimal unitPrice, BigDecimal taxRate, LocalDate effectiveFrom, LocalDate effectiveTo, int status, int ruleVersion, long version) {
            require(ruleNo, "billing rule no is required");
            require(objectCode, "billing object code is required");
            require(feeType, "fee type is required");
            requirePositive(unitPrice, "unit price must be positive");
            requireNonNegative(taxRate, "tax rate cannot be negative");
            if (taxRate.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("tax rate cannot be greater than 1");
            }
            if (effectiveFrom == null || effectiveTo == null || effectiveFrom.isAfter(effectiveTo)) {
                throw new IllegalArgumentException("effective range is invalid");
            }
            this.ruleNo = ruleNo;
            this.objectCode = objectCode;
            this.feeType = feeType;
            this.unitPrice = unitPrice.setScale(4, RoundingMode.HALF_UP);
            this.taxRate = taxRate.setScale(4, RoundingMode.HALF_UP);
            this.effectiveFrom = effectiveFrom;
            this.effectiveTo = effectiveTo;
            this.status = status;
            this.ruleVersion = ruleVersion;
            this.version = version;
        }

        /**
         * 执行命令 {@code create}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param ruleNo 可追踪业务编码，类型为 {@code String}
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param feeType 金额或计费值，类型为 {@code String}
         * @param unitPrice 金额或计费值，类型为 {@code BigDecimal}
         * @param taxRate 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param effectiveFrom 业务处理参数或成员，类型为 {@code LocalDate}
         * @param effectiveTo 业务处理参数或成员，类型为 {@code LocalDate}
         * @return 执行命令的结果，类型为 {@code BillingRuleAggregate}
         */
        public static BillingRuleAggregate create(String ruleNo, String objectCode, String feeType, BigDecimal unitPrice, BigDecimal taxRate, LocalDate effectiveFrom, LocalDate effectiveTo) {
            return new BillingRuleAggregate(ruleNo, objectCode, feeType, unitPrice, taxRate, effectiveFrom, effectiveTo, DRAFT, 0, 1);
        }

        /**
         * 处理当前类型职责中的操作 {@code restore}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param ruleNo 可追踪业务编码，类型为 {@code String}
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param feeType 金额或计费值，类型为 {@code String}
         * @param unitPrice 金额或计费值，类型为 {@code BigDecimal}
         * @param taxRate 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param effectiveFrom 业务处理参数或成员，类型为 {@code LocalDate}
         * @param effectiveTo 业务处理参数或成员，类型为 {@code LocalDate}
         * @param status 生命周期状态，类型为 {@code int}
         * @param ruleVersion 乐观锁或契约版本，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BillingRuleAggregate}
         */
        public static BillingRuleAggregate restore(String ruleNo, String objectCode, String feeType, BigDecimal unitPrice, BigDecimal taxRate, LocalDate effectiveFrom, LocalDate effectiveTo, int status, int ruleVersion, long version) {
            return new BillingRuleAggregate(ruleNo, objectCode, feeType, unitPrice, taxRate, effectiveFrom, effectiveTo, status, ruleVersion, version);
        }

        /**
         * 执行命令 {@code publish}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void publish(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != DRAFT) {
                throw new IllegalStateException("only draft billing rule can publish");
            }
            status = PUBLISHED;
            ruleVersion++;
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code effectiveOn}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param date 业务时间，类型为 {@code LocalDate}
         * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
         */
        public boolean effectiveOn(LocalDate date) {
            return status == PUBLISHED && !date.isBefore(effectiveFrom) && !date.isAfter(effectiveTo);
        }

        /**
         * 处理当前类型职责中的操作 {@code calculate}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param quantity 数量值，类型为 {@code BigDecimal}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code ChargeAmount}
         */
        public ChargeAmount calculate(BigDecimal quantity) {
            requirePositive(quantity, "quantity must be positive");
            BigDecimal amount = unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
            BigDecimal taxAmount = amount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
            return new ChargeAmount(amount, taxAmount, amount.add(taxAmount).setScale(2, RoundingMode.HALF_UP));
        }

        /**
         * 处理当前类型职责中的操作 {@code ruleNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String ruleNo() {
            return ruleNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code objectCode}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String objectCode() {
            return objectCode;
        }

        /**
         * 处理当前类型职责中的操作 {@code feeType}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String feeType() {
            return feeType;
        }

        /**
         * 处理当前类型职责中的操作 {@code unitPrice}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
         */
        public BigDecimal unitPrice() {
            return unitPrice;
        }

        /**
         * 处理当前类型职责中的操作 {@code taxRate}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
         */
        public BigDecimal taxRate() {
            return taxRate;
        }

        /**
         * 处理当前类型职责中的操作 {@code effectiveFrom}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code LocalDate}
         */
        public LocalDate effectiveFrom() {
            return effectiveFrom;
        }

        /**
         * 处理当前类型职责中的操作 {@code effectiveTo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code LocalDate}
         */
        public LocalDate effectiveTo() {
            return effectiveTo;
        }

        /**
         * 处理当前类型职责中的操作 {@code status}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        public int status() {
            return status;
        }

        /**
         * 处理当前类型职责中的操作 {@code ruleVersion}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        public int ruleVersion() {
            return ruleVersion;
        }

        /**
         * 处理当前类型职责中的操作 {@code version}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        public long version() {
            return version;
        }

        /**
         * 校验业务约束 {@code ensureVersion}。
         *
         * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("billing rule version conflict");
            }
        }
    }

    /**
     * ChargeSourceAggregate。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public static final class ChargeSourceAggregate {

        /**
         * PENDING（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int PENDING = 1;

        /**
         * ACCEPTED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int ACCEPTED = 2;

        /**
         * FAILED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int FAILED = 3;

        /**
         * sourceNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String sourceNo;

        /**
         * sourceSystem（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final String sourceSystem;

        /**
         * sourceEventId（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
         */
        private final String sourceEventId;

        /**
         * billingObjectCode（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String billingObjectCode;

        /**
         * feeType（类型：{@code String}）。
         *
         * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
         */
        private final String feeType;

        /**
         * quantity（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
         */
        private final BigDecimal quantity;

        /**
         * status（类型：{@code int}）。
         *
         * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
         */
        private int status;

        /**
         * failureReason（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private String failureReason;

        /**
         * version（类型：{@code long}）。
         *
         * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
         */
        private long version;

        /**
         * 创建 ChargeSourceAggregate。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param sourceNo 可追踪业务编码，类型为 {@code String}
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param sourceEventId 业务或技术标识，类型为 {@code String}
         * @param billingObjectCode 可追踪业务编码，类型为 {@code String}
         * @param feeType 金额或计费值，类型为 {@code String}
         * @param quantity 数量值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param failureReason 业务处理参数或成员，类型为 {@code String}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         */
        private ChargeSourceAggregate(String sourceNo, String sourceSystem, String sourceEventId, String billingObjectCode, String feeType, BigDecimal quantity, int status, String failureReason, long version) {
            require(sourceNo, "charge source no is required");
            require(sourceSystem, "source system is required");
            require(sourceEventId, "source event id is required");
            require(billingObjectCode, "billing object code is required");
            require(feeType, "fee type is required");
            requirePositive(quantity, "quantity must be positive");
            this.sourceNo = sourceNo;
            this.sourceSystem = sourceSystem;
            this.sourceEventId = sourceEventId;
            this.billingObjectCode = billingObjectCode;
            this.feeType = feeType;
            this.quantity = quantity.setScale(4, RoundingMode.HALF_UP);
            this.status = status;
            this.failureReason = failureReason;
            this.version = version;
        }

        /**
         * 执行命令 {@code create}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param sourceNo 可追踪业务编码，类型为 {@code String}
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param sourceEventId 业务或技术标识，类型为 {@code String}
         * @param billingObjectCode 可追踪业务编码，类型为 {@code String}
         * @param feeType 金额或计费值，类型为 {@code String}
         * @param quantity 数量值，类型为 {@code BigDecimal}
         * @return 执行命令的结果，类型为 {@code ChargeSourceAggregate}
         */
        public static ChargeSourceAggregate create(String sourceNo, String sourceSystem, String sourceEventId, String billingObjectCode, String feeType, BigDecimal quantity) {
            return new ChargeSourceAggregate(sourceNo, sourceSystem, sourceEventId, billingObjectCode, feeType, quantity, PENDING, null, 1);
        }

        /**
         * 处理当前类型职责中的操作 {@code restore}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param sourceNo 可追踪业务编码，类型为 {@code String}
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param sourceEventId 业务或技术标识，类型为 {@code String}
         * @param billingObjectCode 可追踪业务编码，类型为 {@code String}
         * @param feeType 金额或计费值，类型为 {@code String}
         * @param quantity 数量值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param failureReason 业务处理参数或成员，类型为 {@code String}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code ChargeSourceAggregate}
         */
        public static ChargeSourceAggregate restore(String sourceNo, String sourceSystem, String sourceEventId, String billingObjectCode, String feeType, BigDecimal quantity, int status, String failureReason, long version) {
            return new ChargeSourceAggregate(sourceNo, sourceSystem, sourceEventId, billingObjectCode, feeType, quantity, status, failureReason, version);
        }

        /**
         * 处理当前类型职责中的操作 {@code accept}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         */
        public void accept() {
            if (status != PENDING && status != FAILED) {
                throw new IllegalStateException("charge source cannot accept");
            }
            status = ACCEPTED;
            failureReason = null;
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code fail}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param reason 业务处理参数或成员，类型为 {@code String}
         */
        public void fail(String reason) {
            require(reason, "failure reason is required");
            status = FAILED;
            failureReason = reason;
            version++;
        }

        /**
         * 执行命令 {@code replay}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         */
        public void replay() {
            if (status != FAILED) {
                throw new IllegalStateException("only failed charge source can replay");
            }
            status = PENDING;
            failureReason = null;
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code sourceNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String sourceNo() {
            return sourceNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code sourceSystem}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String sourceSystem() {
            return sourceSystem;
        }

        /**
         * 处理当前类型职责中的操作 {@code sourceEventId}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String sourceEventId() {
            return sourceEventId;
        }

        /**
         * 处理当前类型职责中的操作 {@code billingObjectCode}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String billingObjectCode() {
            return billingObjectCode;
        }

        /**
         * 处理当前类型职责中的操作 {@code feeType}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String feeType() {
            return feeType;
        }

        /**
         * 处理当前类型职责中的操作 {@code quantity}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
         */
        public BigDecimal quantity() {
            return quantity;
        }

        /**
         * 处理当前类型职责中的操作 {@code status}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        public int status() {
            return status;
        }

        /**
         * 处理当前类型职责中的操作 {@code failureReason}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String failureReason() {
            return failureReason;
        }

        /**
         * 处理当前类型职责中的操作 {@code version}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        public long version() {
            return version;
        }
    }

    /**
     * ChargeDetailAggregate。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public static final class ChargeDetailAggregate {

        /**
         * PENDING_RECONCILIATION（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int PENDING_RECONCILIATION = 1;

        /**
         * CONFIRMED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int CONFIRMED = 2;

        /**
         * VOIDED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int VOIDED = 3;

        /**
         * POSTED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int POSTED = 4;

        /**
         * chargeNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String chargeNo;

        /**
         * sourceNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String sourceNo;

        /**
         * objectCode（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String objectCode;

        /**
         * feeType（类型：{@code String}）。
         *
         * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
         */
        private final String feeType;

        /**
         * ruleNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String ruleNo;

        /**
         * quantity（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
         */
        private BigDecimal quantity;

        /**
         * unitPrice（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
         */
        private BigDecimal unitPrice;

        /**
         * amount（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
         */
        private BigDecimal amount;

        /**
         * taxAmount（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
         */
        private BigDecimal taxAmount;

        /**
         * totalAmount（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
         */
        private BigDecimal totalAmount;

        /**
         * status（类型：{@code int}）。
         *
         * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
         */
        private int status;

        /**
         * version（类型：{@code long}）。
         *
         * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
         */
        private long version;

        /**
         * 创建 ChargeDetailAggregate。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param chargeNo 可追踪业务编码，类型为 {@code String}
         * @param sourceNo 可追踪业务编码，类型为 {@code String}
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param feeType 金额或计费值，类型为 {@code String}
         * @param ruleNo 可追踪业务编码，类型为 {@code String}
         * @param quantity 数量值，类型为 {@code BigDecimal}
         * @param unitPrice 金额或计费值，类型为 {@code BigDecimal}
         * @param amount 金额或计费值，类型为 {@code BigDecimal}
         * @param taxAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param totalAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         */
        private ChargeDetailAggregate(String chargeNo, String sourceNo, String objectCode, String feeType, String ruleNo, BigDecimal quantity, BigDecimal unitPrice, BigDecimal amount, BigDecimal taxAmount, BigDecimal totalAmount, int status, long version) {
            require(chargeNo, "charge no is required");
            require(sourceNo, "source no is required");
            require(objectCode, "billing object code is required");
            require(feeType, "fee type is required");
            require(ruleNo, "rule no is required");
            this.chargeNo = chargeNo;
            this.sourceNo = sourceNo;
            this.objectCode = objectCode;
            this.feeType = feeType;
            this.ruleNo = ruleNo;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.amount = amount;
            this.taxAmount = taxAmount;
            this.totalAmount = totalAmount;
            this.status = status;
            this.version = version;
        }

        /**
         * 执行命令 {@code create}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param chargeNo 可追踪业务编码，类型为 {@code String}
         * @param sourceNo 可追踪业务编码，类型为 {@code String}
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param feeType 金额或计费值，类型为 {@code String}
         * @param ruleNo 可追踪业务编码，类型为 {@code String}
         * @param quantity 数量值，类型为 {@code BigDecimal}
         * @param unitPrice 金额或计费值，类型为 {@code BigDecimal}
         * @param amount 金额或计费值，类型为 {@code ChargeAmount}
         * @return 执行命令的结果，类型为 {@code ChargeDetailAggregate}
         */
        public static ChargeDetailAggregate create(String chargeNo, String sourceNo, String objectCode, String feeType, String ruleNo, BigDecimal quantity, BigDecimal unitPrice, ChargeAmount amount) {
            return new ChargeDetailAggregate(chargeNo, sourceNo, objectCode, feeType, ruleNo, quantity, unitPrice, amount.amount(), amount.taxAmount(), amount.totalAmount(), PENDING_RECONCILIATION, 1);
        }

        /**
         * 处理当前类型职责中的操作 {@code restore}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param chargeNo 可追踪业务编码，类型为 {@code String}
         * @param sourceNo 可追踪业务编码，类型为 {@code String}
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param feeType 金额或计费值，类型为 {@code String}
         * @param ruleNo 可追踪业务编码，类型为 {@code String}
         * @param quantity 数量值，类型为 {@code BigDecimal}
         * @param unitPrice 金额或计费值，类型为 {@code BigDecimal}
         * @param amount 金额或计费值，类型为 {@code BigDecimal}
         * @param taxAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param totalAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code ChargeDetailAggregate}
         */
        public static ChargeDetailAggregate restore(String chargeNo, String sourceNo, String objectCode, String feeType, String ruleNo, BigDecimal quantity, BigDecimal unitPrice, BigDecimal amount, BigDecimal taxAmount, BigDecimal totalAmount, int status, long version) {
            return new ChargeDetailAggregate(chargeNo, sourceNo, objectCode, feeType, ruleNo, quantity, unitPrice, amount, taxAmount, totalAmount, status, version);
        }

        /**
         * 处理当前类型职责中的操作 {@code recalculate}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param newQuantity 数量值，类型为 {@code BigDecimal}
         * @param newUnitPrice 金额或计费值，类型为 {@code BigDecimal}
         * @param newAmount 金额或计费值，类型为 {@code ChargeAmount}
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void recalculate(BigDecimal newQuantity, BigDecimal newUnitPrice, ChargeAmount newAmount, long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != PENDING_RECONCILIATION) {
                throw new IllegalStateException("confirmed charge must use adjustment instead of recalculation");
            }
            quantity = newQuantity;
            unitPrice = newUnitPrice;
            amount = newAmount.amount();
            taxAmount = newAmount.taxAmount();
            totalAmount = newAmount.totalAmount();
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code voidCharge}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void voidCharge(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status == POSTED || status == VOIDED) {
                throw new IllegalStateException("charge cannot void");
            }
            status = VOIDED;
            version++;
        }

        /**
         * 执行命令 {@code confirm}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         */
        public void confirm() {
            if (status != PENDING_RECONCILIATION) {
                throw new IllegalStateException("only pending charge can confirm");
            }
            status = CONFIRMED;
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code post}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         */
        public void post() {
            if (status != CONFIRMED) {
                throw new IllegalStateException("only confirmed charge can post");
            }
            status = POSTED;
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code chargeNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String chargeNo() {
            return chargeNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code sourceNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String sourceNo() {
            return sourceNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code objectCode}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String objectCode() {
            return objectCode;
        }

        /**
         * 处理当前类型职责中的操作 {@code feeType}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String feeType() {
            return feeType;
        }

        /**
         * 处理当前类型职责中的操作 {@code ruleNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String ruleNo() {
            return ruleNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code quantity}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
         */
        public BigDecimal quantity() {
            return quantity;
        }

        /**
         * 处理当前类型职责中的操作 {@code unitPrice}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
         */
        public BigDecimal unitPrice() {
            return unitPrice;
        }

        /**
         * 处理当前类型职责中的操作 {@code amount}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
         */
        public BigDecimal amount() {
            return amount;
        }

        /**
         * 处理当前类型职责中的操作 {@code taxAmount}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
         */
        public BigDecimal taxAmount() {
            return taxAmount;
        }

        /**
         * 转换数据模型 {@code totalAmount}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 转换数据模型的结果，类型为 {@code BigDecimal}
         */
        public BigDecimal totalAmount() {
            return totalAmount;
        }

        /**
         * 处理当前类型职责中的操作 {@code status}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        public int status() {
            return status;
        }

        /**
         * 处理当前类型职责中的操作 {@code version}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        public long version() {
            return version;
        }

        /**
         * 校验业务约束 {@code ensureVersion}。
         *
         * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("charge detail version conflict");
            }
        }
    }

    /**
     * ChargeAdjustmentAggregate。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public static final class ChargeAdjustmentAggregate {

        /**
         * DRAFT（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int DRAFT = 1;

        /**
         * APPROVED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int APPROVED = 2;

        /**
         * EXECUTED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int EXECUTED = 3;

        /**
         * adjustmentNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String adjustmentNo;

        /**
         * originalChargeNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String originalChargeNo;

        /**
         * adjustAmount（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
         */
        private final BigDecimal adjustAmount;

        /**
         * status（类型：{@code int}）。
         *
         * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
         */
        private int status;

        /**
         * version（类型：{@code long}）。
         *
         * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
         */
        private long version;

        /**
         * 创建 ChargeAdjustmentAggregate。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param adjustmentNo 可追踪业务编码，类型为 {@code String}
         * @param originalChargeNo 可追踪业务编码，类型为 {@code String}
         * @param adjustAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         */
        private ChargeAdjustmentAggregate(String adjustmentNo, String originalChargeNo, BigDecimal adjustAmount, int status, long version) {
            require(adjustmentNo, "adjustment no is required");
            require(originalChargeNo, "original charge no is required");
            if (adjustAmount == null || adjustAmount.compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalArgumentException("adjust amount cannot be zero");
            }
            this.adjustmentNo = adjustmentNo;
            this.originalChargeNo = originalChargeNo;
            this.adjustAmount = adjustAmount.setScale(2, RoundingMode.HALF_UP);
            this.status = status;
            this.version = version;
        }

        /**
         * 执行命令 {@code create}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param adjustmentNo 可追踪业务编码，类型为 {@code String}
         * @param originalChargeNo 可追踪业务编码，类型为 {@code String}
         * @param adjustAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param approved 业务处理参数或成员，类型为 {@code boolean}
         * @return 执行命令的结果，类型为 {@code ChargeAdjustmentAggregate}
         */
        public static ChargeAdjustmentAggregate create(String adjustmentNo, String originalChargeNo, BigDecimal adjustAmount, boolean approved) {
            return new ChargeAdjustmentAggregate(adjustmentNo, originalChargeNo, adjustAmount, approved ? APPROVED : DRAFT, 1);
        }

        /**
         * 处理当前类型职责中的操作 {@code restore}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param adjustmentNo 可追踪业务编码，类型为 {@code String}
         * @param originalChargeNo 可追踪业务编码，类型为 {@code String}
         * @param adjustAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code ChargeAdjustmentAggregate}
         */
        public static ChargeAdjustmentAggregate restore(String adjustmentNo, String originalChargeNo, BigDecimal adjustAmount, int status, long version) {
            return new ChargeAdjustmentAggregate(adjustmentNo, originalChargeNo, adjustAmount, status, version);
        }

        /**
         * 处理当前类型职责中的操作 {@code execute}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void execute(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != APPROVED) {
                throw new IllegalStateException("only approved adjustment can execute");
            }
            status = EXECUTED;
            version++;
        }

        /**
         * 执行命令 {@code adjustmentNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 执行命令的结果，类型为 {@code String}
         */
        public String adjustmentNo() {
            return adjustmentNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code originalChargeNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String originalChargeNo() {
            return originalChargeNo;
        }

        /**
         * 执行命令 {@code adjustAmount}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 执行命令的结果，类型为 {@code BigDecimal}
         */
        public BigDecimal adjustAmount() {
            return adjustAmount;
        }

        /**
         * 处理当前类型职责中的操作 {@code status}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        public int status() {
            return status;
        }

        /**
         * 处理当前类型职责中的操作 {@code version}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        public long version() {
            return version;
        }

        /**
         * 校验业务约束 {@code ensureVersion}。
         *
         * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("adjustment version conflict");
            }
        }
    }

    /**
     * ReconciliationAggregate。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public static final class ReconciliationAggregate {

        /**
         * WAIT_CONFIRM（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int WAIT_CONFIRM = 1;

        /**
         * DIFFERENCE（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int DIFFERENCE = 2;

        /**
         * CONFIRMED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int CONFIRMED = 3;

        /**
         * BILLED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int BILLED = 4;

        /**
         * reconciliationNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String reconciliationNo;

        /**
         * objectCode（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String objectCode;

        /**
         * period（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final String period;

        /**
         * totalAmount（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
         */
        private final BigDecimal totalAmount;

        /**
         * status（类型：{@code int}）。
         *
         * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
         */
        private int status;

        /**
         * version（类型：{@code long}）。
         *
         * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
         */
        private long version;

        /**
         * 创建 ReconciliationAggregate。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param reconciliationNo 可追踪业务编码，类型为 {@code String}
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param period 业务处理参数或成员，类型为 {@code String}
         * @param totalAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         */
        private ReconciliationAggregate(String reconciliationNo, String objectCode, String period, BigDecimal totalAmount, int status, long version) {
            require(reconciliationNo, "reconciliation no is required");
            require(objectCode, "billing object code is required");
            require(period, "billing period is required");
            requirePositive(totalAmount, "reconciliation amount must be positive");
            this.reconciliationNo = reconciliationNo;
            this.objectCode = objectCode;
            this.period = period;
            this.totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
            this.status = status;
            this.version = version;
        }

        /**
         * 执行命令 {@code create}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param reconciliationNo 可追踪业务编码，类型为 {@code String}
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param period 业务处理参数或成员，类型为 {@code String}
         * @param totalAmount 金额或计费值，类型为 {@code BigDecimal}
         * @return 执行命令的结果，类型为 {@code ReconciliationAggregate}
         */
        public static ReconciliationAggregate create(String reconciliationNo, String objectCode, String period, BigDecimal totalAmount) {
            return new ReconciliationAggregate(reconciliationNo, objectCode, period, totalAmount, WAIT_CONFIRM, 1);
        }

        /**
         * 处理当前类型职责中的操作 {@code restore}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param reconciliationNo 可追踪业务编码，类型为 {@code String}
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param period 业务处理参数或成员，类型为 {@code String}
         * @param totalAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code ReconciliationAggregate}
         */
        public static ReconciliationAggregate restore(String reconciliationNo, String objectCode, String period, BigDecimal totalAmount, int status, long version) {
            return new ReconciliationAggregate(reconciliationNo, objectCode, period, totalAmount, status, version);
        }

        /**
         * 处理当前类型职责中的操作 {@code raiseDifference}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param peerAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void raiseDifference(BigDecimal peerAmount, long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != WAIT_CONFIRM) {
                throw new IllegalStateException("reconciliation cannot raise difference");
            }
            if (peerAmount == null || peerAmount.compareTo(totalAmount) == 0) {
                throw new IllegalArgumentException("difference amount is required");
            }
            status = DIFFERENCE;
            version++;
        }

        /**
         * 执行命令 {@code confirm}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param confirmedAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void confirm(BigDecimal confirmedAmount, long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != WAIT_CONFIRM && status != DIFFERENCE) {
                throw new IllegalStateException("reconciliation cannot confirm");
            }
            if (confirmedAmount == null || confirmedAmount.setScale(DIFFERENCE, RoundingMode.HALF_UP).compareTo(totalAmount) != 0) {
                throw new IllegalArgumentException("confirmed amount must equal reconciliation amount");
            }
            status = CONFIRMED;
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code markBilled}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         */
        public void markBilled() {
            if (status != CONFIRMED) {
                throw new IllegalStateException("only confirmed reconciliation can bill");
            }
            status = BILLED;
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code reconciliationNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String reconciliationNo() {
            return reconciliationNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code objectCode}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String objectCode() {
            return objectCode;
        }

        /**
         * 处理当前类型职责中的操作 {@code period}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String period() {
            return period;
        }

        /**
         * 转换数据模型 {@code totalAmount}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 转换数据模型的结果，类型为 {@code BigDecimal}
         */
        public BigDecimal totalAmount() {
            return totalAmount;
        }

        /**
         * 处理当前类型职责中的操作 {@code status}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        public int status() {
            return status;
        }

        /**
         * 处理当前类型职责中的操作 {@code version}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        public long version() {
            return version;
        }

        /**
         * 校验业务约束 {@code ensureVersion}。
         *
         * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("reconciliation version conflict");
            }
        }
    }

    /**
     * BillAggregate。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public static final class BillAggregate {

        /**
         * GENERATED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int GENERATED = 1;

        /**
         * CONFIRMED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int CONFIRMED = 2;

        /**
         * INVOICED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int INVOICED = 3;

        /**
         * POSTED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int POSTED = 4;

        /**
         * CLOSED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int CLOSED = 5;

        /**
         * billNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String billNo;

        /**
         * reconciliationNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String reconciliationNo;

        /**
         * objectCode（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String objectCode;

        /**
         * totalAmount（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
         */
        private final BigDecimal totalAmount;

        /**
         * status（类型：{@code int}）。
         *
         * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
         */
        private int status;

        /**
         * version（类型：{@code long}）。
         *
         * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
         */
        private long version;

        /**
         * 创建 BillAggregate。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param billNo 可追踪业务编码，类型为 {@code String}
         * @param reconciliationNo 可追踪业务编码，类型为 {@code String}
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param totalAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         */
        private BillAggregate(String billNo, String reconciliationNo, String objectCode, BigDecimal totalAmount, int status, long version) {
            require(billNo, "bill no is required");
            require(reconciliationNo, "reconciliation no is required");
            require(objectCode, "billing object code is required");
            requirePositive(totalAmount, "bill amount must be positive");
            this.billNo = billNo;
            this.reconciliationNo = reconciliationNo;
            this.objectCode = objectCode;
            this.totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
            this.status = status;
            this.version = version;
        }

        /**
         * 执行命令 {@code create}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param billNo 可追踪业务编码，类型为 {@code String}
         * @param reconciliationNo 可追踪业务编码，类型为 {@code String}
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param totalAmount 金额或计费值，类型为 {@code BigDecimal}
         * @return 执行命令的结果，类型为 {@code BillAggregate}
         */
        public static BillAggregate create(String billNo, String reconciliationNo, String objectCode, BigDecimal totalAmount) {
            return new BillAggregate(billNo, reconciliationNo, objectCode, totalAmount, GENERATED, 1);
        }

        /**
         * 处理当前类型职责中的操作 {@code restore}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param billNo 可追踪业务编码，类型为 {@code String}
         * @param reconciliationNo 可追踪业务编码，类型为 {@code String}
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param totalAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BillAggregate}
         */
        public static BillAggregate restore(String billNo, String reconciliationNo, String objectCode, BigDecimal totalAmount, int status, long version) {
            return new BillAggregate(billNo, reconciliationNo, objectCode, totalAmount, status, version);
        }

        /**
         * 执行命令 {@code confirm}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void confirm(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != GENERATED) {
                throw new IllegalStateException("bill cannot confirm");
            }
            status = CONFIRMED;
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code markInvoiced}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void markInvoiced(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != CONFIRMED) {
                throw new IllegalStateException("only confirmed bill can invoice");
            }
            status = INVOICED;
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code markPosted}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void markPosted(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != CONFIRMED && status != INVOICED) {
                throw new IllegalStateException("bill cannot post");
            }
            status = POSTED;
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code billNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String billNo() {
            return billNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code reconciliationNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String reconciliationNo() {
            return reconciliationNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code objectCode}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String objectCode() {
            return objectCode;
        }

        /**
         * 转换数据模型 {@code totalAmount}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 转换数据模型的结果，类型为 {@code BigDecimal}
         */
        public BigDecimal totalAmount() {
            return totalAmount;
        }

        /**
         * 处理当前类型职责中的操作 {@code status}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        public int status() {
            return status;
        }

        /**
         * 处理当前类型职责中的操作 {@code version}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        public long version() {
            return version;
        }

        /**
         * 校验业务约束 {@code ensureVersion}。
         *
         * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("bill version conflict");
            }
        }
    }

    /**
     * InvoiceAggregate。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public static final class InvoiceAggregate {

        /**
         * REQUESTED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int REQUESTED = 1;

        /**
         * ISSUED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int ISSUED = 2;

        /**
         * invoiceNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String invoiceNo;

        /**
         * billNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String billNo;

        /**
         * invoiceAmount（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
         */
        private final BigDecimal invoiceAmount;

        /**
         * status（类型：{@code int}）。
         *
         * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
         */
        private int status;

        /**
         * version（类型：{@code long}）。
         *
         * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
         */
        private long version;

        /**
         * 创建 InvoiceAggregate。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param invoiceNo 可追踪业务编码，类型为 {@code String}
         * @param billNo 可追踪业务编码，类型为 {@code String}
         * @param invoiceAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         */
        private InvoiceAggregate(String invoiceNo, String billNo, BigDecimal invoiceAmount, int status, long version) {
            require(invoiceNo, "invoice no is required");
            require(billNo, "bill no is required");
            requirePositive(invoiceAmount, "invoice amount must be positive");
            this.invoiceNo = invoiceNo;
            this.billNo = billNo;
            this.invoiceAmount = invoiceAmount.setScale(2, RoundingMode.HALF_UP);
            this.status = status;
            this.version = version;
        }

        /**
         * 处理当前类型职责中的操作 {@code request}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param invoiceNo 可追踪业务编码，类型为 {@code String}
         * @param billNo 可追踪业务编码，类型为 {@code String}
         * @param invoiceAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param billAmount 金额或计费值，类型为 {@code BigDecimal}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code InvoiceAggregate}
         */
        public static InvoiceAggregate request(String invoiceNo, String billNo, BigDecimal invoiceAmount, BigDecimal billAmount) {
            if (invoiceAmount.compareTo(billAmount) > 0) {
                throw new IllegalArgumentException("invoice amount cannot exceed bill amount");
            }
            return new InvoiceAggregate(invoiceNo, billNo, invoiceAmount, REQUESTED, 1);
        }

        /**
         * 处理当前类型职责中的操作 {@code restore}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param invoiceNo 可追踪业务编码，类型为 {@code String}
         * @param billNo 可追踪业务编码，类型为 {@code String}
         * @param invoiceAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code InvoiceAggregate}
         */
        public static InvoiceAggregate restore(String invoiceNo, String billNo, BigDecimal invoiceAmount, int status, long version) {
            return new InvoiceAggregate(invoiceNo, billNo, invoiceAmount, status, version);
        }

        /**
         * 处理当前类型职责中的操作 {@code issue}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void issue(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != REQUESTED) {
                throw new IllegalStateException("invoice cannot issue");
            }
            status = ISSUED;
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code invoiceNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String invoiceNo() {
            return invoiceNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code billNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String billNo() {
            return billNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code invoiceAmount}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
         */
        public BigDecimal invoiceAmount() {
            return invoiceAmount;
        }

        /**
         * 处理当前类型职责中的操作 {@code status}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        public int status() {
            return status;
        }

        /**
         * 处理当前类型职责中的操作 {@code version}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        public long version() {
            return version;
        }

        /**
         * 校验业务约束 {@code ensureVersion}。
         *
         * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("invoice version conflict");
            }
        }
    }

    /**
     * FinanceHandoverAggregate。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public static final class FinanceHandoverAggregate {

        /**
         * REQUESTED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int REQUESTED = 1;

        /**
         * POSTED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int POSTED = 2;

        /**
         * FAILED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int FAILED = 3;

        /**
         * handoverNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String handoverNo;

        /**
         * billNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String billNo;

        /**
         * status（类型：{@code int}）。
         *
         * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
         */
        private int status;

        /**
         * voucherNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private String voucherNo;

        /**
         * failureReason（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private String failureReason;

        /**
         * version（类型：{@code long}）。
         *
         * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
         */
        private long version;

        /**
         * 创建 FinanceHandoverAggregate。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param handoverNo 可追踪业务编码，类型为 {@code String}
         * @param billNo 可追踪业务编码，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param voucherNo 可追踪业务编码，类型为 {@code String}
         * @param failureReason 业务处理参数或成员，类型为 {@code String}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         */
        private FinanceHandoverAggregate(String handoverNo, String billNo, int status, String voucherNo, String failureReason, long version) {
            require(handoverNo, "finance handover no is required");
            require(billNo, "bill no is required");
            this.handoverNo = handoverNo;
            this.billNo = billNo;
            this.status = status;
            this.voucherNo = voucherNo;
            this.failureReason = failureReason;
            this.version = version;
        }

        /**
         * 处理当前类型职责中的操作 {@code request}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param handoverNo 可追踪业务编码，类型为 {@code String}
         * @param billNo 可追踪业务编码，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code FinanceHandoverAggregate}
         */
        public static FinanceHandoverAggregate request(String handoverNo, String billNo) {
            return new FinanceHandoverAggregate(handoverNo, billNo, REQUESTED, null, null, 1);
        }

        /**
         * 处理当前类型职责中的操作 {@code restore}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param handoverNo 可追踪业务编码，类型为 {@code String}
         * @param billNo 可追踪业务编码，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param voucherNo 可追踪业务编码，类型为 {@code String}
         * @param failureReason 业务处理参数或成员，类型为 {@code String}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code FinanceHandoverAggregate}
         */
        public static FinanceHandoverAggregate restore(String handoverNo, String billNo, int status, String voucherNo, String failureReason, long version) {
            return new FinanceHandoverAggregate(handoverNo, billNo, status, voucherNo, failureReason, version);
        }

        /**
         * 处理当前类型职责中的操作 {@code post}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param voucherNo 可追踪业务编码，类型为 {@code String}
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void post(String voucherNo, long expectedVersion) {
            ensureVersion(expectedVersion);
            require(voucherNo, "voucher no is required");
            if (status != REQUESTED && status != FAILED) {
                throw new IllegalStateException("finance handover cannot post");
            }
            status = POSTED;
            this.voucherNo = voucherNo;
            failureReason = null;
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code fail}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param reason 业务处理参数或成员，类型为 {@code String}
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void fail(String reason, long expectedVersion) {
            ensureVersion(expectedVersion);
            require(reason, "failure reason is required");
            if (status == POSTED) {
                throw new IllegalStateException("posted finance handover cannot fail");
            }
            status = FAILED;
            failureReason = reason;
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code handoverNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String handoverNo() {
            return handoverNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code billNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String billNo() {
            return billNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code status}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        public int status() {
            return status;
        }

        /**
         * 处理当前类型职责中的操作 {@code voucherNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String voucherNo() {
            return voucherNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code failureReason}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String failureReason() {
            return failureReason;
        }

        /**
         * 处理当前类型职责中的操作 {@code version}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        public long version() {
            return version;
        }

        /**
         * 校验业务约束 {@code ensureVersion}。
         *
         * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("finance handover version conflict");
            }
        }
    }

    /**
     * RefundSettlementAggregate。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public static final class RefundSettlementAggregate {

        /**
         * REQUESTED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int REQUESTED = 1;

        /**
         * FINISHED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int FINISHED = 2;

        /**
         * FAILED（类型：{@code int}）。
         *
         * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
         */
        public static final int FAILED = 3;

        /**
         * refundNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String refundNo;

        /**
         * billNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String billNo;

        /**
         * refundAmount（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
         */
        private final BigDecimal refundAmount;

        /**
         * status（类型：{@code int}）。
         *
         * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
         */
        private int status;

        /**
         * failureReason（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private String failureReason;

        /**
         * version（类型：{@code long}）。
         *
         * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
         */
        private long version;

        /**
         * 创建 RefundSettlementAggregate。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param refundNo 可追踪业务编码，类型为 {@code String}
         * @param billNo 可追踪业务编码，类型为 {@code String}
         * @param refundAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param failureReason 业务处理参数或成员，类型为 {@code String}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         */
        private RefundSettlementAggregate(String refundNo, String billNo, BigDecimal refundAmount, int status, String failureReason, long version) {
            require(refundNo, "refund no is required");
            require(billNo, "bill no is required");
            requirePositive(refundAmount, "refund amount must be positive");
            this.refundNo = refundNo;
            this.billNo = billNo;
            this.refundAmount = refundAmount.setScale(2, RoundingMode.HALF_UP);
            this.status = status;
            this.failureReason = failureReason;
            this.version = version;
        }

        /**
         * 处理当前类型职责中的操作 {@code request}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param refundNo 可追踪业务编码，类型为 {@code String}
         * @param billNo 可追踪业务编码，类型为 {@code String}
         * @param refundAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param refundableAmount 金额或计费值，类型为 {@code BigDecimal}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code RefundSettlementAggregate}
         */
        public static RefundSettlementAggregate request(String refundNo, String billNo, BigDecimal refundAmount, BigDecimal refundableAmount) {
            if (refundAmount.compareTo(refundableAmount) > 0) {
                throw new IllegalArgumentException("refund amount cannot exceed refundable amount");
            }
            return new RefundSettlementAggregate(refundNo, billNo, refundAmount, REQUESTED, null, 1);
        }

        /**
         * 处理当前类型职责中的操作 {@code restore}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param refundNo 可追踪业务编码，类型为 {@code String}
         * @param billNo 可追踪业务编码，类型为 {@code String}
         * @param refundAmount 金额或计费值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param failureReason 业务处理参数或成员，类型为 {@code String}
         * @param version 乐观锁或契约版本，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code RefundSettlementAggregate}
         */
        public static RefundSettlementAggregate restore(String refundNo, String billNo, BigDecimal refundAmount, int status, String failureReason, long version) {
            return new RefundSettlementAggregate(refundNo, billNo, refundAmount, status, failureReason, version);
        }

        /**
         * 处理当前类型职责中的操作 {@code finish}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void finish(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != REQUESTED) {
                throw new IllegalStateException("refund cannot finish");
            }
            status = FINISHED;
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code fail}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param reason 业务处理参数或成员，类型为 {@code String}
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void fail(String reason, long expectedVersion) {
            ensureVersion(expectedVersion);
            require(reason, "failure reason is required");
            if (status != REQUESTED) {
                throw new IllegalStateException("refund cannot fail");
            }
            status = FAILED;
            failureReason = reason;
            version++;
        }

        /**
         * 执行命令 {@code retry}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        public void retry(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != FAILED) {
                throw new IllegalStateException("only failed refund can retry");
            }
            status = REQUESTED;
            failureReason = null;
            version++;
        }

        /**
         * 处理当前类型职责中的操作 {@code refundNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String refundNo() {
            return refundNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code billNo}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String billNo() {
            return billNo;
        }

        /**
         * 处理当前类型职责中的操作 {@code refundAmount}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
         */
        public BigDecimal refundAmount() {
            return refundAmount;
        }

        /**
         * 处理当前类型职责中的操作 {@code status}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        public int status() {
            return status;
        }

        /**
         * 处理当前类型职责中的操作 {@code failureReason}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        public String failureReason() {
            return failureReason;
        }

        /**
         * 处理当前类型职责中的操作 {@code version}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        public long version() {
            return version;
        }

        /**
         * 校验业务约束 {@code ensureVersion}。
         *
         * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
         * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
         */
        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("refund settlement version conflict");
            }
        }
    }

    /**
     * ChargeAmount。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ChargeAmount(BigDecimal amount, BigDecimal taxAmount, BigDecimal totalAmount) {
    }

    /**
     * 查询并返回 {@code require}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @param message 业务处理参数或成员，类型为 {@code String}
     */
    private static void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 查询并返回 {@code requirePositive}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param value 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param message 业务处理参数或成员，类型为 {@code String}
     */
    private static void requirePositive(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 查询并返回 {@code requireNonNegative}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param value 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param message 业务处理参数或成员，类型为 {@code String}
     */
    private static void requireNonNegative(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
