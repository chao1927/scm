package com.chaobo.scm.purchase.infrastructure.persistence.idempotency;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.application.shared.IdempotencyPort;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * MyBatisIdempotencyAdapter。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class MyBatisIdempotencyAdapter implements IdempotencyPort {

    /**
     * mapper（类型：{@code PurchaseIdempotencyMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseIdempotencyMapper mapper;

    /**
     * 创建 MyBatisIdempotencyAdapter。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code PurchaseIdempotencyMapper}
     */
    public MyBatisIdempotencyAdapter(PurchaseIdempotencyMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<CommandResult>}
     */
    @Override
    public Optional<CommandResult> find(String businessType, String idempotencyKey) {
        PurchaseIdempotencyMapper.Row row = mapper.find(businessType, idempotencyKey);
        return row != null && row.status() == 2 ? Optional.of(result(row, false)) : Optional.empty();
    }

    /**
     * 处理当前类型职责中的操作 {@code execute}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param action 业务处理参数或成员，类型为 {@code Supplier<CommandResult>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Override
    public CommandResult execute(String businessType, CommandContext context, Supplier<CommandResult> action) {
        String key = context.requiredIdempotencyKey();
        String digest = context.requiredRequestDigest();
        PurchaseIdempotencyMapper.Row existing = claimOrLoad(businessType, key, digest);
        if (existing != null) {
            verifyDigest(existing, digest);
            if (existing.status() == EXECUTE_VALUE_2) {
                return result(existing, true);
            }
            if (existing.status() == 1) {
                throw conflict("幂等请求正在处理");
            }
            if (mapper.retryFailed(businessType, key, digest) != 1) {
                throw conflict("幂等失败请求已被其他线程重试");
            }
        }
        try {
            CommandResult result = action.get();
            if (mapper.complete(businessType, key, result) != 1) {
                throw conflict("幂等请求完成状态冲突");
            }
            return result;
        } catch (RuntimeException exception) {
            mapper.fail(businessType, key, message(exception));
            throw exception;
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code claimOrLoad}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param digest 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchaseIdempotencyMapper.Row}
     */
    private PurchaseIdempotencyMapper.Row claimOrLoad(String businessType, String key, String digest) {
        try {
            mapper.insertProcessing(businessType, key, digest);
            return null;
        } catch (DuplicateKeyException duplicate) {
            PurchaseIdempotencyMapper.Row existing = mapper.find(businessType, key);
            if (existing == null) {
                throw conflict("幂等占用状态不可见，请重试");
            }
            return existing;
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code verifyDigest}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code PurchaseIdempotencyMapper.Row}
     * @param digest 业务处理参数或成员，类型为 {@code String}
     */
    private static void verifyDigest(PurchaseIdempotencyMapper.Row row, String digest) {
        if (!row.requestDigest().equals(digest)) {
            throw conflict("同一幂等键不得用于不同请求内容");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code result}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code PurchaseIdempotencyMapper.Row}
     * @param duplicated 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private static CommandResult result(PurchaseIdempotencyMapper.Row row, boolean duplicated) {
        return new CommandResult(row.resultId(), row.resultBusinessNo(), row.resultStatus(), row.resultStatusName(), row.resultVersion(), row.resultEventCode(), duplicated);
    }

    /**
     * 处理当前类型职责中的操作 {@code conflict}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param message 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private static BusinessException conflict(String message) {
        return new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, message);
    }

    /**
     * 处理当前类型职责中的操作 {@code message}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param exception 业务处理参数或成员，类型为 {@code RuntimeException}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String message(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null ? exception.getClass().getSimpleName() : message.substring(0, Math.min(500, message.length()));
    }

    /**
     * 业务常量 {@code EXECUTE_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int EXECUTE_VALUE_2 = 2;
}
