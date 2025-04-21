package org.scm.bdp.service.application.handler;

import org.scm.bdp.service.application.command.*;
import org.scm.bdp.service.application.converter.LogisticsChannelConverter;
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
        LogisticsChannelAgg agg = LogisticsChannelConverter.cmdConvertAgg(command);

        // TODO 校验物流覆盖区域

        // TODO 校验物流计费名称

        repository.save(agg);
        publisher.publish(new LogisticsChannelCreatedEvent(agg.id()));
    }

    public void handle(UpdateLogisticsChannelCommand command) {
        LogisticsChannelAgg agg = repository.findById(command.id());
        // TODO 校验物流覆盖区域

        // TODO 校验物流计费名称

        agg.update(command.name(), command.serviceType(), command.coverageArea(), command.freightCalculationRules());
        repository.save(agg);

        publisher.publish(new LogisticsChannelUpdatedEvent(command.id()));
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
        repository.checkExistById(command.id());
        repository.deleteById(command.id());

        publisher.publish(new LogisticsChannelDeletedEvent(command.id()));
    }
}
