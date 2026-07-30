package com.chaobo.scm.inventory.application;

import com.chaobo.scm.inventory.infrastructure.persistence.ReturnDispositionMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReturnDispositionApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class ReturnDispositionApplicationServiceTest {

    /**
     * 处理当前类型职责中的操作 {@code appliesSellableAndQuarantineOnlyOnce}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void appliesSellableAndQuarantineOnlyOnce() {
        MemoryMapper mapper = new MemoryMapper();
        RecordingInventory inventory = new RecordingInventory();
        var service = new ReturnDispositionApplicationService(mapper, inventory);
        var command = new ReturnDispositionApplicationService.Command("E-1", "AS-1", 88, 10, "SKU-1", null, new BigDecimal("5"), new BigDecimal("2"), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO);
        var first = service.apply(command);
        var duplicate = service.apply(command);
        assertThat(first.duplicated()).isFalse();
        assertThat(duplicate.duplicated()).isTrue();
        assertThat(inventory.inbound).isEqualByComparingTo("4");
        assertThat(inventory.frozen).isEqualByComparingTo("2");
    }

    /**
     * RecordingInventory。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class RecordingInventory extends InventoryApplicationService {

        /**
         * inbound（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        BigDecimal inbound = BigDecimal.ZERO;

        /**
         * frozen（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        BigDecimal frozen = BigDecimal.ZERO;

        /**
         * 创建 RecordingInventory。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         */
        RecordingInventory() {
            super(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code inbound}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param command 用例输入命令，类型为 {@code AccountCommand}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code AccountResult}
         */
        @Override
        public AccountResult inbound(AccountCommand command) {
            inbound = inbound.add(command.qty());
            return null;
        }

        /**
         * 执行命令 {@code freeze}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param command 用例输入命令，类型为 {@code AccountCommand}
         * @return 执行命令的结果，类型为 {@code AccountResult}
         */
        @Override
        public AccountResult freeze(AccountCommand command) {
            frozen = frozen.add(command.qty());
            return null;
        }
    }

    /**
     * MemoryMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class MemoryMapper implements ReturnDispositionMapper {

        /**
         * row（类型：{@code Row}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        Row row;

        /**
         * 查询并返回 {@code findByEvent}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param sourceEventId 业务或技术标识，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Row}
         */
        @Override
        public Row findByEvent(String sourceEventId) {
            return row != null && row.sourceEventId().equals(sourceEventId) ? row : null;
        }

        /**
         * 处理当前类型职责中的操作 {@code insert}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code Row}
         */
        @Override
        public void insert(Row row) {
            this.row = row;
        }
    }
}
