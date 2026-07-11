package com.chaobo.scm.supplier.domain.quote;
import com.chaobo.scm.common.error.*;import java.math.BigDecimal;
public record QuoteLine(long lineId,String skuCode,BigDecimal quoteQty,BigDecimal unitPrice,BigDecimal taxRate,int deliveryDays,BigDecimal moq){public QuoteLine{if(skuCode==null||skuCode.isBlank()||quoteQty==null||quoteQty.signum()<=0||unitPrice==null||unitPrice.signum()<=0||taxRate==null||taxRate.signum()<0||taxRate.compareTo(new BigDecimal("100"))>0||deliveryDays<0||moq==null||moq.signum()<=0)throw new BusinessException(ErrorCode.VALIDATION_FAILED,"报价行价格、数量、税率或交期不合法");}}
