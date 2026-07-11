package com.chaobo.scm.supplier.domain.quote;import java.util.*;
public interface SupplierQuoteRepository { Optional<SupplierQuoteAggregate> findById(long id); java.util.List<Long> expiredIds(); void save(SupplierQuoteAggregate aggregate,long operatorId); }
