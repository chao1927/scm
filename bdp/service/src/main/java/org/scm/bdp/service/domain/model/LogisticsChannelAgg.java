package org.scm.bdp.service.domain.model;

import org.scm.bdp.service.adapter.infra.domain.LogisticsChannel;
import org.scm.bdp.service.application.command.CreateLogisticsChannelCommand;
import org.scm.bdp.service.application.command.UpdateLogisticsChannelCommand;

public record LogisticsChannelAgg(LogisticsChannel logisticsChannel) {

    public static LogisticsChannelAgg create(CreateLogisticsChannelCommand command) {
        // TODO 实现创建逻辑
        return null;
    }

    public Long id() {
        return logisticsChannel.getId();
    }

    public String name() {
        // TODO 实现获取逻辑
        return null;
    }

    public void update(UpdateLogisticsChannelCommand command) {


    }

    public void disable() {
        // TODO 实现禁用逻辑
    }

    public void enable() {
        // TODO 实现启用逻辑
    }

    public void delete() {
        // TODO 实现删除逻辑
    }
}
