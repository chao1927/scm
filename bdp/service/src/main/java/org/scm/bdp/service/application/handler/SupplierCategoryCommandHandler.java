package org.scm.bdp.service.application.handler;

import org.scm.bdp.service.application.command.supplier.CreateSupplierCategoryCommand;
import org.scm.bdp.service.application.command.supplier.DeleteSupplierCategoryCommand;
import org.scm.bdp.service.application.command.supplier.UpdateSupplierCategoryCommand;
import org.scm.bdp.service.application.converter.SupplierCategoryConverter;
import org.scm.bdp.service.application.event.supplier.SupplierCategoryCreatedEvent;
import org.scm.bdp.service.application.event.supplier.SupplierCategoryDeletedEvent;
import org.scm.bdp.service.application.event.supplier.SupplierCategoryUpdatedEvent;
import org.scm.bdp.service.domain.model.SupplierCategoryAgg;
import org.scm.bdp.service.domain.repository.SupplierCategoryRepository;
import org.scm.common.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SupplierCategoryCommandHandler {

    @Autowired
    private SupplierCategoryRepository supplierCategoryRepository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(CreateSupplierCategoryCommand command) {
        // 判断分类是否已经存在
        supplierCategoryRepository.checkNameExist(command.name());

        SupplierCategoryAgg agg = SupplierCategoryConverter.cmdConvertAgg(command);
        supplierCategoryRepository.save(agg);

        // 发布商品分类创建事件
        eventPublisher.publish(new SupplierCategoryCreatedEvent(agg.id()));
    }

    public void handle(UpdateSupplierCategoryCommand command) {
        // 判断分类名称是否重复
        supplierCategoryRepository.checkNameDuplicate(command.id(), command.name());

        // 更新商品分类
        SupplierCategoryAgg agg = supplierCategoryRepository.findById(command.id());
        agg.update(command.name(), command.description());
        supplierCategoryRepository.save(agg);

        // 发布商品分类更新事件
        eventPublisher.publish(new SupplierCategoryUpdatedEvent(agg.category().getId()));
    }

    public void handle(DeleteSupplierCategoryCommand command) {
        // 判断分类是否存在
        supplierCategoryRepository.checkExistById(command.id());

        // 删除商品分类
        supplierCategoryRepository.deleteById(command.id());

        // 发布商品分类删除事件
        eventPublisher.publish(new SupplierCategoryDeletedEvent(command.id()));
    }
}
