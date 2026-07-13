package com.chaobo.scm.bms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BmsMapper {
    @Select("select object_code objectCode,object_name objectName,object_type objectType,direction,currency,status,version from bms_billing_object where object_code=#{objectCode}")
    BillingObjectRow findBillingObject(@Param("objectCode") String objectCode);

    @Select("select object_code objectCode,object_name objectName,object_type objectType,direction,currency,status,version from bms_billing_object where (#{status} is null or status=#{status}) order by id desc")
    List<BillingObjectRow> listBillingObjects(@Param("status") Integer status);

    @Insert("insert into bms_billing_object(object_code,object_name,object_type,direction,currency,status,version,created_at,updated_at) values(#{objectCode},#{objectName},#{objectType},#{direction},#{currency},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertBillingObject(BillingObjectRow row);

    @Update("update bms_billing_object set status=#{status},version=#{version},updated_at=now(3) where object_code=#{objectCode}")
    void updateBillingObject(BillingObjectRow row);

    @Select("select rule_no ruleNo,object_code objectCode,fee_type feeType,unit_price unitPrice,tax_rate taxRate,effective_from effectiveFrom,effective_to effectiveTo,status,rule_version ruleVersion,version from bms_billing_rule where rule_no=#{ruleNo}")
    BillingRuleRow findBillingRule(@Param("ruleNo") String ruleNo);

    @Select("select rule_no ruleNo,object_code objectCode,fee_type feeType,unit_price unitPrice,tax_rate taxRate,effective_from effectiveFrom,effective_to effectiveTo,status,rule_version ruleVersion,version from bms_billing_rule where object_code=#{objectCode} and fee_type=#{feeType} and status=2 order by rule_version desc,id desc limit 1")
    BillingRuleRow findPublishedRule(@Param("objectCode") String objectCode, @Param("feeType") String feeType);

    @Select("select count(1) from bms_billing_rule where object_code=#{objectCode} and fee_type=#{feeType} and status=2 and effective_from<=#{effectiveTo} and effective_to>=#{effectiveFrom}")
    int countPublishedRuleOverlap(@Param("objectCode") String objectCode, @Param("feeType") String feeType,
                                  @Param("effectiveFrom") LocalDate effectiveFrom,
                                  @Param("effectiveTo") LocalDate effectiveTo);

    @Select("select rule_no ruleNo,object_code objectCode,fee_type feeType,unit_price unitPrice,tax_rate taxRate,effective_from effectiveFrom,effective_to effectiveTo,status,rule_version ruleVersion,version from bms_billing_rule where object_code=#{objectCode} order by id desc")
    List<BillingRuleRow> listBillingRules(@Param("objectCode") String objectCode);

    @Insert("insert into bms_billing_rule(rule_no,object_code,fee_type,unit_price,tax_rate,effective_from,effective_to,status,rule_version,version,created_at,updated_at) values(#{ruleNo},#{objectCode},#{feeType},#{unitPrice},#{taxRate},#{effectiveFrom},#{effectiveTo},#{status},#{ruleVersion},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertBillingRule(BillingRuleRow row);

    @Update("update bms_billing_rule set status=#{status},rule_version=#{ruleVersion},version=#{version},updated_at=now(3) where rule_no=#{ruleNo}")
    void updateBillingRule(BillingRuleRow row);

    @Select("select source_no sourceNo,source_system sourceSystem,source_event_id sourceEventId,idempotency_key idempotencyKey,billing_object_code billingObjectCode,fee_type feeType,quantity,billing_period billingPeriod,payload,status,failure_reason failureReason,version from bms_charge_source where source_no=#{sourceNo}")
    ChargeSourceRow findChargeSource(@Param("sourceNo") String sourceNo);

    @Select("select source_no sourceNo,source_system sourceSystem,source_event_id sourceEventId,idempotency_key idempotencyKey,billing_object_code billingObjectCode,fee_type feeType,quantity,billing_period billingPeriod,payload,status,failure_reason failureReason,version from bms_charge_source where source_system=#{sourceSystem} and idempotency_key=#{idempotencyKey} order by id limit 1")
    ChargeSourceRow findChargeSourceByIdempotency(@Param("sourceSystem") String sourceSystem,
                                                  @Param("idempotencyKey") String idempotencyKey);

    @Select("select source_no sourceNo,source_system sourceSystem,source_event_id sourceEventId,idempotency_key idempotencyKey,billing_object_code billingObjectCode,fee_type feeType,quantity,billing_period billingPeriod,payload,status,failure_reason failureReason,version from bms_charge_source where (#{status} is null or status=#{status}) order by id desc")
    List<ChargeSourceRow> listChargeSources(@Param("status") Integer status);

    @Insert("insert into bms_charge_source(source_no,source_system,source_event_id,idempotency_key,billing_object_code,fee_type,quantity,billing_period,payload,status,failure_reason,version,created_at,updated_at) values(#{sourceNo},#{sourceSystem},#{sourceEventId},#{idempotencyKey},#{billingObjectCode},#{feeType},#{quantity},#{billingPeriod},#{payload},#{status},#{failureReason},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertChargeSource(ChargeSourceRow row);

    @Update("update bms_charge_source set status=#{status},failure_reason=#{failureReason},version=#{version},updated_at=now(3) where source_no=#{sourceNo}")
    void updateChargeSource(ChargeSourceRow row);

    @Select("select charge_no chargeNo,source_no sourceNo,billing_object_code objectCode,fee_type feeType,rule_no ruleNo,quantity,unit_price unitPrice,amount,tax_amount taxAmount,total_amount totalAmount,billing_period billingPeriod,status,version from bms_charge_detail where charge_no=#{chargeNo}")
    ChargeDetailRow findChargeDetail(@Param("chargeNo") String chargeNo);

    @Select("select charge_no chargeNo,source_no sourceNo,billing_object_code objectCode,fee_type feeType,rule_no ruleNo,quantity,unit_price unitPrice,amount,tax_amount taxAmount,total_amount totalAmount,billing_period billingPeriod,status,version from bms_charge_detail where source_no=#{sourceNo} order by id limit 1")
    ChargeDetailRow findChargeBySource(@Param("sourceNo") String sourceNo);

    @Select("select charge_no chargeNo,source_no sourceNo,billing_object_code objectCode,fee_type feeType,rule_no ruleNo,quantity,unit_price unitPrice,amount,tax_amount taxAmount,total_amount totalAmount,billing_period billingPeriod,status,version from bms_charge_detail where billing_object_code=#{objectCode} and billing_period=#{billingPeriod} and (#{status} is null or status=#{status}) order by id")
    List<ChargeDetailRow> listCharges(@Param("objectCode") String objectCode,
                                      @Param("billingPeriod") String billingPeriod,
                                      @Param("status") Integer status);

    @Insert("insert into bms_charge_detail(charge_no,source_no,billing_object_code,fee_type,rule_no,quantity,unit_price,amount,tax_amount,total_amount,billing_period,status,version,created_at,updated_at) values(#{chargeNo},#{sourceNo},#{objectCode},#{feeType},#{ruleNo},#{quantity},#{unitPrice},#{amount},#{taxAmount},#{totalAmount},#{billingPeriod},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertChargeDetail(ChargeDetailRow row);

    @Update("update bms_charge_detail set quantity=#{quantity},unit_price=#{unitPrice},amount=#{amount},tax_amount=#{taxAmount},total_amount=#{totalAmount},status=#{status},version=#{version},updated_at=now(3) where charge_no=#{chargeNo}")
    void updateChargeDetail(ChargeDetailRow row);

    @Update("update bms_charge_detail set status=2,version=version+1,updated_at=now(3) where billing_object_code=#{objectCode} and billing_period=#{billingPeriod} and status=1")
    void markChargesConfirmed(@Param("objectCode") String objectCode, @Param("billingPeriod") String billingPeriod);

    @Insert("insert into bms_adjustment(adjustment_no,original_charge_no,adjust_amount,reason,status,version,created_at,updated_at) values(#{adjustmentNo},#{originalChargeNo},#{adjustAmount},#{reason},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertAdjustment(AdjustmentRow row);

    @Select("select adjustment_no adjustmentNo,original_charge_no originalChargeNo,adjust_amount adjustAmount,reason,status,version from bms_adjustment where adjustment_no=#{adjustmentNo}")
    AdjustmentRow findAdjustment(@Param("adjustmentNo") String adjustmentNo);

    @Update("update bms_adjustment set status=#{status},version=#{version},updated_at=now(3) where adjustment_no=#{adjustmentNo}")
    void updateAdjustment(AdjustmentRow row);

    @Insert("insert into bms_reconciliation(reconciliation_no,billing_object_code object_code,billing_period,total_amount,status,version,created_at,updated_at) values(#{reconciliationNo},#{objectCode},#{billingPeriod},#{totalAmount},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertReconciliation(ReconciliationRow row);

    @Select("select reconciliation_no reconciliationNo,billing_object_code objectCode,billing_period billingPeriod,total_amount totalAmount,status,version from bms_reconciliation where reconciliation_no=#{reconciliationNo}")
    ReconciliationRow findReconciliation(@Param("reconciliationNo") String reconciliationNo);

    @Update("update bms_reconciliation set status=#{status},version=#{version},updated_at=now(3) where reconciliation_no=#{reconciliationNo}")
    void updateReconciliation(ReconciliationRow row);

    @Insert("insert into bms_bill(bill_no,reconciliation_no,billing_object_code,total_amount,status,version,created_at,updated_at) values(#{billNo},#{reconciliationNo},#{objectCode},#{totalAmount},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertBill(BillRow row);

    @Select("select bill_no billNo,reconciliation_no reconciliationNo,billing_object_code objectCode,total_amount totalAmount,status,version from bms_bill where bill_no=#{billNo}")
    BillRow findBill(@Param("billNo") String billNo);

    @Update("update bms_bill set status=#{status},version=#{version},updated_at=now(3) where bill_no=#{billNo}")
    void updateBill(BillRow row);

    @Insert("insert into bms_invoice(invoice_no,bill_no,invoice_amount,status,version,created_at,updated_at) values(#{invoiceNo},#{billNo},#{invoiceAmount},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertInvoice(InvoiceRow row);

    @Select("select invoice_no invoiceNo,bill_no billNo,invoice_amount invoiceAmount,status,version from bms_invoice where invoice_no=#{invoiceNo}")
    InvoiceRow findInvoice(@Param("invoiceNo") String invoiceNo);

    @Update("update bms_invoice set status=#{status},version=#{version},updated_at=now(3) where invoice_no=#{invoiceNo}")
    void updateInvoice(InvoiceRow row);

    @Insert("insert into bms_finance_handover(handover_no,bill_no,status,voucher_no,failure_reason,version,created_at,updated_at) values(#{handoverNo},#{billNo},#{status},#{voucherNo},#{failureReason},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertFinanceHandover(FinanceHandoverRow row);

    @Select("select handover_no handoverNo,bill_no billNo,status,voucher_no voucherNo,failure_reason failureReason,version from bms_finance_handover where handover_no=#{handoverNo}")
    FinanceHandoverRow findFinanceHandover(@Param("handoverNo") String handoverNo);

    @Update("update bms_finance_handover set status=#{status},voucher_no=#{voucherNo},failure_reason=#{failureReason},version=#{version},updated_at=now(3) where handover_no=#{handoverNo}")
    void updateFinanceHandover(FinanceHandoverRow row);

    @Insert("insert into bms_refund_settlement(refund_no,bill_no,refund_amount,status,failure_reason,version,created_at,updated_at) values(#{refundNo},#{billNo},#{refundAmount},#{status},#{failureReason},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertRefundSettlement(RefundSettlementRow row);

    @Select("select refund_no refundNo,bill_no billNo,refund_amount refundAmount,status,failure_reason failureReason,version from bms_refund_settlement where refund_no=#{refundNo}")
    RefundSettlementRow findRefundSettlement(@Param("refundNo") String refundNo);

    @Update("update bms_refund_settlement set status=#{status},failure_reason=#{failureReason},version=#{version},updated_at=now(3) where refund_no=#{refundNo}")
    void updateRefundSettlement(RefundSettlementRow row);

    @Insert("insert into bms_domain_event(event_no,event_type,aggregate_no,business_no,payload,status,created_at) values(#{eventNo},#{eventType},#{aggregateNo},#{businessNo},#{payload},1,now(3))")
    void insertOutboxEvent(OutboxEventRow row);

    @Select("select event_no eventNo,event_type eventType,aggregate_no aggregateNo,business_no businessNo,payload,status from bms_domain_event order by id desc")
    List<OutboxEventRow> listOutboxEvents();

    @Select("select inbox_no inboxNo,source_system sourceSystem,source_event_id sourceEventId,event_type eventType,business_no businessNo,payload,status,failure_reason failureReason from bms_event_consume_log where source_system=#{sourceSystem} and source_event_id=#{sourceEventId}")
    InboxEventRow findInboxEvent(@Param("sourceSystem") String sourceSystem, @Param("sourceEventId") String sourceEventId);

    @Insert("insert into bms_event_consume_log(inbox_no,source_system,source_event_id,event_type,business_no,payload,status,failure_reason,created_at,updated_at) values(#{inboxNo},#{sourceSystem},#{sourceEventId},#{eventType},#{businessNo},#{payload},#{status},#{failureReason},now(3),now(3))")
    void insertInboxEvent(InboxEventRow row);

    @Update("update bms_event_consume_log set status=#{status},failure_reason=#{failureReason},updated_at=now(3) where inbox_no=#{inboxNo}")
    void updateInboxEvent(InboxEventRow row);

    @Insert("insert into bms_operation_audit_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now(3))")
    void insertOperationLog(OperationLogRow row);

    @Select("select coalesce(sum(total_amount),0) billAmount,count(1) billCount from bms_bill where created_at>=#{from} and created_at<#{to}")
    SettlementSummaryRow settlementSummary(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    record BillingObjectRow(Long id, String objectCode, String objectName, String objectType, String direction,
                            String currency, int status, long version) {}

    record BillingRuleRow(Long id, String ruleNo, String objectCode, String feeType, BigDecimal unitPrice,
                          BigDecimal taxRate, LocalDate effectiveFrom, LocalDate effectiveTo, int status,
                          int ruleVersion, long version) {}

    record ChargeSourceRow(Long id, String sourceNo, String sourceSystem, String sourceEventId, String idempotencyKey,
                           String billingObjectCode, String feeType, BigDecimal quantity, String billingPeriod,
                           String payload, int status, String failureReason, long version) {}

    record ChargeDetailRow(Long id, String chargeNo, String sourceNo, String objectCode, String feeType, String ruleNo,
                           BigDecimal quantity, BigDecimal unitPrice, BigDecimal amount, BigDecimal taxAmount,
                           BigDecimal totalAmount, String billingPeriod, int status, long version) {}

    record AdjustmentRow(Long id, String adjustmentNo, String originalChargeNo, BigDecimal adjustAmount,
                         String reason, int status, long version) {}

    record ReconciliationRow(Long id, String reconciliationNo, String objectCode, String billingPeriod,
                             BigDecimal totalAmount, int status, long version) {}

    record BillRow(Long id, String billNo, String reconciliationNo, String objectCode, BigDecimal totalAmount,
                   int status, long version) {}

    record InvoiceRow(Long id, String invoiceNo, String billNo, BigDecimal invoiceAmount, int status, long version) {}

    record FinanceHandoverRow(Long id, String handoverNo, String billNo, int status, String voucherNo,
                              String failureReason, long version) {}

    record RefundSettlementRow(Long id, String refundNo, String billNo, BigDecimal refundAmount, int status,
                               String failureReason, long version) {}

    record OutboxEventRow(String eventNo, String eventType, String aggregateNo, String businessNo, String payload,
                          int status) {}

    record InboxEventRow(String inboxNo, String sourceSystem, String sourceEventId, String eventType, String businessNo,
                         String payload, int status, String failureReason) {}

    record OperationLogRow(String operationType, String businessNo, Long operatorId, String idempotencyKey,
                           LocalDateTime createdAt) {}

    record SettlementSummaryRow(BigDecimal billAmount, long billCount) {}
}
