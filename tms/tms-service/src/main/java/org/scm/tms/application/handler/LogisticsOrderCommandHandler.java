package org.scm.tms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.tms._share.enums.LogisticsStatus;
import org.scm.tms.application.command.AssignLogisticsCommand;
import org.scm.tms.application.command.UpdateTrackingStatusCommand;
import org.scm.tms.application.event.LogisticsAssignedEvent;
import org.scm.tms.application.event.LogisticsStatusUpdatedEvent;
import org.scm.tms.domain.model.LogisticsOrderAgg;
import org.scm.tms.domain.repository.LogisticsOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LogisticsOrderCommandHandler {

    @Autowired
    private LogisticsOrderRepository repository;

    @Autowired
    private EventPublisher publisher;

    public void handle(AssignLogisticsCommand command) {
        LogisticsOrderAgg agg = LogisticsOrderAgg.create(command);
        repository.save(agg);
        publisher.publish(new LogisticsAssignedEvent(command.logisticsNo()));
    }

    public void handle(UpdateTrackingStatusCommand command) {
        LogisticsOrderAgg agg = repository.findByLogisticsNo(command.logisticsNo());
        agg.updateTrackingStatus(LogisticsStatus.of(command.logisticStatus()));
        repository.save(agg);
        publisher.publish(new LogisticsStatusUpdatedEvent(command.logisticsNo(), command.logisticStatus()));
    }
}
