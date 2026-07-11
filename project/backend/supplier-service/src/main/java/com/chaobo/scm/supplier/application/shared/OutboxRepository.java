package com.chaobo.scm.supplier.application.shared;

import com.chaobo.scm.supplier.domain.shared.DomainEvent;
import java.util.List;

public interface OutboxRepository {
    void saveAll(List<DomainEvent> events);
}
