package org.scm.bdp.service.application.handler;

import org.scm.bdp.service.application.command.*;
import org.scm.bdp.service.application.event.*;
import org.scm.bdp.service.domain.model.LogisticsChannelAgg;
import org.scm.bdp.service.domain.repository.LogisticsChannelRepository;
import org.scm.common.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LogisticsChannelCommandHandler {

    @Autowired
    private LogisticsChannelRepository repository;

    @Autowired
    private EventPublisher publisher;

    public void handle(CreateLogisticsChannelCommand command) {
        LogisticsChannelAgg agg = LogisticsChannelAgg.create(command);
        repository.save(agg);
        publisher.publish(new LogisticsChannelCreatedEvent(agg.id(), agg.name()));
    }

    public void handle(UpdateLogisticsChannelCommand command) {
        LogisticsChannelAgg agg = repository.findById(command.id());
        agg.update(command);
        repository.save(agg);
        publisher.publish(new LogisticsChannelUpdatedEvent(agg.id(), agg.name()));
    }

    public void handle(DisableLogisticsChannelCommand command) {
        LogisticsChannelAgg agg = repository.findById(command.id());
        agg.disable();
        repository.save(agg);
        publisher.publish(new LogisticsChannelDisabledEvent(command.id()));
    }

    public void handle(EnableLogisticsChannelCommand command) {
        LogisticsChannelAgg agg = repository.findById(command.id());
        agg.enable();
        repository.save(agg);
        publisher.publish(new LogisticsChannelEnabledEvent(command.id()));
    }

    public void handle(DeleteLogisticsChannelCommand command) {
        LogisticsChannelAgg agg = repository.findById(command.id());
        agg.delete();
        repository.save(agg);
        publisher.publish(new LogisticsChannelDeletedEvent(command.id()));
    }
}
