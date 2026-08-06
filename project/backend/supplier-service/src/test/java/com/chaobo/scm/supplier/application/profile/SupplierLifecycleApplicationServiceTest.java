package com.chaobo.scm.supplier.application.profile;

import com.chaobo.scm.supplier.application.integration.IntegrationCommand;
import com.chaobo.scm.supplier.application.integration.IntegrationCommandEnqueuer;
import com.chaobo.scm.supplier.application.integration.IntegrationCommandRepository;
import com.chaobo.scm.supplier.application.masterdata.MasterDataSnapshotPort;
import com.chaobo.scm.supplier.application.shared.CommandContext;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证供应商生命周期命令的权限、数据范围与主数据协同边界。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class SupplierLifecycleApplicationServiceTest {

    /**
     * 验证冻结由供应商上下文登记主数据状态变更命令，不直接篡改正式档案。
     */
    @Test
    void shouldEnqueueSupplierFreezeCommand() {
        var commands = new InMemoryIntegrationCommandRepository();
        var sequence = new AtomicLong(1);
        IdentifierGenerator ids = new IdentifierGenerator() {
            @Override
            public long nextId() {
                return sequence.getAndIncrement();
            }

            @Override
            public String nextBusinessNo(String prefix) {
                return prefix + sequence.getAndIncrement();
            }
        };
        var integrations = new IntegrationCommandEnqueuer(commands, ids, new ObjectMapper());
        var service = new SupplierLifecycleApplicationService(new SupplierSnapshotStub(), integrations);
        var context = new CommandContext(1L, "风险管理员", 1L, 101L, "REQ-1", "TRACE-1",
                "IDEM-1", Set.of("supplier:lifecycle:manage"));

        service.change(101L, 4, "质量整改逾期", context);

        assertThat(commands.saved).isNotNull();
        assertThat(commands.saved.type()).isEqualTo("MDM_CHANGE_SUPPLIER_STATUS");
        assertThat(commands.saved.aggregateId()).isEqualTo(101L);
        assertThat(commands.saved.aggregateVersion()).isEqualTo(7);
        assertThat(commands.saved.payloadJson()).contains("\"targetStatus\":4", "质量整改逾期");
    }

    /** 提供固定供应商快照，用于验证命令编排而不依赖外部服务。 */
    private static final class SupplierSnapshotStub implements MasterDataSnapshotPort {
        @Override
        public Optional<SupplierSnapshot> findSupplier(long supplierId) {
            return Optional.of(new SupplierSnapshot(101L, "SUP-101", "华东供应商", 3, 1, "{}", 7L));
        }

        @Override
        public Optional<SkuSnapshot> findSku(String skuCode) {
            return Optional.empty();
        }

        @Override
        public void saveSupplier(SupplierSnapshot snapshot) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void saveSku(SkuSnapshot snapshot) {
            throw new UnsupportedOperationException();
        }
    }

    /** 仅记录本用例产生的一条集成命令。 */
    private static final class InMemoryIntegrationCommandRepository implements IntegrationCommandRepository {
        private IntegrationCommand saved;

        @Override
        public void save(IntegrationCommand command) {
            saved = command;
        }

        @Override
        public List<IntegrationCommand> lockDispatchable(int size) {
            return List.of();
        }

        @Override
        public boolean markExecuting(long id) {
            return false;
        }

        @Override
        public void markSucceeded(long id, String reference) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void markRetry(long id, int expectedRetry, OffsetDateTime nextRetry, String reason, int maxRetries) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void retryManually(long id, String reason) {
            throw new UnsupportedOperationException();
        }
    }
}
