package org.scm.bdp.service.domain.model;

import lombok.AllArgsConstructor;
import org.scm.bdp.service.adapter.infra.domain.ProductCategory;
import org.scm.bdp.service.application.command.product.CreateProductCategoryCommand;
import org.scm.bdp.service.application.command.product.UpdateProductCategoryCommand;


@AllArgsConstructor
public record ProductCategoryAgg(ProductCategory category) {

    private Long id() {
        return category.getId();
    }

    public static ProductCategoryAgg create(CreateProductCategoryCommand cmd) {
        ProductCategoryAgg agg = new ProductCategoryAgg();
        // todo 实现创建逻辑
        return agg;
    }

    public void update(UpdateProductCategoryCommand cmd) {
        // todo 实现更新逻辑
    }

    public void delete() {
        // todo 实现删除逻辑
    }

    public void enable() {
        // todo 实现启用逻辑
    }

    public void disable() {
        // todo 实现禁用逻辑
    }
}
