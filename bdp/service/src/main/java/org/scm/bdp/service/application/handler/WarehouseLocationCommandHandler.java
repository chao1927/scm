package org.scm.bdp.service.application.handler;

import org.scm.bdp.service.application.command.warehouse.*;
import org.scm.bdp.service.application.converter.StorageLocationConverter;
import org.scm.bdp.service.application.event.warehouse.*;
import org.scm.bdp.service.domain.model.StorageLocationAgg;
import org.scm.bdp.service.domain.model.WarehouseAgg;
import org.scm.bdp.service.domain.repository.StorageLocationRepository;
import org.scm.bdp.service.domain.repository.WarehouseRepository;
import org.scm.common.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.scm.bdp.service._share.enums.StorageLocationMixingStrategy;

@Component
public class WarehouseLocationCommandHandler {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private StorageLocationRepository storageLocationRepository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(CreateLocationCommand command) {
        // 校验库位code是否存在
        storageLocationRepository.checkCodeExist(command.code());

        // 校验库位混合策略
        StorageLocationMixingStrategy.checkStrategy(command.mixingStrategy());

        // 校验仓库是否存在
        warehouseRepository.checkExistById(command.warehouseId());

        // 转换聚合
        StorageLocationAgg agg = StorageLocationConverter.cmdConvertAgg(command);
        storageLocationRepository.save(agg);

        // 发送库位创建事件
        eventPublisher.publish(new LocationCreatedEvent(agg.id()));
    }

    public void handle(UpdateLocationCommand command) {
        // 校验库位code是否存在
        storageLocationRepository.checkCodeDuplicate(command.id(), command.code());

        // 校验库位混合策略
        StorageLocationMixingStrategy.checkStrategy(command.mixingStrategy());

        // 校验仓库是否存在
        warehouseRepository.checkExistById(command.warehouseId());

        // 更新聚合
        StorageLocationAgg agg = storageLocationRepository.findById(command.id());
        agg.update(
                command.warehouseId(),
                command.code(),
                command.maxVolume(),
                command.maxWeight(),
                command.mixingStrategy()
        );

        // 发送库位更新事件
        eventPublisher.publish(new LocationUpdatedEvent(command.id()));
    }

    public void handle(DisableLocationCommand command) {
        StorageLocationAgg agg = storageLocationRepository.findById(command.locationId());
        agg.disable();
        storageLocationRepository.save(agg);
        eventPublisher.publish(new LocationDisabledEvent(command.locationId()));
    }

    public void handle(EnableLocationCommand command) {
        StorageLocationAgg agg = storageLocationRepository.findById(command.locationId());
        agg.enable();
        storageLocationRepository.save(agg);
        eventPublisher.publish(new LocationEnabledEvent(command.locationId()));
    }

    public void handle(DeleteLocationCommand command) {
        storageLocationRepository.checkExistById(command.locationId());

        storageLocationRepository.deleteById(command.locationId());
        eventPublisher.publish(new LocationDeletedEvent(command.locationId()));
    }
}
