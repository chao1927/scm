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

/**
 * TransactionalCommandExecutor。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class TransactionalCommandExecutor {

    /**
     * TTL（类型：{@code Duration}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final Duration TTL = Duration.ofHours(24);

    /**
     * idempotencyPort（类型：{@code IdempotencyPort}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdempotencyPort idempotencyPort;

    /**
     * 创建 TransactionalCommandExecutor。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param idempotencyPort 业务或技术标识，类型为 {@code IdempotencyPort}
     */
    public TransactionalCommandExecutor(IdempotencyPort idempotencyPort) {
        this.idempotencyPort = idempotencyPort;
    }

    /**
     * 处理当前类型职责中的操作 {@code execute}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param namespace 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param request 接口请求参数，类型为 {@code Object}
     * @param action 业务处理参数或成员，类型为 {@code Supplier<CommandResult>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    public CommandResult execute(String namespace, CommandContext context, Object request, Supplier<CommandResult> action) {
        String key = namespace + ":" + context.requiredIdempotencyKey();
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

                /**
                 * 处理当前类型职责中的操作 {@code afterCommit}。
                 *
                 * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
                 */
                @Override
                public void afterCommit() {
                    idempotencyPort.complete(key, hash, result, TTL);
                }

                /**
                 * 处理当前类型职责中的操作 {@code afterCompletion}。
                 *
                 * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
                 * @param status 生命周期状态，类型为 {@code int}
                 */
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        idempotencyPort.release(key, hash);
                    }
                }
            });
            return result;
        } catch (RuntimeException exception) {
            idempotencyPort.release(key, hash);
            throw exception;
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code sha256}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
