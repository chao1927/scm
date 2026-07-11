package com.chaobo.scm.supplier.infrastructure.persistence.contract;

import org.apache.ibatis.annotations.*;import java.util.List;
@Mapper public interface SupplierContractExpiryMapper {@Select("SELECT contract_id FROM sup_supplier_contract WHERE contract_status=3 AND effective_to<CURDATE() AND deleted=0")List<Long> expiredIds();}
