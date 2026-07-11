package com.chaobo.scm.supplier.domain.returning;

import com.chaobo.scm.common.error.*;
import java.math.BigDecimal;

public final class SupplierReturnLine {
    private final long id; private final String skuCode,batchNo,inventoryStatus; private final BigDecimal requestedQty;
    private BigDecimal lockedQty,outboundQty,signedQty;
    public SupplierReturnLine(long id,String skuCode,String batchNo,String inventoryStatus,BigDecimal requestedQty,BigDecimal lockedQty,BigDecimal outboundQty,BigDecimal signedQty){this.id=id;this.skuCode=skuCode;this.batchNo=batchNo;this.inventoryStatus=inventoryStatus;this.requestedQty=requestedQty;this.lockedQty=nvl(lockedQty);this.outboundQty=nvl(outboundQty);this.signedQty=nvl(signedQty);validate();}
    public void lock(BigDecimal qty){qty=positive(qty,"锁定数量");if(qty.compareTo(requestedQty)>0)throw rule("锁定数量不能超过申请数量");lockedQty=qty;}
    public void outbound(BigDecimal qty){qty=positive(qty,"出库数量");if(qty.compareTo(lockedQty)>0)throw rule("出库数量不能超过锁定数量");outboundQty=qty;}
    public void sign(BigDecimal qty){qty=nonNegative(qty,"签收数量");if(qty.compareTo(outboundQty)>0)throw rule("签收数量不能超过出库数量");signedQty=qty;}
    private void validate(){if(id<=0||skuCode==null||skuCode.isBlank()||inventoryStatus==null||inventoryStatus.isBlank())throw rule("退供明细信息不完整");positive(requestedQty,"申请数量");if(lockedQty.compareTo(requestedQty)>0||outboundQty.compareTo(lockedQty)>0||signedQty.compareTo(outboundQty)>0)throw rule("退供数量链不合法");}
    private static BigDecimal nvl(BigDecimal v){return v==null?BigDecimal.ZERO:v;} private static BigDecimal positive(BigDecimal v,String n){if(v==null||v.signum()<=0)throw rule(n+"必须大于0");return v;} private static BigDecimal nonNegative(BigDecimal v,String n){if(v==null||v.signum()<0)throw rule(n+"不能小于0");return v;} private static BusinessException rule(String m){return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED,m);}
    public long id(){return id;} public String skuCode(){return skuCode;} public String batchNo(){return batchNo;} public String inventoryStatus(){return inventoryStatus;} public BigDecimal requestedQty(){return requestedQty;} public BigDecimal lockedQty(){return lockedQty;} public BigDecimal outboundQty(){return outboundQty;} public BigDecimal signedQty(){return signedQty;}
}
