package org.scm.srm.wms.adapter.infra.jpa;

import org.scm.srm.wms.adapter.infra.domain.SortingOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SortingOrderItemJpaRepository extends JpaRepository<SortingOrderItem, Long> {
    List<SortingOrderItem> findBySortingNo(String sortingNo);
}
