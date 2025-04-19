package org.scm.srm.wms.application.command;

import java.util.List;

public record CreateWaveOrderCommand(
        String waveNo,
        List<String> deliveryNos,
        Integer totalSkuCount,
        Integer totalProductQuantity
) {}