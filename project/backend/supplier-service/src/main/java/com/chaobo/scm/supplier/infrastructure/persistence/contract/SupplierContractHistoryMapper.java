package com.chaobo.scm.supplier.infrastructure.persistence.contract;
import org.apache.ibatis.annotations.*;import java.time.LocalDate;
@Mapper public interface SupplierContractHistoryMapper {@Insert("INSERT IGNORE INTO sup_supplier_contract_version_history(contract_id,contract_version,effective_to,terms_json,attachment_url,contract_status,changed_by) VALUES(#{r.id},#{r.version},#{r.to},CAST(#{r.terms} AS JSON),#{r.attachment},#{r.status},#{operator})")void snapshot(@Param("r")SupplierContractMapper.Row row,@Param("operator")long operator);}
