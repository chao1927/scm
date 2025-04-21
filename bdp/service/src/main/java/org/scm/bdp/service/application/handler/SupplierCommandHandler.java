package org.scm.bdp.service.application.handler;

import org.scm.bdp.service.application.command.supplier.*;
import org.scm.bdp.service.application.converter.SupplierConverter;
import org.scm.bdp.service.application.event.supplier.*;
import org.scm.bdp.service.domain.model.SupplierAgg;
import org.scm.bdp.service.domain.repository.SupplierCategoryRepository;
import org.scm.bdp.service.domain.repository.SupplierRepository;
import org.scm.common.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SupplierCommandHandler {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SupplierCategoryRepository supplierCategoryRepository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(CreateSupplierCommand command) {
        // 校验供应商名称是否存在
        supplierRepository.checkNameExist(command.name());

        // 校验供应商分类是否存在
        supplierCategoryRepository.checkExistById(command.categoryId());

        // 转换聚合
        SupplierAgg agg = SupplierConverter.cmdConvertAgg(command);
        supplierRepository.save(agg);
        eventPublisher.publish(new SupplierCreatedEvent(agg.id()));
    }

    public void handle(UpdateSupplierCommand command) {
        // 校验供应商名称是否存在
        supplierRepository.checkExistById(command.categoryId());

        // 校验供应商分类是否存在
        supplierCategoryRepository.checkExistById(command.categoryId());

        // 校验供应商名称重复
        supplierRepository.checkNameDuplicate(command.id(), command.name());

        // 更新供应商
        SupplierAgg agg = supplierRepository.findById(command.id());
        agg.update(command);
        supplierRepository.save(agg);

        // 发送供应商更新事件
        eventPublisher.publish(new SupplierUpdatedEvent(command.id()));
    }

    public void handle(DisableSupplierCommand command) {
        // 校验供应商是否存在
        SupplierAgg agg = supplierRepository.findById(command.id());
        agg.disable();
        supplierRepository.save(agg);

        // 发送供应商禁用事件
        eventPublisher.publish(new SupplierDisabledEvent(command.id()));
    }

    public void handle(EnableSupplierCommand command) {
        // 校验供应商是否存在
        SupplierAgg agg = supplierRepository.findById(command.id());
        agg.enable();
        supplierRepository.save(agg);

        // 发送供应商启用事件
        eventPublisher.publish(new SupplierEnabledEvent(command.id()));
    }

    public void handle(DeleteSupplierCommand command) {
        // 校验供应商是否存在
        supplierRepository.checkExistById(command.id());

        // 删除供应商
        supplierRepository.deleteById(command.id());

        // 发送供应商删除事件
        eventPublisher.publish(new SupplierDeletedEvent(command.id()));
    }
}
