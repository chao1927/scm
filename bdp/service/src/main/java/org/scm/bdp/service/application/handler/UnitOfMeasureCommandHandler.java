package org.scm.bdp.service.application.handler;

import org.scm.bdp.service.adapter.infra.domain.UnitOfMeasure;
import org.scm.bdp.service.adapter.repository.impl.UnitOfMeasureRepositoryImpl;
import org.scm.bdp.service.application.command.product.*;
import org.scm.bdp.service.domain.model.UnitOfMeasureAgg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UnitOfMeasureCommandHandler {

    @Autowired
    private UnitOfMeasureRepositoryImpl unitOfMeasureRepository;

    public void handle(CreateUnitOfMeasureCommand command) {
        UnitOfMeasure unitOfMeasure = new UnitOfMeasure();
        unitOfMeasure.setName(command.name());

        UnitOfMeasureAgg agg = new UnitOfMeasureAgg(unitOfMeasure);
        agg.init();
        unitOfMeasureRepository.save(agg);
    }

    public void handle(UpdateUnitOfMeasureCommand command) {
        UnitOfMeasureAgg agg = unitOfMeasureRepository.findById(command.id());
        agg.update(command.name());
        unitOfMeasureRepository.save(agg);
    }

    public void handle(DeleteUnitOfMeasureCommand command) {
        unitOfMeasureRepository.checkExistById(command.id());
        unitOfMeasureRepository.deleteById(command.id());
    }

    public void handle(DisableUnitOfMeasureCommand command) {
        UnitOfMeasureAgg agg = unitOfMeasureRepository.findById(command.id());
        agg.disable();
        unitOfMeasureRepository.save(agg);
    }

    public void handle(EnableUnitOfMeasureCommand command) {
        UnitOfMeasureAgg agg = unitOfMeasureRepository.findById(command.id());
        agg.enable();
        unitOfMeasureRepository.save(agg);
    }
}
