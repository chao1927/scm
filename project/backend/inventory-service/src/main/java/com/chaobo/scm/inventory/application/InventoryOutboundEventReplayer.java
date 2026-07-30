package com.chaobo.scm.inventory.application;

/**
 * 出站失败事件人工重放端口。
 *
 * @author SCM Team
 */
public interface InventoryOutboundEventReplayer {

    /**
     * 重新投递指定失败 Outbox 事件。
     *
     * @param eventCode 事件编码
     */
    void replay(String eventCode);
}
