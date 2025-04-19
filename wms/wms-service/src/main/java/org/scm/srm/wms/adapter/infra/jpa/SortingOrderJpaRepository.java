package org.scm.srm.wms.adapter.infra.jpa;

import org.scm.srm.wms.adapter.infra.domain.SortingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SortingOrderJpaRepository extends JpaRepository<SortingOrder, Long> {
    SortingOrder findBySortingNo(String sortingNo);
}
