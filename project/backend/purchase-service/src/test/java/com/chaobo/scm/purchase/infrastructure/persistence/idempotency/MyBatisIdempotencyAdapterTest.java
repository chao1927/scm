package com.chaobo.scm.purchase.infrastructure.persistence.idempotency;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MyBatisIdempotencyAdapterTest。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class MyBatisIdempotencyAdapterTest {

    /**
     * mapper（类型：{@code MemoryMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final MemoryMapper mapper = new MemoryMapper();

    /**
     * adapter（类型：{@code MyBatisIdempotencyAdapter}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MyBatisIdempotencyAdapter adapter = new MyBatisIdempotencyAdapter(mapper);

    /**
     * 执行命令 {@code completedRequestIsReusedAndDifferentDigestIsRejected}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void completedRequestIsReusedAndDifferentDigestIsRejected() {
        AtomicInteger executions = new AtomicInteger();
        CommandResult first = adapter.execute("purchase:po:create", context("KEY-1", "digest-a"), () -> result(executions.incrementAndGet()));
        CommandResult duplicate = adapter.execute("purchase:po:create", context("KEY-1", "digest-a"), () -> result(executions.incrementAndGet()));
        assertThat(first.duplicated()).isFalse();
        assertThat(duplicate.duplicated()).isTrue();
        assertThat(executions).hasValue(1);
        assertThatThrownBy(() -> adapter.execute("purchase:po:create", context("KEY-1", "digest-b"), () -> result(2))).isInstanceOf(BusinessException.class);
    }

    /**
     * 处理当前类型职责中的操作 {@code processingRequestRejectsConcurrentExecutionAndFailedRequestCanRetry}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void processingRequestRejectsConcurrentExecutionAndFailedRequestCanRetry() {
        mapper.insertProcessing("purchase:po:create", "BUSY", "digest-busy");
        assertThatThrownBy(() -> adapter.execute("purchase:po:create", context("BUSY", "digest-busy"), () -> result(1))).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> adapter.execute("purchase:po:create", context("RETRY", "digest-retry"), () -> {
            throw new IllegalStateException("temporary failure");
        })).isInstanceOf(IllegalStateException.class);
        CommandResult retried = adapter.execute("purchase:po:create", context("RETRY", "digest-retry"), () -> result(3));
        assertThat(retried.id()).isEqualTo(3);
        assertThat(mapper.find("purchase:po:create", "RETRY").status()).isEqualTo(2);
    }

    /**
     * 处理当前类型职责中的操作 {@code context}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param digest 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandContext}
     */
    private static CommandContext context(String key, String digest) {
        return new CommandContext(1, "buyer", 1, 10L, "request", "trace", key, Set.of("purchase:*"), digest);
    }

    /**
     * 处理当前类型职责中的操作 {@code result}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private static CommandResult result(long id) {
        return new CommandResult(id, "PO-" + id, 1, "DRAFT", 0, "EVENT-" + id, false);
    }

    /**
     * MemoryMapper。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static final class MemoryMapper implements PurchaseIdempotencyMapper {

        /**
         * rows（类型：{@code Map<String,Row>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final Map<String, Row> rows = new LinkedHashMap<>();

        /**
         * 处理当前类型职责中的操作 {@code insertProcessing}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param businessType 业务处理参数或成员，类型为 {@code String}
         * @param idempotencyKey 业务或技术标识，类型为 {@code String}
         * @param requestDigest 接口请求参数，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        @Override
        public int insertProcessing(String businessType, String idempotencyKey, String requestDigest) {
            String key = key(businessType, idempotencyKey);
            if (rows.containsKey(key)) {
                throw new DuplicateKeyException("duplicate");
            }
            rows.put(key, new Row(businessType, idempotencyKey, requestDigest, 1, null, null, null, null, null, null));
            return 1;
        }

        /**
         * 查询并返回 {@code find}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param businessType 业务处理参数或成员，类型为 {@code String}
         * @param idempotencyKey 业务或技术标识，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Row}
         */
        @Override
        public Row find(String businessType, String idempotencyKey) {
            return rows.get(key(businessType, idempotencyKey));
        }

        /**
         * 执行命令 {@code complete}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param businessType 业务处理参数或成员，类型为 {@code String}
         * @param idempotencyKey 业务或技术标识，类型为 {@code String}
         * @param result 处理结果，类型为 {@code CommandResult}
         * @return 执行命令的结果，类型为 {@code int}
         */
        @Override
        public int complete(String businessType, String idempotencyKey, CommandResult result) {
            Row row = find(businessType, idempotencyKey);
            if (row == null || row.status() != 1) {
                return 0;
            }
            rows.put(key(businessType, idempotencyKey), new Row(businessType, idempotencyKey, row.requestDigest(), 2, result.id(), result.businessNo(), result.status(), result.statusName(), result.version(), result.eventCode()));
            return 1;
        }

        /**
         * 执行命令 {@code retryFailed}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param businessType 业务处理参数或成员，类型为 {@code String}
         * @param idempotencyKey 业务或技术标识，类型为 {@code String}
         * @param requestDigest 接口请求参数，类型为 {@code String}
         * @return 执行命令的结果，类型为 {@code int}
         */
        @Override
        public int retryFailed(String businessType, String idempotencyKey, String requestDigest) {
            Row row = find(businessType, idempotencyKey);
            if (row == null || row.status() != DELETE_FAILED_VALUE_3 || !row.requestDigest().equals(requestDigest)) {
                return 0;
            }
            rows.put(key(businessType, idempotencyKey), new Row(businessType, idempotencyKey, requestDigest, 1, null, null, null, null, null, null));
            return 1;
        }

        /**
         * 处理当前类型职责中的操作 {@code fail}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param businessType 业务处理参数或成员，类型为 {@code String}
         * @param idempotencyKey 业务或技术标识，类型为 {@code String}
         * @param reason 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        @Override
        public int fail(String businessType, String idempotencyKey, String reason) {
            Row row = find(businessType, idempotencyKey);
            if (row == null || row.status() != 1) {
                return 0;
            }
            rows.put(key(businessType, idempotencyKey), new Row(businessType, idempotencyKey, row.requestDigest(), 3, null, null, null, null, null, null));
            return 1;
        }

        /**
         * 执行命令 {@code deleteFailed}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param businessType 业务处理参数或成员，类型为 {@code String}
         * @param idempotencyKey 业务或技术标识，类型为 {@code String}
         * @return 执行命令的结果，类型为 {@code int}
         */
        @Override
        public int deleteFailed(String businessType, String idempotencyKey) {
            Row row = find(businessType, idempotencyKey);
            if (row == null || row.status() != DELETE_FAILED_VALUE_3) {
                return 0;
            }
            rows.remove(key(businessType, idempotencyKey));
            return 1;
        }

        /**
         * 处理当前类型职责中的操作 {@code key}。
         *
         * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
         * @param type 业务处理参数或成员，类型为 {@code String}
         * @param idempotencyKey 业务或技术标识，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        private static String key(String type, String idempotencyKey) {
            return type + ":" + idempotencyKey;
        }

        /**
         * 业务常量 {@code DELETE_FAILED_VALUE_3}。
         *
         * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
         */
        private static final int DELETE_FAILED_VALUE_3 = 3;
    }
}
