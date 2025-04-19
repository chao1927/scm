package org.scm.srm.wms.application.handler;

import org.scm.common.EventPublisher;
import org.scm.common.exception.BizException;
import org.scm.srm.wms._share.enums.WaveErrorCode;
import org.scm.srm.wms.application.command.CloseWaveOrderCommand;
import org.scm.srm.wms.application.command.CreateWaveOrderCommand;
import org.scm.srm.wms.application.event.WaveCompletedEvent;
import org.scm.srm.wms.application.event.WaveCreatedEvent;
import org.scm.srm.wms.domain.model.WaveOrderAgg;
import org.scm.srm.wms.domain.repository.WaveOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WaveOrderCommandHandler {

    @Autowired
    private WaveOrderRepository waveOrderRepository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(CreateWaveOrderCommand command) {


        WaveOrderAgg agg = WaveOrderAgg.create(command);
        waveOrderRepository.save(agg);

        eventPublisher.publish(new WaveCreatedEvent(agg.getWaveOrder().getWaveNo()));
    }

    public void handle(CloseWaveOrderCommand command) {
        WaveOrderAgg agg = waveOrderRepository.findByWaveNo(command.waveNo());
        if (agg == null) throw new BizException(WaveErrorCode.WAVE_NOT_FOUND);
        agg.complete();
        waveOrderRepository.save(agg);

        eventPublisher.publish(new WaveCompletedEvent(command.waveNo()));
    }
}
