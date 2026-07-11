package com.chaobo.scm.purchase.application.shared;

import com.chaobo.scm.purchase.domain.shared.DomainEvent;

import java.util.List;

public interface OutboxRepository {

    void saveAll(List<DomainEvent> events);
}
