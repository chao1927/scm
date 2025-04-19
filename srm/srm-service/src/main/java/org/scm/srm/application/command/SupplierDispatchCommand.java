package org.scm.srm.application.command;

import java.time.LocalDateTime;

public record SupplierDispatchCommand(
        String deliveryNo,
        LocalDateTime deliveryTime,
        String logisticsNo
) {}