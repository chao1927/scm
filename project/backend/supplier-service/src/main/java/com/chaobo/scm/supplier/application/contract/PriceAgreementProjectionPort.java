package com.chaobo.scm.supplier.application.contract;import com.chaobo.scm.supplier.domain.contract.*;import com.chaobo.scm.supplier.domain.quote.*;
public interface PriceAgreementProjectionPort{void activate(SupplierContractAggregate contract,SupplierQuoteAggregate quote);void renew(SupplierContractAggregate contract);void terminate(SupplierContractAggregate contract);}
