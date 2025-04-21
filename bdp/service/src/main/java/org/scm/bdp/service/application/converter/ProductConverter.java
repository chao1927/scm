package org.scm.bdp.service.application.converter;

import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.bdp.service.adapter.infra.domain.Product;
import org.scm.bdp.service.application.command.product.CreateProductCommand;
import org.scm.bdp.service.domain.model.ProductAgg;

public class ProductConverter {
    public static ProductAgg cmdConvertAgg(CreateProductCommand command) {
        Product product = new Product();
        product.setName(command.name());
        product.setDescription(command.description());
        product.setCategoryId(command.categoryId());
        product.setUnitId(command.unitId());
        product.setKeyAttributes(command.keyAttributes());
        product.setSalesAttributes(command.salesAttributes());
        product.setReferencePurchasePrice(command.referencePurchasePrice());
        product.setReferenceSalesPrice(command.referenceSalesPrice());
        product.setStatus(SwitchStatus.DISABLED.getValue());
        return new ProductAgg(product);
    }
}
