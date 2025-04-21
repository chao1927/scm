package org.scm.bdp.service.domain.model;

import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.bdp.service.adapter.infra.domain.Product;

import java.math.BigDecimal;

public record ProductAgg(Product product) {

    public Long id() {
        return this.product.getId();
    }

    public void enable() {
        this.product.setStatus(SwitchStatus.ENABLED.getValue());
    }

    public void disable() {
        this.product.setStatus(SwitchStatus.DISABLED.getValue());
    }

    public void initSpu() {

    }

    public void initSku() {

    }

    public void update(String name, String description, Long categoryId, Long unitId) {
        product.setName(name);
        product.setDescription(description);
        product.setCategoryId(categoryId);
        product.setUnitId(unitId);
    }

    public void updateAttributes(String keyAttributes, String salesAttributes) {
        product.setKeyAttributes(keyAttributes);
        product.setSalesAttributes(salesAttributes);
    }

    public void updatePrice(BigDecimal referencePurchasePrice, BigDecimal referenceSalesPrice) {
        product.setReferencePurchasePrice(referencePurchasePrice);
        product.setReferenceSalesPrice(referenceSalesPrice);
    }
}
