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

/**
 * BmsMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface BmsMapper {

    /**
     * 查询并返回 {@code findBillingObject}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param objectCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code BillingObjectRow}
     */
    @Select("select object_code objectCode,object_name objectName,object_type objectType,direction,currency,status,version from bms_billing_object where object_code=#{objectCode}")
    BillingObjectRow findBillingObject(@Param("objectCode") String objectCode);

    /**
     * 查询并返回 {@code listBillingObjects}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code List<BillingObjectRow>}
     */
    @Select("select object_code objectCode,object_name objectName,object_type objectType,direction,currency,status,version from bms_billing_object where (#{status} is null or status=#{status}) order by id desc")
    List<BillingObjectRow> listBillingObjects(@Param("status") Integer status);

    /**
     * 处理当前类型职责中的操作 {@code insertBillingObject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code BillingObjectRow}
     */
    @Insert("insert into bms_billing_object(object_code,object_name,object_type,direction,currency,status,version,created_at,updated_at) values(#{objectCode},#{objectName},#{objectType},#{direction},#{currency},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertBillingObject(BillingObjectRow row);

    /**
     * 执行命令 {@code updateBillingObject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code BillingObjectRow}
     */
    @Update("update bms_billing_object set status=#{status},version=#{version},updated_at=now(3) where object_code=#{objectCode}")
    void updateBillingObject(BillingObjectRow row);

    /**
     * 查询并返回 {@code findBillingRule}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param ruleNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code BillingRuleRow}
     */
    @Select("select rule_no ruleNo,object_code objectCode,fee_type feeType,unit_price unitPrice,tax_rate taxRate,effective_from effectiveFrom,effective_to effectiveTo,status,rule_version ruleVersion,version from bms_billing_rule where rule_no=#{ruleNo}")
    BillingRuleRow findBillingRule(@Param("ruleNo") String ruleNo);

    /**
     * 查询并返回 {@code findPublishedRule}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param objectCode 可追踪业务编码，类型为 {@code String}
     * @param feeType 金额或计费值，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code BillingRuleRow}
     */
    @Select("select rule_no ruleNo,object_code objectCode,fee_type feeType,unit_price unitPrice,tax_rate taxRate,effective_from effectiveFrom,effective_to effectiveTo,status,rule_version ruleVersion,version from bms_billing_rule where object_code=#{objectCode} and fee_type=#{feeType} and status=2 order by rule_version desc,id desc limit 1")
    BillingRuleRow findPublishedRule(@Param("objectCode") String objectCode, @Param("feeType") String feeType);

    /**
     * 查询并返回 {@code countPublishedRuleOverlap}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param objectCode 可追踪业务编码，类型为 {@code String}
     * @param feeType 金额或计费值，类型为 {@code String}
     * @param effectiveFrom 业务处理参数或成员，类型为 {@code LocalDate}
     * @param effectiveTo 业务处理参数或成员，类型为 {@code LocalDate}
     * @return 查询并返回的结果，类型为 {@code int}
     */
    @Select("select count(1) from bms_billing_rule where object_code=#{objectCode} and fee_type=#{feeType} and status=2 and effective_from<=#{effectiveTo} and effective_to>=#{effectiveFrom}")
    int countPublishedRuleOverlap(@Param("objectCode") String objectCode, @Param("feeType") String feeType, @Param("effectiveFrom") LocalDate effectiveFrom, @Param("effectiveTo") LocalDate effectiveTo);

    /**
     * 查询并返回 {@code listBillingRules}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param objectCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code List<BillingRuleRow>}
     */
    @Select("select rule_no ruleNo,object_code objectCode,fee_type feeType,unit_price unitPrice,tax_rate taxRate,effective_from effectiveFrom,effective_to effectiveTo,status,rule_version ruleVersion,version from bms_billing_rule where object_code=#{objectCode} order by id desc")
    List<BillingRuleRow> listBillingRules(@Param("objectCode") String objectCode);

    /**
     * 处理当前类型职责中的操作 {@code insertBillingRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code BillingRuleRow}
     */
    @Insert("insert into bms_billing_rule(rule_no,object_code,fee_type,unit_price,tax_rate,effective_from,effective_to,status,rule_version,version,created_at,updated_at) values(#{ruleNo},#{objectCode},#{feeType},#{unitPrice},#{taxRate},#{effectiveFrom},#{effectiveTo},#{status},#{ruleVersion},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertBillingRule(BillingRuleRow row);

    /**
     * 执行命令 {@code updateBillingRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code BillingRuleRow}
     */
    @Update("update bms_billing_rule set status=#{status},rule_version=#{ruleVersion},version=#{version},updated_at=now(3) where rule_no=#{ruleNo}")
    void updateBillingRule(BillingRuleRow row);

    /**
     * 查询并返回 {@code findChargeSource}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ChargeSourceRow}
     */
    @Select("select source_no sourceNo,source_system sourceSystem,source_event_id sourceEventId,idempotency_key idempotencyKey,billing_object_code billingObjectCode,fee_type feeType,quantity,billing_period billingPeriod,payload,status,failure_reason failureReason,version from bms_charge_source where source_no=#{sourceNo}")
    ChargeSourceRow findChargeSource(@Param("sourceNo") String sourceNo);

    /**
     * 查询并返回 {@code findChargeSourceByIdempotency}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ChargeSourceRow}
     */
    @Select("select source_no sourceNo,source_system sourceSystem,source_event_id sourceEventId,idempotency_key idempotencyKey,billing_object_code billingObjectCode,fee_type feeType,quantity,billing_period billingPeriod,payload,status,failure_reason failureReason,version from bms_charge_source where source_system=#{sourceSystem} and idempotency_key=#{idempotencyKey} order by id limit 1")
    ChargeSourceRow findChargeSourceByIdempotency(@Param("sourceSystem") String sourceSystem, @Param("idempotencyKey") String idempotencyKey);

    /**
     * 查询并返回 {@code listChargeSources}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code List<ChargeSourceRow>}
     */
    @Select("select source_no sourceNo,source_system sourceSystem,source_event_id sourceEventId,idempotency_key idempotencyKey,billing_object_code billingObjectCode,fee_type feeType,quantity,billing_period billingPeriod,payload,status,failure_reason failureReason,version from bms_charge_source where (#{status} is null or status=#{status}) order by id desc")
    List<ChargeSourceRow> listChargeSources(@Param("status") Integer status);

    /**
     * 处理当前类型职责中的操作 {@code insertChargeSource}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ChargeSourceRow}
     */
    @Insert("insert into bms_charge_source(source_no,source_system,source_event_id,idempotency_key,billing_object_code,fee_type,quantity,billing_period,payload,status,failure_reason,version,created_at,updated_at) values(#{sourceNo},#{sourceSystem},#{sourceEventId},#{idempotencyKey},#{billingObjectCode},#{feeType},#{quantity},#{billingPeriod},#{payload},#{status},#{failureReason},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertChargeSource(ChargeSourceRow row);

    /**
     * 执行命令 {@code updateChargeSource}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ChargeSourceRow}
     */
    @Update("update bms_charge_source set status=#{status},failure_reason=#{failureReason},version=#{version},updated_at=now(3) where source_no=#{sourceNo}")
    void updateChargeSource(ChargeSourceRow row);

    /**
     * 查询并返回 {@code findChargeDetail}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param chargeNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ChargeDetailRow}
     */
    @Select("select charge_no chargeNo,source_no sourceNo,billing_object_code objectCode,fee_type feeType,rule_no ruleNo,quantity,unit_price unitPrice,amount,tax_amount taxAmount,total_amount totalAmount,billing_period billingPeriod,status,version from bms_charge_detail where charge_no=#{chargeNo}")
    ChargeDetailRow findChargeDetail(@Param("chargeNo") String chargeNo);

    /**
     * 查询并返回 {@code findChargeBySource}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ChargeDetailRow}
     */
    @Select("select charge_no chargeNo,source_no sourceNo,billing_object_code objectCode,fee_type feeType,rule_no ruleNo,quantity,unit_price unitPrice,amount,tax_amount taxAmount,total_amount totalAmount,billing_period billingPeriod,status,version from bms_charge_detail where source_no=#{sourceNo} order by id limit 1")
    ChargeDetailRow findChargeBySource(@Param("sourceNo") String sourceNo);

    /**
     * 查询并返回 {@code listCharges}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param objectCode 可追踪业务编码，类型为 {@code String}
     * @param billingPeriod 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code List<ChargeDetailRow>}
     */
    @Select("select charge_no chargeNo,source_no sourceNo,billing_object_code objectCode,fee_type feeType,rule_no ruleNo,quantity,unit_price unitPrice,amount,tax_amount taxAmount,total_amount totalAmount,billing_period billingPeriod,status,version from bms_charge_detail where billing_object_code=#{objectCode} and billing_period=#{billingPeriod} and (#{status} is null or status=#{status}) order by id")
    List<ChargeDetailRow> listCharges(@Param("objectCode") String objectCode, @Param("billingPeriod") String billingPeriod, @Param("status") Integer status);

    /**
     * 处理当前类型职责中的操作 {@code insertChargeDetail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ChargeDetailRow}
     */
    @Insert("insert into bms_charge_detail(charge_no,source_no,billing_object_code,fee_type,rule_no,quantity,unit_price,amount,tax_amount,total_amount,billing_period,status,version,created_at,updated_at) values(#{chargeNo},#{sourceNo},#{objectCode},#{feeType},#{ruleNo},#{quantity},#{unitPrice},#{amount},#{taxAmount},#{totalAmount},#{billingPeriod},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertChargeDetail(ChargeDetailRow row);

    /**
     * 执行命令 {@code updateChargeDetail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ChargeDetailRow}
     */
    @Update("update bms_charge_detail set quantity=#{quantity},unit_price=#{unitPrice},amount=#{amount},tax_amount=#{taxAmount},total_amount=#{totalAmount},status=#{status},version=#{version},updated_at=now(3) where charge_no=#{chargeNo}")
    void updateChargeDetail(ChargeDetailRow row);

    /**
     * 处理当前类型职责中的操作 {@code markChargesConfirmed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param objectCode 可追踪业务编码，类型为 {@code String}
     * @param billingPeriod 业务处理参数或成员，类型为 {@code String}
     */
    @Update("update bms_charge_detail set status=2,version=version+1,updated_at=now(3) where billing_object_code=#{objectCode} and billing_period=#{billingPeriod} and status=1")
    void markChargesConfirmed(@Param("objectCode") String objectCode, @Param("billingPeriod") String billingPeriod);

    /**
     * 处理当前类型职责中的操作 {@code insertAdjustment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code AdjustmentRow}
     */
    @Insert("insert into bms_adjustment(adjustment_no,original_charge_no,adjust_amount,reason,status,version,created_at,updated_at) values(#{adjustmentNo},#{originalChargeNo},#{adjustAmount},#{reason},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertAdjustment(AdjustmentRow row);

    /**
     * 查询并返回 {@code findAdjustment}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param adjustmentNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code AdjustmentRow}
     */
    @Select("select adjustment_no adjustmentNo,original_charge_no originalChargeNo,adjust_amount adjustAmount,reason,status,version from bms_adjustment where adjustment_no=#{adjustmentNo}")
    AdjustmentRow findAdjustment(@Param("adjustmentNo") String adjustmentNo);

    /**
     * 执行命令 {@code updateAdjustment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code AdjustmentRow}
     */
    @Update("update bms_adjustment set status=#{status},version=#{version},updated_at=now(3) where adjustment_no=#{adjustmentNo}")
    void updateAdjustment(AdjustmentRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertReconciliation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ReconciliationRow}
     */
    @Insert("insert into bms_reconciliation(reconciliation_no,billing_object_code object_code,billing_period,total_amount,status,version,created_at,updated_at) values(#{reconciliationNo},#{objectCode},#{billingPeriod},#{totalAmount},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertReconciliation(ReconciliationRow row);

    /**
     * 查询并返回 {@code findReconciliation}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param reconciliationNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ReconciliationRow}
     */
    @Select("select reconciliation_no reconciliationNo,billing_object_code objectCode,billing_period billingPeriod,total_amount totalAmount,status,version from bms_reconciliation where reconciliation_no=#{reconciliationNo}")
    ReconciliationRow findReconciliation(@Param("reconciliationNo") String reconciliationNo);

    /**
     * 执行命令 {@code updateReconciliation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ReconciliationRow}
     */
    @Update("update bms_reconciliation set status=#{status},version=#{version},updated_at=now(3) where reconciliation_no=#{reconciliationNo}")
    void updateReconciliation(ReconciliationRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertBill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code BillRow}
     */
    @Insert("insert into bms_bill(bill_no,reconciliation_no,billing_object_code,total_amount,status,version,created_at,updated_at) values(#{billNo},#{reconciliationNo},#{objectCode},#{totalAmount},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertBill(BillRow row);

    /**
     * 查询并返回 {@code findBill}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param billNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code BillRow}
     */
    @Select("select bill_no billNo,reconciliation_no reconciliationNo,billing_object_code objectCode,total_amount totalAmount,status,version from bms_bill where bill_no=#{billNo}")
    BillRow findBill(@Param("billNo") String billNo);

    /**
     * 执行命令 {@code updateBill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code BillRow}
     */
    @Update("update bms_bill set status=#{status},version=#{version},updated_at=now(3) where bill_no=#{billNo}")
    void updateBill(BillRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertInvoice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code InvoiceRow}
     */
    @Insert("insert into bms_invoice(invoice_no,bill_no,invoice_amount,status,version,created_at,updated_at) values(#{invoiceNo},#{billNo},#{invoiceAmount},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertInvoice(InvoiceRow row);

    /**
     * 查询并返回 {@code findInvoice}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param invoiceNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code InvoiceRow}
     */
    @Select("select invoice_no invoiceNo,bill_no billNo,invoice_amount invoiceAmount,status,version from bms_invoice where invoice_no=#{invoiceNo}")
    InvoiceRow findInvoice(@Param("invoiceNo") String invoiceNo);

    /**
     * 执行命令 {@code updateInvoice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code InvoiceRow}
     */
    @Update("update bms_invoice set status=#{status},version=#{version},updated_at=now(3) where invoice_no=#{invoiceNo}")
    void updateInvoice(InvoiceRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertFinanceHandover}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code FinanceHandoverRow}
     */
    @Insert("insert into bms_finance_handover(handover_no,bill_no,status,voucher_no,failure_reason,version,created_at,updated_at) values(#{handoverNo},#{billNo},#{status},#{voucherNo},#{failureReason},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertFinanceHandover(FinanceHandoverRow row);

    /**
     * 查询并返回 {@code findFinanceHandover}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param handoverNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code FinanceHandoverRow}
     */
    @Select("select handover_no handoverNo,bill_no billNo,status,voucher_no voucherNo,failure_reason failureReason,version from bms_finance_handover where handover_no=#{handoverNo}")
    FinanceHandoverRow findFinanceHandover(@Param("handoverNo") String handoverNo);

    /**
     * 执行命令 {@code updateFinanceHandover}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code FinanceHandoverRow}
     */
    @Update("update bms_finance_handover set status=#{status},voucher_no=#{voucherNo},failure_reason=#{failureReason},version=#{version},updated_at=now(3) where handover_no=#{handoverNo}")
    void updateFinanceHandover(FinanceHandoverRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertRefundSettlement}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code RefundSettlementRow}
     */
    @Insert("insert into bms_refund_settlement(refund_no,bill_no,refund_amount,status,failure_reason,version,created_at,updated_at) values(#{refundNo},#{billNo},#{refundAmount},#{status},#{failureReason},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertRefundSettlement(RefundSettlementRow row);

    /**
     * 查询并返回 {@code findRefundSettlement}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param refundNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code RefundSettlementRow}
     */
    @Select("select refund_no refundNo,bill_no billNo,refund_amount refundAmount,status,failure_reason failureReason,version from bms_refund_settlement where refund_no=#{refundNo}")
    RefundSettlementRow findRefundSettlement(@Param("refundNo") String refundNo);

    /**
     * 处理当前类型职责中的操作 {@code lockBill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param billNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BillRow}
     */
    @Select("select bill_no billNo,reconciliation_no reconciliationNo,object_code objectCode,total_amount totalAmount,status,version from bms_bill where bill_no=#{billNo} for update")
    BillRow lockBill(@Param("billNo") String billNo);

    /**
     * 处理当前类型职责中的操作 {@code occupiedRefundAmount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param billNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    @Select("select coalesce(sum(refund_amount),0) from bms_refund_settlement where bill_no=#{billNo} and status in (1,2)")
    BigDecimal occupiedRefundAmount(@Param("billNo") String billNo);

    /**
     * 执行命令 {@code updateRefundSettlement}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code RefundSettlementRow}
     */
    @Update("update bms_refund_settlement set status=#{status},failure_reason=#{failureReason},version=#{version},updated_at=now(3) where refund_no=#{refundNo}")
    void updateRefundSettlement(RefundSettlementRow row);

    /**
     * 处理当前类型职责中的操作 {@code claimRefundReceipt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param receiptNo 可追踪业务编码，类型为 {@code String}
     * @param refundNo 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Insert("insert ignore into bms_refund_receipt(receipt_no,refund_no,receipt_status,payload,created_at) values(#{receiptNo},#{refundNo},#{status},#{payload},now(3))")
    int claimRefundReceipt(@Param("receiptNo") String receiptNo, @Param("refundNo") String refundNo, @Param("status") String status, @Param("payload") String payload);

    /**
     * 处理当前类型职责中的操作 {@code hasRefundReceipt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param receiptNo 可追踪业务编码，类型为 {@code String}
     * @param refundNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Select("select count(1) from bms_refund_receipt where receipt_no=#{receiptNo} and refund_no=#{refundNo}")
    int hasRefundReceipt(@Param("receiptNo") String receiptNo, @Param("refundNo") String refundNo);

    /**
     * 处理当前类型职责中的操作 {@code insertOutboxEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code OutboxEventRow}
     */
    @Insert("insert into bms_domain_event(event_no,event_type,aggregate_no,business_no,payload,status,created_at) values(#{eventNo},#{eventType},#{aggregateNo},#{businessNo},#{payload},1,now(3))")
    void insertOutboxEvent(OutboxEventRow row);

    /**
     * 查询并返回 {@code listOutboxEvents}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<OutboxEventRow>}
     */
    @Select("select event_no eventNo,event_type eventType,aggregate_no aggregateNo,business_no businessNo,payload,status from bms_domain_event order by id desc")
    List<OutboxEventRow> listOutboxEvents();

    /**
     * 查询并返回 {@code findInboxEvent}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param sourceEventId 业务或技术标识，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code InboxEventRow}
     */
    @Select("select inbox_no inboxNo,source_system sourceSystem,source_event_id sourceEventId,event_type eventType,business_no businessNo,payload,status,failure_reason failureReason from bms_event_consume_log where source_system=#{sourceSystem} and source_event_id=#{sourceEventId}")
    InboxEventRow findInboxEvent(@Param("sourceSystem") String sourceSystem, @Param("sourceEventId") String sourceEventId);

    /**
     * 处理当前类型职责中的操作 {@code insertInboxEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code InboxEventRow}
     */
    @Insert("insert into bms_event_consume_log(inbox_no,source_system,source_event_id,event_type,business_no,payload,status,failure_reason,created_at,updated_at) values(#{inboxNo},#{sourceSystem},#{sourceEventId},#{eventType},#{businessNo},#{payload},#{status},#{failureReason},now(3),now(3))")
    void insertInboxEvent(InboxEventRow row);

    /**
     * 执行命令 {@code updateInboxEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code InboxEventRow}
     */
    @Update("update bms_event_consume_log set status=#{status},failure_reason=#{failureReason},updated_at=now(3) where inbox_no=#{inboxNo}")
    void updateInboxEvent(InboxEventRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertOperationLog}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code OperationLogRow}
     */
    @Insert("insert into bms_operation_audit_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now(3))")
    void insertOperationLog(OperationLogRow row);

    /**
     * 处理当前类型职责中的操作 {@code settlementSummary}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param from 业务处理参数或成员，类型为 {@code LocalDateTime}
     * @param to 业务处理参数或成员，类型为 {@code LocalDateTime}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SettlementSummaryRow}
     */
    @Select("select coalesce(sum(total_amount),0) billAmount,count(1) billCount from bms_bill where created_at>=#{from} and created_at<#{to}")
    SettlementSummaryRow settlementSummary(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * BillingObjectRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record BillingObjectRow(Long id, String objectCode, String objectName, String objectType, String direction, String currency, int status, long version) {
    }

    /**
     * BillingRuleRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record BillingRuleRow(Long id, String ruleNo, String objectCode, String feeType, BigDecimal unitPrice, BigDecimal taxRate, LocalDate effectiveFrom, LocalDate effectiveTo, int status, int ruleVersion, long version) {
    }

    /**
     * ChargeSourceRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ChargeSourceRow(Long id, String sourceNo, String sourceSystem, String sourceEventId, String idempotencyKey, String billingObjectCode, String feeType, BigDecimal quantity, String billingPeriod, String payload, int status, String failureReason, long version) {
    }

    /**
     * ChargeDetailRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ChargeDetailRow(Long id, String chargeNo, String sourceNo, String objectCode, String feeType, String ruleNo, BigDecimal quantity, BigDecimal unitPrice, BigDecimal amount, BigDecimal taxAmount, BigDecimal totalAmount, String billingPeriod, int status, long version) {
    }

    /**
     * AdjustmentRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record AdjustmentRow(Long id, String adjustmentNo, String originalChargeNo, BigDecimal adjustAmount, String reason, int status, long version) {
    }

    /**
     * ReconciliationRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ReconciliationRow(Long id, String reconciliationNo, String objectCode, String billingPeriod, BigDecimal totalAmount, int status, long version) {
    }

    /**
     * BillRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record BillRow(Long id, String billNo, String reconciliationNo, String objectCode, BigDecimal totalAmount, int status, long version) {
    }

    /**
     * InvoiceRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record InvoiceRow(Long id, String invoiceNo, String billNo, BigDecimal invoiceAmount, int status, long version) {
    }

    /**
     * FinanceHandoverRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record FinanceHandoverRow(Long id, String handoverNo, String billNo, int status, String voucherNo, String failureReason, long version) {
    }

    /**
     * RefundSettlementRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record RefundSettlementRow(Long id, String refundNo, String billNo, BigDecimal refundAmount, int status, String failureReason, long version) {
    }

    /**
     * OutboxEventRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record OutboxEventRow(String eventNo, String eventType, String aggregateNo, String businessNo, String payload, int status) {
    }

    /**
     * InboxEventRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record InboxEventRow(String inboxNo, String sourceSystem, String sourceEventId, String eventType, String businessNo, String payload, int status, String failureReason) {
    }

    /**
     * OperationLogRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record OperationLogRow(String operationType, String businessNo, Long operatorId, String idempotencyKey, LocalDateTime createdAt) {
    }

    /**
     * SettlementSummaryRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record SettlementSummaryRow(BigDecimal billAmount, long billCount) {
    }
}
