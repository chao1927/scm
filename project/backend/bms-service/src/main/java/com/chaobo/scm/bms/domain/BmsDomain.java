package com.chaobo.scm.bms.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public final class BmsDomain {
    private BmsDomain() {}

    public static final class BillingObjectAggregate {
        public static final int ENABLED = 1;
        public static final int DISABLED = 2;

        private final String objectCode;
        private final String objectName;
        private final String objectType;
        private final String direction;
        private final String currency;
        private int status;
        private long version;

        private BillingObjectAggregate(String objectCode, String objectName, String objectType, String direction,
                                       String currency, int status, long version) {
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

        public static BillingObjectAggregate create(String objectCode, String objectName, String objectType,
                                                    String direction, String currency) {
            return new BillingObjectAggregate(objectCode, objectName, objectType, direction, currency, ENABLED, 1);
        }

        public static BillingObjectAggregate restore(String objectCode, String objectName, String objectType,
                                                     String direction, String currency, int status, long version) {
            return new BillingObjectAggregate(objectCode, objectName, objectType, direction, currency, status, version);
        }

        public void enable(long expectedVersion) {
            ensureVersion(expectedVersion);
            status = ENABLED;
            version++;
        }

        public void disable(long expectedVersion) {
            ensureVersion(expectedVersion);
            status = DISABLED;
            version++;
        }

        public void ensureEnabled() {
            if (status != ENABLED) {
                throw new IllegalStateException("disabled billing object cannot generate charge");
            }
        }

        public String objectCode() { return objectCode; }
        public String objectName() { return objectName; }
        public String objectType() { return objectType; }
        public String direction() { return direction; }
        public String currency() { return currency; }
        public int status() { return status; }
        public long version() { return version; }

        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("billing object version conflict");
            }
        }
    }

    public static final class BillingRuleAggregate {
        public static final int DRAFT = 1;
        public static final int PUBLISHED = 2;
        public static final int DISABLED = 3;

        private final String ruleNo;
        private final String objectCode;
        private final String feeType;
        private final BigDecimal unitPrice;
        private final BigDecimal taxRate;
        private final LocalDate effectiveFrom;
        private final LocalDate effectiveTo;
        private int status;
        private int ruleVersion;
        private long version;

        private BillingRuleAggregate(String ruleNo, String objectCode, String feeType, BigDecimal unitPrice,
                                     BigDecimal taxRate, LocalDate effectiveFrom, LocalDate effectiveTo, int status,
                                     int ruleVersion, long version) {
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

        public static BillingRuleAggregate create(String ruleNo, String objectCode, String feeType,
                                                  BigDecimal unitPrice, BigDecimal taxRate, LocalDate effectiveFrom,
                                                  LocalDate effectiveTo) {
            return new BillingRuleAggregate(ruleNo, objectCode, feeType, unitPrice, taxRate, effectiveFrom,
                    effectiveTo, DRAFT, 0, 1);
        }

        public static BillingRuleAggregate restore(String ruleNo, String objectCode, String feeType,
                                                   BigDecimal unitPrice, BigDecimal taxRate, LocalDate effectiveFrom,
                                                   LocalDate effectiveTo, int status, int ruleVersion, long version) {
            return new BillingRuleAggregate(ruleNo, objectCode, feeType, unitPrice, taxRate, effectiveFrom,
                    effectiveTo, status, ruleVersion, version);
        }

        public void publish(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != DRAFT) {
                throw new IllegalStateException("only draft billing rule can publish");
            }
            status = PUBLISHED;
            ruleVersion++;
            version++;
        }

        public boolean effectiveOn(LocalDate date) {
            return status == PUBLISHED && !date.isBefore(effectiveFrom) && !date.isAfter(effectiveTo);
        }

        public ChargeAmount calculate(BigDecimal quantity) {
            requirePositive(quantity, "quantity must be positive");
            BigDecimal amount = unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
            BigDecimal taxAmount = amount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
            return new ChargeAmount(amount, taxAmount, amount.add(taxAmount).setScale(2, RoundingMode.HALF_UP));
        }

        public String ruleNo() { return ruleNo; }
        public String objectCode() { return objectCode; }
        public String feeType() { return feeType; }
        public BigDecimal unitPrice() { return unitPrice; }
        public BigDecimal taxRate() { return taxRate; }
        public LocalDate effectiveFrom() { return effectiveFrom; }
        public LocalDate effectiveTo() { return effectiveTo; }
        public int status() { return status; }
        public int ruleVersion() { return ruleVersion; }
        public long version() { return version; }

        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("billing rule version conflict");
            }
        }
    }

    public static final class ChargeSourceAggregate {
        public static final int PENDING = 1;
        public static final int ACCEPTED = 2;
        public static final int FAILED = 3;

        private final String sourceNo;
        private final String sourceSystem;
        private final String sourceEventId;
        private final String billingObjectCode;
        private final String feeType;
        private final BigDecimal quantity;
        private int status;
        private String failureReason;
        private long version;

        private ChargeSourceAggregate(String sourceNo, String sourceSystem, String sourceEventId,
                                      String billingObjectCode, String feeType, BigDecimal quantity, int status,
                                      String failureReason, long version) {
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

        public static ChargeSourceAggregate create(String sourceNo, String sourceSystem, String sourceEventId,
                                                   String billingObjectCode, String feeType, BigDecimal quantity) {
            return new ChargeSourceAggregate(sourceNo, sourceSystem, sourceEventId, billingObjectCode, feeType,
                    quantity, PENDING, null, 1);
        }

        public static ChargeSourceAggregate restore(String sourceNo, String sourceSystem, String sourceEventId,
                                                    String billingObjectCode, String feeType, BigDecimal quantity,
                                                    int status, String failureReason, long version) {
            return new ChargeSourceAggregate(sourceNo, sourceSystem, sourceEventId, billingObjectCode, feeType,
                    quantity, status, failureReason, version);
        }

        public void accept() {
            if (status != PENDING && status != FAILED) {
                throw new IllegalStateException("charge source cannot accept");
            }
            status = ACCEPTED;
            failureReason = null;
            version++;
        }

        public void fail(String reason) {
            require(reason, "failure reason is required");
            status = FAILED;
            failureReason = reason;
            version++;
        }

        public void replay() {
            if (status != FAILED) {
                throw new IllegalStateException("only failed charge source can replay");
            }
            status = PENDING;
            failureReason = null;
            version++;
        }

        public String sourceNo() { return sourceNo; }
        public String sourceSystem() { return sourceSystem; }
        public String sourceEventId() { return sourceEventId; }
        public String billingObjectCode() { return billingObjectCode; }
        public String feeType() { return feeType; }
        public BigDecimal quantity() { return quantity; }
        public int status() { return status; }
        public String failureReason() { return failureReason; }
        public long version() { return version; }
    }

    public static final class ChargeDetailAggregate {
        public static final int PENDING_RECONCILIATION = 1;
        public static final int CONFIRMED = 2;
        public static final int VOIDED = 3;
        public static final int POSTED = 4;

        private final String chargeNo;
        private final String sourceNo;
        private final String objectCode;
        private final String feeType;
        private final String ruleNo;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
        private int status;
        private long version;

        private ChargeDetailAggregate(String chargeNo, String sourceNo, String objectCode, String feeType,
                                      String ruleNo, BigDecimal quantity, BigDecimal unitPrice, BigDecimal amount,
                                      BigDecimal taxAmount, BigDecimal totalAmount, int status, long version) {
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

        public static ChargeDetailAggregate create(String chargeNo, String sourceNo, String objectCode,
                                                   String feeType, String ruleNo, BigDecimal quantity,
                                                   BigDecimal unitPrice, ChargeAmount amount) {
            return new ChargeDetailAggregate(chargeNo, sourceNo, objectCode, feeType, ruleNo, quantity, unitPrice,
                    amount.amount(), amount.taxAmount(), amount.totalAmount(), PENDING_RECONCILIATION, 1);
        }

        public static ChargeDetailAggregate restore(String chargeNo, String sourceNo, String objectCode,
                                                    String feeType, String ruleNo, BigDecimal quantity,
                                                    BigDecimal unitPrice, BigDecimal amount, BigDecimal taxAmount,
                                                    BigDecimal totalAmount, int status, long version) {
            return new ChargeDetailAggregate(chargeNo, sourceNo, objectCode, feeType, ruleNo, quantity, unitPrice,
                    amount, taxAmount, totalAmount, status, version);
        }

        public void recalculate(BigDecimal newQuantity, BigDecimal newUnitPrice, ChargeAmount newAmount,
                                long expectedVersion) {
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

        public void voidCharge(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status == POSTED || status == VOIDED) {
                throw new IllegalStateException("charge cannot void");
            }
            status = VOIDED;
            version++;
        }

        public void confirm() {
            if (status != PENDING_RECONCILIATION) {
                throw new IllegalStateException("only pending charge can confirm");
            }
            status = CONFIRMED;
            version++;
        }

        public void post() {
            if (status != CONFIRMED) {
                throw new IllegalStateException("only confirmed charge can post");
            }
            status = POSTED;
            version++;
        }

        public String chargeNo() { return chargeNo; }
        public String sourceNo() { return sourceNo; }
        public String objectCode() { return objectCode; }
        public String feeType() { return feeType; }
        public String ruleNo() { return ruleNo; }
        public BigDecimal quantity() { return quantity; }
        public BigDecimal unitPrice() { return unitPrice; }
        public BigDecimal amount() { return amount; }
        public BigDecimal taxAmount() { return taxAmount; }
        public BigDecimal totalAmount() { return totalAmount; }
        public int status() { return status; }
        public long version() { return version; }

        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("charge detail version conflict");
            }
        }
    }

    public static final class ChargeAdjustmentAggregate {
        public static final int DRAFT = 1;
        public static final int APPROVED = 2;
        public static final int EXECUTED = 3;

        private final String adjustmentNo;
        private final String originalChargeNo;
        private final BigDecimal adjustAmount;
        private int status;
        private long version;

        private ChargeAdjustmentAggregate(String adjustmentNo, String originalChargeNo, BigDecimal adjustAmount,
                                          int status, long version) {
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

        public static ChargeAdjustmentAggregate create(String adjustmentNo, String originalChargeNo,
                                                       BigDecimal adjustAmount, boolean approved) {
            return new ChargeAdjustmentAggregate(adjustmentNo, originalChargeNo, adjustAmount,
                    approved ? APPROVED : DRAFT, 1);
        }

        public static ChargeAdjustmentAggregate restore(String adjustmentNo, String originalChargeNo,
                                                        BigDecimal adjustAmount, int status, long version) {
            return new ChargeAdjustmentAggregate(adjustmentNo, originalChargeNo, adjustAmount, status, version);
        }

        public void execute(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != APPROVED) {
                throw new IllegalStateException("only approved adjustment can execute");
            }
            status = EXECUTED;
            version++;
        }

        public String adjustmentNo() { return adjustmentNo; }
        public String originalChargeNo() { return originalChargeNo; }
        public BigDecimal adjustAmount() { return adjustAmount; }
        public int status() { return status; }
        public long version() { return version; }

        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("adjustment version conflict");
            }
        }
    }

    public static final class ReconciliationAggregate {
        public static final int WAIT_CONFIRM = 1;
        public static final int DIFFERENCE = 2;
        public static final int CONFIRMED = 3;
        public static final int BILLED = 4;

        private final String reconciliationNo;
        private final String objectCode;
        private final String period;
        private final BigDecimal totalAmount;
        private int status;
        private long version;

        private ReconciliationAggregate(String reconciliationNo, String objectCode, String period,
                                        BigDecimal totalAmount, int status, long version) {
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

        public static ReconciliationAggregate create(String reconciliationNo, String objectCode, String period,
                                                     BigDecimal totalAmount) {
            return new ReconciliationAggregate(reconciliationNo, objectCode, period, totalAmount, WAIT_CONFIRM, 1);
        }

        public static ReconciliationAggregate restore(String reconciliationNo, String objectCode, String period,
                                                      BigDecimal totalAmount, int status, long version) {
            return new ReconciliationAggregate(reconciliationNo, objectCode, period, totalAmount, status, version);
        }

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

        public void confirm(BigDecimal confirmedAmount, long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != WAIT_CONFIRM && status != DIFFERENCE) {
                throw new IllegalStateException("reconciliation cannot confirm");
            }
            if (confirmedAmount == null || confirmedAmount.setScale(2, RoundingMode.HALF_UP).compareTo(totalAmount) != 0) {
                throw new IllegalArgumentException("confirmed amount must equal reconciliation amount");
            }
            status = CONFIRMED;
            version++;
        }

        public void markBilled() {
            if (status != CONFIRMED) {
                throw new IllegalStateException("only confirmed reconciliation can bill");
            }
            status = BILLED;
            version++;
        }

        public String reconciliationNo() { return reconciliationNo; }
        public String objectCode() { return objectCode; }
        public String period() { return period; }
        public BigDecimal totalAmount() { return totalAmount; }
        public int status() { return status; }
        public long version() { return version; }

        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("reconciliation version conflict");
            }
        }
    }

    public static final class BillAggregate {
        public static final int GENERATED = 1;
        public static final int CONFIRMED = 2;
        public static final int INVOICED = 3;
        public static final int POSTED = 4;
        public static final int CLOSED = 5;

        private final String billNo;
        private final String reconciliationNo;
        private final String objectCode;
        private final BigDecimal totalAmount;
        private int status;
        private long version;

        private BillAggregate(String billNo, String reconciliationNo, String objectCode, BigDecimal totalAmount,
                              int status, long version) {
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

        public static BillAggregate create(String billNo, String reconciliationNo, String objectCode,
                                           BigDecimal totalAmount) {
            return new BillAggregate(billNo, reconciliationNo, objectCode, totalAmount, GENERATED, 1);
        }

        public static BillAggregate restore(String billNo, String reconciliationNo, String objectCode,
                                            BigDecimal totalAmount, int status, long version) {
            return new BillAggregate(billNo, reconciliationNo, objectCode, totalAmount, status, version);
        }

        public void confirm(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != GENERATED) {
                throw new IllegalStateException("bill cannot confirm");
            }
            status = CONFIRMED;
            version++;
        }

        public void markInvoiced(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != CONFIRMED) {
                throw new IllegalStateException("only confirmed bill can invoice");
            }
            status = INVOICED;
            version++;
        }

        public void markPosted(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != CONFIRMED && status != INVOICED) {
                throw new IllegalStateException("bill cannot post");
            }
            status = POSTED;
            version++;
        }

        public String billNo() { return billNo; }
        public String reconciliationNo() { return reconciliationNo; }
        public String objectCode() { return objectCode; }
        public BigDecimal totalAmount() { return totalAmount; }
        public int status() { return status; }
        public long version() { return version; }

        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("bill version conflict");
            }
        }
    }

    public static final class InvoiceAggregate {
        public static final int REQUESTED = 1;
        public static final int ISSUED = 2;

        private final String invoiceNo;
        private final String billNo;
        private final BigDecimal invoiceAmount;
        private int status;
        private long version;

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

        public static InvoiceAggregate request(String invoiceNo, String billNo, BigDecimal invoiceAmount,
                                               BigDecimal billAmount) {
            if (invoiceAmount.compareTo(billAmount) > 0) {
                throw new IllegalArgumentException("invoice amount cannot exceed bill amount");
            }
            return new InvoiceAggregate(invoiceNo, billNo, invoiceAmount, REQUESTED, 1);
        }

        public static InvoiceAggregate restore(String invoiceNo, String billNo, BigDecimal invoiceAmount,
                                               int status, long version) {
            return new InvoiceAggregate(invoiceNo, billNo, invoiceAmount, status, version);
        }

        public void issue(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != REQUESTED) {
                throw new IllegalStateException("invoice cannot issue");
            }
            status = ISSUED;
            version++;
        }

        public String invoiceNo() { return invoiceNo; }
        public String billNo() { return billNo; }
        public BigDecimal invoiceAmount() { return invoiceAmount; }
        public int status() { return status; }
        public long version() { return version; }

        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("invoice version conflict");
            }
        }
    }

    public static final class FinanceHandoverAggregate {
        public static final int REQUESTED = 1;
        public static final int POSTED = 2;
        public static final int FAILED = 3;

        private final String handoverNo;
        private final String billNo;
        private int status;
        private String voucherNo;
        private String failureReason;
        private long version;

        private FinanceHandoverAggregate(String handoverNo, String billNo, int status, String voucherNo,
                                         String failureReason, long version) {
            require(handoverNo, "finance handover no is required");
            require(billNo, "bill no is required");
            this.handoverNo = handoverNo;
            this.billNo = billNo;
            this.status = status;
            this.voucherNo = voucherNo;
            this.failureReason = failureReason;
            this.version = version;
        }

        public static FinanceHandoverAggregate request(String handoverNo, String billNo) {
            return new FinanceHandoverAggregate(handoverNo, billNo, REQUESTED, null, null, 1);
        }

        public static FinanceHandoverAggregate restore(String handoverNo, String billNo, int status, String voucherNo,
                                                       String failureReason, long version) {
            return new FinanceHandoverAggregate(handoverNo, billNo, status, voucherNo, failureReason, version);
        }

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

        public String handoverNo() { return handoverNo; }
        public String billNo() { return billNo; }
        public int status() { return status; }
        public String voucherNo() { return voucherNo; }
        public String failureReason() { return failureReason; }
        public long version() { return version; }

        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("finance handover version conflict");
            }
        }
    }

    public static final class RefundSettlementAggregate {
        public static final int REQUESTED = 1;
        public static final int FINISHED = 2;
        public static final int FAILED = 3;

        private final String refundNo;
        private final String billNo;
        private final BigDecimal refundAmount;
        private int status;
        private String failureReason;
        private long version;

        private RefundSettlementAggregate(String refundNo, String billNo, BigDecimal refundAmount, int status,
                                          String failureReason, long version) {
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

        public static RefundSettlementAggregate request(String refundNo, String billNo, BigDecimal refundAmount,
                                                        BigDecimal refundableAmount) {
            if (refundAmount.compareTo(refundableAmount) > 0) {
                throw new IllegalArgumentException("refund amount cannot exceed refundable amount");
            }
            return new RefundSettlementAggregate(refundNo, billNo, refundAmount, REQUESTED, null, 1);
        }

        public static RefundSettlementAggregate restore(String refundNo, String billNo, BigDecimal refundAmount,
                                                        int status, String failureReason, long version) {
            return new RefundSettlementAggregate(refundNo, billNo, refundAmount, status, failureReason, version);
        }

        public void finish(long expectedVersion) {
            ensureVersion(expectedVersion);
            if (status != REQUESTED) {
                throw new IllegalStateException("refund cannot finish");
            }
            status = FINISHED;
            version++;
        }

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

        public String refundNo() { return refundNo; }
        public String billNo() { return billNo; }
        public BigDecimal refundAmount() { return refundAmount; }
        public int status() { return status; }
        public String failureReason() { return failureReason; }
        public long version() { return version; }

        private void ensureVersion(long expectedVersion) {
            if (version != expectedVersion) {
                throw new IllegalStateException("refund settlement version conflict");
            }
        }
    }

    public record ChargeAmount(BigDecimal amount, BigDecimal taxAmount, BigDecimal totalAmount) {}

    private static void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void requirePositive(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void requireNonNegative(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
