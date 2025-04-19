package org.scm.srm.wms.adapter.infra.repository;

import org.scm.srm.wms.domain.model.SalesOutboundOrderAgg;
import org.scm.srm.wms.domain.repository.SalesOutboundOrderRepository;

public class SalesOutboundOrderRepositoryImpl implements SalesOutboundOrderRepository {
    @Override
    public void save(SalesOutboundOrderAgg salesOutboundOrderAgg) {

    }

    @Override
    public SalesOutboundOrderAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public SalesOutboundOrderAgg findByOutboundNo(String outboundNo) {
        return null;
    }
}
