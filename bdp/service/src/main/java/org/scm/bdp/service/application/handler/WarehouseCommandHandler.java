package org.scm.bdp.service.application.handler;

import org.scm.bdp.service._share.enums.WarehouseType;
import org.scm.bdp.service.application.command.warehouse.*;
import org.scm.bdp.service.application.converter.WarehouseConverter;
import org.scm.bdp.service.application.event.warehouse.*;
import org.scm.bdp.service.domain.model.WarehouseAgg;
import org.scm.bdp.service.domain.repository.WarehouseRepository;
import org.scm.common.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WarehouseCommandHandler {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(CreateWarehouseCommand command) {
        // 验证名称是否已存在
        warehouseRepository.checkNameExist(command.name());

        // 验证类型
        WarehouseType.checkType(command.type());

        // 转换命令为聚合根
        WarehouseAgg agg = WarehouseConverter.cmdConvertAgg(command);
        warehouseRepository.save(agg);

        // 发送仓库创建事件
        eventPublisher.publish(new WarehouseCreatedEvent(agg.id()));
    }

    public void handle(UpdateWarehouseCommand command) {
        // 验证名称是否已存在
        warehouseRepository.checkNameDuplicate(command.id(), command.name());

        // 验证类型
        WarehouseType.checkType(command.type());

        // 更新聚合根
        WarehouseAgg agg = warehouseRepository.findById(command.id());
        agg.update(command.name(), command.description(), command.address(), command.type(), command.area(), command.manager(), command.managerPhone());
        warehouseRepository.save(agg);

        // 发送仓库更新事件
        eventPublisher.publish(new WarehouseUpdatedEvent(command.id()));
    }

    public void handle(DisableWarehouseCommand command) {
        // 禁用聚合根
        WarehouseAgg agg = warehouseRepository.findById(command.id());
        agg.disable();
        warehouseRepository.save(agg);

        // 发送仓库禁用事件
        eventPublisher.publish(new WarehouseDisabledEvent(agg.warehouse().getId()));
    }

    public void handle(EnableWarehouseCommand command) {
        // 启用聚合
        WarehouseAgg agg = warehouseRepository.findById(command.id());
        agg.enable();
        warehouseRepository.save(agg);
        eventPublisher.publish(new WarehouseEnabledEvent(agg.warehouse().getId()));
    }

    public void handle(DeleteWarehouseCommand command) {
        warehouseRepository.checkExistById(command.id());


        warehouseRepository.deleteById(command.id());

        eventPublisher.publish(new WarehouseDeletedEvent(command.id()));
    }
}
