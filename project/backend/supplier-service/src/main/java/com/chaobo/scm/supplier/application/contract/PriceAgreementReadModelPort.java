package com.chaobo.scm.supplier.application.contract;

import com.chaobo.scm.common.api.PageResult;
import java.time.LocalDate;
import java.util.Optional;

public interface PriceAgreementReadModelPort {Optional<PriceAgreementView> detail(long agreementId);PageResult<PriceAgreementView> page(Long supplierId,String skuCode,int pageNo,int pageSize);Optional<PriceAgreementView.Line> activeLine(long supplierId,String skuCode,String currency,LocalDate businessDate);}
