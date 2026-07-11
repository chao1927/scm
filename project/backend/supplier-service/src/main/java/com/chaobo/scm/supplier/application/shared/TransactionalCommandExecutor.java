package com.chaobo.scm.supplier.application.shared;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.function.Supplier;

@Component
public class TransactionalCommandExecutor {
    private static final Duration TTL = Duration.ofHours(24);
    private final IdempotencyPort idempotencyPort;

    public TransactionalCommandExecutor(IdempotencyPort idempotencyPort) {
        this.idempotencyPort = idempotencyPort;
    }

    public CommandResult execute(String namespace, CommandContext context, Object request,
                                 Supplier<CommandResult> action) {
        String key = namespace + ":" + context.idempotencyKey();
        String hash = sha256(request.toString());
        var stored = idempotencyPort.find(key);
        if (stored.isPresent()) {
            if (!stored.get().requestHash().equals(hash)) {
                throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "同一幂等键对应了不同请求内容");
            }
            if (stored.get().result() == null) {
                throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "相同命令正在处理中");
            }
            return stored.get().result().asIdempotentHit();
        }
        if (!idempotencyPort.reserve(key, hash, TTL)) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "相同命令正在处理中");
        }
        try {
            CommandResult result = action.get();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { idempotencyPort.complete(key, hash, result, TTL); }
                @Override public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) idempotencyPort.release(key, hash);
                }
            });
            return result;
        } catch (RuntimeException exception) {
            idempotencyPort.release(key, hash);
            throw exception;
        }
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
