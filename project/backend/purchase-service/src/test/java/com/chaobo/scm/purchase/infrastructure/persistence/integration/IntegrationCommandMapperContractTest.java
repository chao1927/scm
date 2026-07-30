package com.chaobo.scm.purchase.infrastructure.persistence.integration;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 采购集成命令持久化状态契约测试。
 *
 * <p>验证领取、发送成功、重试和最终失败均使用数据库状态比较更新，防止重复发送或终态重放。
 */
class IntegrationCommandMapperContractTest {

    @Test
    void dispatcherOnlyLocksPendingOrRetryableCommands() throws Exception {
        Method method = IntegrationCommandMapper.class
                .getMethod("lockDispatchable", int.class);
        String sql = String.join(" ", method.getAnnotation(Select.class).value());

        assertThat(sql)
                .contains("status in(1,4)")
                .contains("next_retry_at<=now(3)")
                .contains("for update skip locked")
                .doesNotContain("status in(1,4,5)");
    }

    @Test
    void stateChangesUseCompareAndSetAndFinalFailureIsTerminalStatusFive()
            throws Exception {
        String executing = updateSql("markExecuting", long.class);
        String succeeded = updateSql(
                "markSucceeded", long.class, String.class);
        String retry = updateSql(
                "markRetry",
                long.class,
                int.class,
                OffsetDateTime.class,
                String.class,
                int.class
        );

        assertThat(executing)
                .contains("set status=2")
                .contains("status in(1,4)");
        assertThat(succeeded)
                .contains("set status=3")
                .contains("where command_id=#{id} and status=2");
        assertThat(retry)
                .contains("status=if(retry_count+1>=#{max},5,4)")
                .contains("where command_id=#{id} and status=2")
                .contains("retry_count=#{expected}");
    }

    private static String updateSql(
            String methodName,
            Class<?>... parameterTypes
    ) throws Exception {
        Method method = IntegrationCommandMapper.class
                .getMethod(methodName, parameterTypes);
        return String.join(" ", method.getAnnotation(Update.class).value());
    }
}
