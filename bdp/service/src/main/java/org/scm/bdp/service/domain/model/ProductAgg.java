package org.scm.bdp.service.domain.model;

import lombok.AllArgsConstructor;
import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.bdp.service.adapter.infra.domain.Product;
import org.scm.bdp.service.application.command.product.UpdateProductCommand;

@AllArgsConstructor
public record ProductAgg(Product product) {

    private Long id() {
        return this.product.getId();
    }

    public void enable() {
        this.product.setStatus(SwitchStatus.ENABLED.getValue());
    }

    public void disable() {
        this.product.setStatus(SwitchStatus.DISABLED.getValue());
    }


    public void update(UpdateProductCommand command) {
        // TODO: 更新商品信息
    }

    public void delete() {

    }
}
