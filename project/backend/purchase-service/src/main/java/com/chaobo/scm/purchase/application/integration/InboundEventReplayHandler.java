package com.chaobo.scm.purchase.application.integration;

public interface InboundEventReplayHandler {
    String consumerName();

    void replay(InboundEventLogPort.ReplayEvent event);
}
