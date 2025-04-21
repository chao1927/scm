package org.scm.bdp.service.application.converter;

import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.bdp.service.adapter.infra.domain.ProductCategory;
import org.scm.bdp.service.application.command.product.CreateProductCategoryCommand;
import org.scm.bdp.service.domain.model.ProductCategoryAgg;

public class ProductCategoryConverter {


    public static ProductCategoryAgg cmdConvertAgg(CreateProductCategoryCommand cmd) {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setName(cmd.name());
        productCategory.setParentId(cmd.parentId());
        productCategory.setAttributes(cmd.attributes());
        productCategory.setSortOrder(cmd.sortOrder());
        productCategory.setStatus(SwitchStatus.DISABLED.getValue());
        return new ProductCategoryAgg(productCategory);
    }
}
