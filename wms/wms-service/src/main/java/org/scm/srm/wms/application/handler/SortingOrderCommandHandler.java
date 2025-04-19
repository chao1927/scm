package org.scm.srm.wms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.srm.wms.application.command.CompleteSortingCommand;
import org.scm.srm.wms.application.command.StartSortingCommand;
import org.scm.srm.wms.application.event.SortingCompletedEvent;
import org.scm.srm.wms.application.event.SortingStartedEvent;
import org.scm.srm.wms.domain.repository.SortingOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SortingOrderCommandHandler {

    @Autowired
    private SortingOrderRepository repository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(StartSortingCommand command) {
        var agg = repository.findById(command.id());
        agg.startSorting(command);
        repository.save(agg);
        eventPublisher.publish(new SortingStartedEvent(command.id()));
    }

    public void handle(CompleteSortingCommand command) {
        var agg = repository.findById(command.id());
        agg.completeSorting(command);
        repository.save(agg);
        eventPublisher.publish(new SortingCompletedEvent(command.id()));
    }
}
