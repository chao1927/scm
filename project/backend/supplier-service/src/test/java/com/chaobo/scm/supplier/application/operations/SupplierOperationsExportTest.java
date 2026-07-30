package com.chaobo.scm.supplier.application.operations;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.supplier.application.shared.AuditLogRepository;
import com.chaobo.scm.supplier.application.shared.CommandContext;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import com.chaobo.scm.supplier.infrastructure.persistence.operations.SupplierOperationsMapper;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SupplierOperationsExportTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class SupplierOperationsExportTest {

    /**
     * fakeMapper（类型：{@code FakeOperationsMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final FakeOperationsMapper fakeMapper = new FakeOperationsMapper();

    /**
     * mapper（类型：{@code SupplierOperationsMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierOperationsMapper mapper = fakeMapper.proxy();

    /**
     * service（类型：{@code SupplierOperationsApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierOperationsApplicationService service = new SupplierOperationsApplicationService(mapper, new FixedIdentifierGenerator(), noopAudit());

    /**
     * 执行命令 {@code createExportUsesSupplierScopeAndWritesTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createExportUsesSupplierScopeAndWritesTask() {
        var id = service.createExport("WARNING", 3001L, "{\"status\":1}", context(3001L, "supplier:export:create"));
        assertThat(id).isEqualTo(9001L);
        assertThat(fakeMapper.insertedExportId).isEqualTo(9001L);
        assertThat(fakeMapper.insertedExportType).isEqualTo("WARNING");
        assertThat(fakeMapper.insertedSupplierId).isEqualTo(3001L);
        assertThat(fakeMapper.insertedQueryJson).isEqualTo("{\"status\":1}");
    }

    /**
     * 执行命令 {@code createExportRejectsUnsupportedType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createExportRejectsUnsupportedType() {
        assertThatThrownBy(() -> service.createExport("UNKNOWN", null, "{}", context(null, "supplier:export:create"))).isInstanceOf(BusinessException.class).hasMessageContaining("不支持的导出类型");
    }

    /**
     * 执行命令 {@code completeExportRequiresSuccessfulVersionUpdate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void completeExportRequiresSuccessfulVersionUpdate() {
        fakeMapper.completeResult = 1;
        service.completeExport(9001L, 0, "oss://bucket/file.csv", context(null, "supplier:export:complete"));
        assertThat(fakeMapper.completedExportId).isEqualTo(9001L);
        assertThat(fakeMapper.completedFileUrl).isEqualTo("oss://bucket/file.csv");
    }

    /**
     * 处理当前类型职责中的操作 {@code context}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param supplierScopeId 业务或技术标识，类型为 {@code Long}
     * @param permission 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandContext}
     */
    private CommandContext context(Long supplierScopeId, String permission) {
        return new CommandContext(1001L, "测试用户", 1L, supplierScopeId, "REQ-1", "TRACE-1", "IDEMP-1", Set.of(permission));
    }

    /**
     * 处理当前类型职责中的操作 {@code noopAudit}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code AuditLogRepository}
     */
    private AuditLogRepository noopAudit() {
        return (context, operationType, targetType, targetId, targetNo, beforeSnapshot, afterSnapshot) -> {
        };
    }

    /**
     * FakeOperationsMapper。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static final class FakeOperationsMapper {

        /**
         * insertedExportId（类型：{@code long}）。
         *
         * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
         */
        long insertedExportId;

        /**
         * insertedExportType（类型：{@code String}）。
         *
         * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
         */
        String insertedExportType;

        /**
         * insertedSupplierId（类型：{@code Long}）。
         *
         * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
         */
        Long insertedSupplierId;

        /**
         * insertedQueryJson（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        String insertedQueryJson;

        /**
         * completedExportId（类型：{@code long}）。
         *
         * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
         */
        long completedExportId;

        /**
         * completedFileUrl（类型：{@code String}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        String completedFileUrl;

        /**
         * completeResult（类型：{@code int}）。
         *
         * <p>保存当前对象所需的处理结果；其具体生命周期由所属对象统一管理。
         */
        int completeResult;

        /**
         * 处理当前类型职责中的操作 {@code proxy}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierOperationsMapper}
         */
        SupplierOperationsMapper proxy() {
            return (SupplierOperationsMapper) Proxy.newProxyInstance(SupplierOperationsMapper.class.getClassLoader(), new Class<?>[] { SupplierOperationsMapper.class }, (target, method, args) -> switch(method.getName()) {
                case "insertExport" ->
                    {
                        insertedExportId = (Long) args[0];
                        insertedExportType = (String) args[1];
                        insertedSupplierId = (Long) args[2];
                        insertedQueryJson = (String) args[3];
                        yield null;
                    }
                case "completeExport" ->
                    {
                        completedExportId = (Long) args[0];
                        completedFileUrl = (String) args[2];
                        yield completeResult;
                    }
                case "workItems", "warnings", "failedInbound", "failedOutbound", "reconciliations", "exportTasks" ->
                    java.util.List.of();
                case "exportTask", "dashboard" ->
                    null;
                case "localAsnCount", "localReturnCount", "localStatementCount" ->
                    0L;
                case "localStatementAmount" ->
                    BigDecimal.ZERO;
                case "insertWork", "insertWarning", "processWork", "processWarning", "replayInbound", "replayOutbound", "failExport" ->
                    1;
                case "upsertReconciliation" ->
                    null;
                default ->
                    throw new UnsupportedOperationException(method.getName());
            });
        }
    }

    /**
     * FixedIdentifierGenerator。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static final class FixedIdentifierGenerator implements IdentifierGenerator {

        /**
         * 处理当前类型职责中的操作 {@code nextId}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        @Override
        public long nextId() {
            return 9001L;
        }

        /**
         * 处理当前类型职责中的操作 {@code nextBusinessNo}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param prefix 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        @Override
        public String nextBusinessNo(String prefix) {
            return prefix + "9001";
        }
    }
}
