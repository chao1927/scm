package com.chaobo.scm.purchase.infrastructure.persistence.event;

import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SnowflakeLikeIdentifierGenerator。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class SnowflakeLikeIdentifierGenerator implements IdentifierGenerator {

    /**
     * sequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong sequence = new AtomicLong(System.currentTimeMillis());

    /**
     * 处理当前类型职责中的操作 {@code nextId}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Override
    public long nextId() {
        return sequence.incrementAndGet();
    }

    /**
     * 处理当前类型职责中的操作 {@code nextCode}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param prefix 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    @Override
    public String nextCode(String prefix) {
        var date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return prefix + date + sequence.incrementAndGet();
    }
}
