package com.chaobo.scm.supplier.application.shared;

import java.time.Duration;
import java.util.Optional;

public interface IdempotencyPort {
    Optional<StoredCommandResult> find(String key);
    boolean reserve(String key, String requestHash, Duration ttl);
    void complete(String key, String requestHash, CommandResult result, Duration ttl);
    void release(String key, String requestHash);

    record StoredCommandResult(String requestHash, CommandResult result) {}
}
