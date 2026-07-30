package com.chaobo.scm.bms.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * BMS 八类标准页面读模型 Mapper。
 *
 * <p>每个查询结果都携带计费对象编码，应用层据此执行结算对象数据范围过滤。
 * 金额字段始终使用数据库 DECIMAL 映射到 {@link BigDecimal}。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface BmsReadQueryMapper {

    /**
     * 查询费用明细。
     *
     * @param objectCode 计费对象编码，可为空
     * @param billingPeriod 账期，可为空
     * @return 费用明细
     */
    @Select("""
        select d.charge_no chargeNo,d.source_no sourceNo,d.billing_object_code objectCode,
               o.object_name objectName,o.direction,o.currency,d.fee_type feeType,
               d.rule_no ruleNo,d.quantity,d.unit_price unitPrice,d.amount,
               d.tax_amount taxAmount,d.total_amount totalAmount,
               d.billing_period billingPeriod,d.status,d.updated_at updatedAt
          from bms_charge_detail d
          join bms_billing_object o on o.object_code=d.billing_object_code
         where (#{objectCode} is null or d.billing_object_code=#{objectCode})
           and (#{billingPeriod} is null or d.billing_period=#{billingPeriod})
         order by d.id desc
        """)
    List<ChargeView> listCharges(@Param("objectCode") String objectCode,
                                 @Param("billingPeriod") String billingPeriod);

    /**
     * 查询计费规则。
     *
     * @param objectCode 计费对象编码，可为空
     * @return 计费规则
     */
    @Select("""
        select r.rule_no ruleNo,r.object_code objectCode,o.object_name objectName,
               o.currency,r.fee_type feeType,r.unit_price unitPrice,r.tax_rate taxRate,
               r.effective_from effectiveFrom,r.effective_to effectiveTo,
               r.status,r.rule_version ruleVersion,r.updated_at updatedAt
          from bms_billing_rule r
          join bms_billing_object o on o.object_code=r.object_code
         where (#{objectCode} is null or r.object_code=#{objectCode})
         order by r.id desc
        """)
    List<RuleView> listRules(@Param("objectCode") String objectCode);

    /**
     * 查询对账单。
     *
     * @param billingPeriod 账期，可为空
     * @return 对账单
     */
    @Select("""
        select r.reconciliation_no reconciliationNo,r.billing_object_code objectCode,
               o.object_name objectName,o.direction,o.currency,r.billing_period billingPeriod,
               r.total_amount totalAmount,r.status,r.version,r.updated_at updatedAt
          from bms_reconciliation r
          join bms_billing_object o on o.object_code=r.billing_object_code
         where (#{billingPeriod} is null or r.billing_period=#{billingPeriod})
         order by r.id desc
        """)
    List<ReconciliationView> listReconciliations(
        @Param("billingPeriod") String billingPeriod);

    /**
     * 查询账单。
     *
     * @param billingPeriod 账期，可为空
     * @return 账单
     */
    @Select("""
        select b.bill_no billNo,b.reconciliation_no reconciliationNo,
               b.billing_object_code objectCode,o.object_name objectName,
               o.direction,o.currency,r.billing_period billingPeriod,
               b.total_amount totalAmount,b.status,b.version,b.updated_at updatedAt
          from bms_bill b
          join bms_reconciliation r on r.reconciliation_no=b.reconciliation_no
          join bms_billing_object o on o.object_code=b.billing_object_code
         where (#{billingPeriod} is null or r.billing_period=#{billingPeriod})
         order by b.id desc
        """)
    List<BillView> listBills(@Param("billingPeriod") String billingPeriod);

    /**
     * 查询发票。
     *
     * @param billingPeriod 账期，可为空
     * @return 发票
     */
    @Select("""
        select i.invoice_no invoiceNo,i.bill_no billNo,b.billing_object_code objectCode,
               o.object_name objectName,o.currency,r.billing_period billingPeriod,
               b.total_amount billAmount,i.invoice_amount invoiceAmount,
               i.status,i.version,i.updated_at updatedAt
          from bms_invoice i
          join bms_bill b on b.bill_no=i.bill_no
          join bms_reconciliation r on r.reconciliation_no=b.reconciliation_no
          join bms_billing_object o on o.object_code=b.billing_object_code
         where (#{billingPeriod} is null or r.billing_period=#{billingPeriod})
         order by i.id desc
        """)
    List<InvoiceView> listInvoices(@Param("billingPeriod") String billingPeriod);

    /**
     * 查询财务交接及其最新外部任务。
     *
     * @return 财务交接记录
     */
    @Select("""
        select f.handover_no handoverNo,f.bill_no billNo,b.billing_object_code objectCode,
               o.object_name objectName,o.currency,b.total_amount totalAmount,
               f.status,f.voucher_no voucherNo,f.failure_reason failureReason,
               e.task_no externalTaskNo,e.status externalTaskStatus,
               e.attempt_count externalAttemptCount,e.last_error externalLastError,
               f.version,f.updated_at updatedAt
          from bms_finance_handover f
          join bms_bill b on b.bill_no=f.bill_no
          join bms_billing_object o on o.object_code=b.billing_object_code
          left join bms_external_task e on e.id=(
               select max(x.id) from bms_external_task x
                where x.task_type='ERP_POST' and x.business_no=f.handover_no)
         order by f.id desc
        """)
    List<FinanceView> listFinanceHandovers();

    /**
     * 查询退款结算及其最新外部任务。
     *
     * @return 退款结算记录
     */
    @Select("""
        select f.refund_no refundNo,f.bill_no billNo,b.billing_object_code objectCode,
               o.object_name objectName,o.currency,b.total_amount billAmount,
               f.refund_amount refundAmount,f.status,f.failure_reason failureReason,
               e.task_no externalTaskNo,e.status externalTaskStatus,
               e.attempt_count externalAttemptCount,e.last_error externalLastError,
               f.version,f.updated_at updatedAt
          from bms_refund_settlement f
          join bms_bill b on b.bill_no=f.bill_no
          join bms_billing_object o on o.object_code=b.billing_object_code
          left join bms_external_task e on e.id=(
               select max(x.id) from bms_external_task x
                where x.task_type='PAYMENT_REFUND' and x.business_no=f.refund_no)
         order by f.id desc
        """)
    List<RefundView> listRefunds();

    /**
     * 按结算对象和期间查询严格金额口径的结算汇总。
     *
     * @param billingPeriod 账期，可为空
     * @return 结算汇总
     */
    @Select("""
        select o.object_code objectCode,o.object_name objectName,o.direction,o.currency,
               r.billing_period billingPeriod,
               coalesce((select sum(b.total_amount) from bms_bill b
                 join bms_reconciliation rb on rb.reconciliation_no=b.reconciliation_no
                where b.billing_object_code=o.object_code
                  and rb.billing_period=r.billing_period),0) billAmount,
               coalesce((select sum(i.invoice_amount) from bms_invoice i
                 join bms_bill bi on bi.bill_no=i.bill_no
                 join bms_reconciliation ri on ri.reconciliation_no=bi.reconciliation_no
                where bi.billing_object_code=o.object_code
                  and ri.billing_period=r.billing_period),0) invoiceAmount,
               coalesce((select sum(f.refund_amount) from bms_refund_settlement f
                 join bms_bill bf on bf.bill_no=f.bill_no
                 join bms_reconciliation rf on rf.reconciliation_no=bf.reconciliation_no
                where bf.billing_object_code=o.object_code
                  and rf.billing_period=r.billing_period),0) refundAmount
          from bms_billing_object o
          join bms_reconciliation r on r.billing_object_code=o.object_code
         where (#{billingPeriod} is null or r.billing_period=#{billingPeriod})
         group by o.object_code,o.object_name,o.direction,o.currency,r.billing_period
         order by r.billing_period desc,o.object_code
        """)
    List<SettlementView> listSettlementSummaries(
        @Param("billingPeriod") String billingPeriod);

    /**
     * 费用明细列表项。
     */
    record ChargeView(String chargeNo, String sourceNo, String objectCode,
                      String objectName, String direction, String currency,
                      String feeType, String ruleNo, BigDecimal quantity,
                      BigDecimal unitPrice, BigDecimal amount, BigDecimal taxAmount,
                      BigDecimal totalAmount, String billingPeriod, int status,
                      LocalDateTime updatedAt) {
    }

    /**
     * 计费规则列表项。
     */
    record RuleView(String ruleNo, String objectCode, String objectName, String currency,
                    String feeType, BigDecimal unitPrice, BigDecimal taxRate,
                    LocalDate effectiveFrom, LocalDate effectiveTo, int status,
                    int ruleVersion, LocalDateTime updatedAt) {
    }

    /**
     * 对账单列表项。
     */
    record ReconciliationView(String reconciliationNo, String objectCode,
                              String objectName, String direction, String currency,
                              String billingPeriod, BigDecimal totalAmount, int status,
                              long version, LocalDateTime updatedAt) {
    }

    /**
     * 账单列表项。
     */
    record BillView(String billNo, String reconciliationNo, String objectCode,
                    String objectName, String direction, String currency,
                    String billingPeriod, BigDecimal totalAmount, int status,
                    long version, LocalDateTime updatedAt) {
    }

    /**
     * 发票列表项。
     */
    record InvoiceView(String invoiceNo, String billNo, String objectCode,
                       String objectName, String currency, String billingPeriod,
                       BigDecimal billAmount, BigDecimal invoiceAmount, int status,
                       long version, LocalDateTime updatedAt) {
    }

    /**
     * 财务交接列表项。
     */
    record FinanceView(String handoverNo, String billNo, String objectCode,
                       String objectName, String currency, BigDecimal totalAmount,
                       int status, String voucherNo, String failureReason,
                       String externalTaskNo, Integer externalTaskStatus,
                       Integer externalAttemptCount, String externalLastError,
                       long version, LocalDateTime updatedAt) {
    }

    /**
     * 退款结算列表项。
     */
    record RefundView(String refundNo, String billNo, String objectCode,
                      String objectName, String currency, BigDecimal billAmount,
                      BigDecimal refundAmount, int status, String failureReason,
                      String externalTaskNo, Integer externalTaskStatus,
                      Integer externalAttemptCount, String externalLastError,
                      long version, LocalDateTime updatedAt) {
    }

    /**
     * 结算汇总列表项。
     */
    record SettlementView(String objectCode, String objectName, String direction,
                          String currency, String billingPeriod, BigDecimal billAmount,
                          BigDecimal invoiceAmount, BigDecimal refundAmount) {

        /**
         * 账单金额扣减退款后的净结算额。
         *
         * @return 净结算额
         */
        public BigDecimal netAmount() {
            return billAmount.subtract(refundAmount);
        }
    }
}
