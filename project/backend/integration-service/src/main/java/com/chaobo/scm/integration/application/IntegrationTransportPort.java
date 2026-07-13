package com.chaobo.scm.integration.application;

public interface IntegrationTransportPort {
    DeliveryResult deliver(DeliveryRequest request);

    record DeliveryRequest(String messageNo, String messageType, String sourceSystem, String targetSystem,
                           String channelType, String businessNo, String idempotencyKey, String payload) {}

    record DeliveryResult(boolean success, String failureReason, long durationMillis) {
        public static DeliveryResult success(long durationMillis) {
            return new DeliveryResult(true, null, Math.max(durationMillis, 0));
        }

        public static DeliveryResult failed(String failureReason, long durationMillis) {
            if (failureReason == null || failureReason.isBlank()) {
                throw new IllegalArgumentException("delivery failure reason is required");
            }
            return new DeliveryResult(false, failureReason, Math.max(durationMillis, 0));
        }
    }
}
