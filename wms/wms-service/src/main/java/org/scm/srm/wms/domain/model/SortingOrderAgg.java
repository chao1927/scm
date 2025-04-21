package org.scm.srm.wms.domain.model;

import lombok.Getter;
import org.scm.srm.wms._share.enums.SortingOrderStatus;
import org.scm.srm.wms.adapter.infra.domain.SortingOrder;
import org.scm.srm.wms.application.command.CompleteSortingCommand;
import org.scm.srm.wms.application.command.StartSortingCommand;

public record SortingOrderAgg(SortingOrder sortingOrder) {

    public void startSorting(StartSortingCommand command) {
        sortingOrder.setSortingStatus(SortingOrderStatus.SORTING.getCode());
    }

    public void completeSorting(CompleteSortingCommand command) {
        sortingOrder.setSortingStatus(SortingOrderStatus.COMPLETED.getCode());
    }
}
