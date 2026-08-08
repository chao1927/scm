package com.chaobo.scm.purchase.application.shared;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** 仅供单元测试使用的进程内幂等端口。生产环境必须使用持久化实现。 */
public class InMemoryIdempotencyPort implements IdempotencyPort {

    private final Map<String, CommandResult> cache = new ConcurrentHashMap<>();

    @Override
    public Optional<CommandResult> find(String businessType, String idempotencyKey) {
        return Optional.ofNullable(cache.get(key(businessType, idempotencyKey)));
    }

    @Override
    public CommandResult execute(String businessType, CommandContext context,
                                 Supplier<CommandResult> action) {
        String key = key(businessType, context.requiredIdempotencyKey());
        CommandResult existing = cache.get(key);
        if (existing != null) {
            return new CommandResult(existing.id(), existing.businessNo(), existing.status(),
                existing.statusName(), existing.version(), existing.eventCode(), true);
        }
        CommandResult result = action.get();
        cache.put(key, result);
        return result;
    }

    private static String key(String businessType, String idempotencyKey) {
        return businessType + ':' + idempotencyKey;
    }
}
