package org.scm.bdp.service.application.converter;

import org.scm.bdp.service.adapter.infra.domain.SupplierCategory;
import org.scm.bdp.service.application.command.supplier.CreateSupplierCategoryCommand;
import org.scm.bdp.service.domain.model.SupplierCategoryAgg;

public class SupplierCategoryConverter {

    public static SupplierCategoryAgg cmdConvertAgg(CreateSupplierCategoryCommand command) {
        SupplierCategory entity = new SupplierCategory();
        entity.setName(command.name());
        entity.setDescription(command.description());
        return new SupplierCategoryAgg(entity);
    }
}
