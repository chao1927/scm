package org.scm.cis.adapter.infra.jpa;

import org.scm.cis.adapter.infra.domain.CentralInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CentralInventoryRepository extends JpaRepository<CentralInventory, Long> {
    CentralInventory findBySku(String sku);
}
