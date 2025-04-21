package org.scm.srm.wms.domain.model;

import org.scm.srm.wms._share.enums.WaveStatus;
import org.scm.srm.wms.adapter.infra.domain.WaveOrder;
import org.scm.srm.wms.application.command.CreateWaveOrderCommand;

public record WaveOrderAgg(WaveOrder waveOrder) {

    public static WaveOrderAgg create(CreateWaveOrderCommand command) {
        // TODO 实现创建逻辑
        return null;
    }

    public void complete() {
        this.waveOrder.setWaveStatus(WaveStatus.COMPLETED.getCode());
    }
}
