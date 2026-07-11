package com.chaobo.scm.supplier.infrastructure.persistence.finance;

import org.apache.ibatis.annotations.*;import java.math.BigDecimal;
@Mapper public interface SupplierFinanceLifecycleMapper {
    @Update("UPDATE sup_reconciliation SET confirmed_amount=#{amount},difference_reason=#{reason},status=2,version=version+1 WHERE reconciliation_id=#{id} AND version=#{version} AND status=3 AND deleted=0") int resolveDifference(@Param("id")long id,@Param("version")int version,@Param("amount")BigDecimal amount,@Param("reason")String reason);
    @Update("UPDATE sup_reconciliation SET status=5,version=version+1 WHERE reconciliation_id=#{id} AND version=#{version} AND status IN (2,3) AND deleted=0") int close(@Param("id")long id,@Param("version")int version);
    @Update("UPDATE sup_reconciliation SET status=5,version=version+1 WHERE statement_no=#{statementNo} AND supplier_id=#{supplierId} AND source_version&lt;=#{sourceVersion} AND status IN (2,3) AND deleted=0") int closeFromBms(@Param("statementNo")String statementNo,@Param("supplierId")long supplierId,@Param("sourceVersion")int sourceVersion);
    @Update("UPDATE sup_invoice_collaboration SET amount_excluding_tax=#{net},tax_amount=#{tax},tax_rate=#{rate},attachment_url=#{url},status=1,validation_message=NULL,version=version+1 WHERE invoice_id=#{id} AND version=#{version} AND status=3 AND deleted=0") int resubmitInvoice(@Param("id")long id,@Param("version")int version,@Param("net")BigDecimal net,@Param("tax")BigDecimal tax,@Param("rate")BigDecimal rate,@Param("url")String url);
    @Update("UPDATE sup_invoice_collaboration SET status=4,version=version+1 WHERE invoice_id=#{id} AND version=#{version} AND status IN (2,3) AND deleted=0") int closeInvoice(@Param("id")long id,@Param("version")int version);
}
