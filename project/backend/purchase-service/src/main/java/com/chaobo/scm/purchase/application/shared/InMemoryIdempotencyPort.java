package com.chaobo.scm.purchase.application.shared;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class InMemoryIdempotencyPort implements IdempotencyPort {
    private final Map<String, CommandResult> cache = new ConcurrentHashMap<>();

    @Override
    public Optional<CommandResult> find(String businessType, String idempotencyKey) {
        return Optional.ofNullable(cache.get(key(businessType, idempotencyKey)));
    }

    @Override
    public CommandResult execute(String businessType, CommandContext context, Supplier<CommandResult> action) {
        var key = key(businessType, context.requiredIdempotencyKey());
        var existing = cache.get(key);
        if (existing != null) {
            return new CommandResult(
                    existing.id(),
                    existing.businessNo(),
                    existing.status(),
                    existing.statusName(),
                    existing.version(),
                    existing.eventCode(),
                    true);
        }
        var result = action.get();
        cache.put(key, result);
        return result;
    }

    private static String key(String businessType, String idempotencyKey) {
        return businessType + ":" + idempotencyKey;
    }
}
