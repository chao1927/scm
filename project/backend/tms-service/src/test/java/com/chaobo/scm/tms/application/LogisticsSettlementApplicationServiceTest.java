package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.LogisticsExceptionAggregate;
import com.chaobo.scm.tms.domain.LogisticsFeeSourceAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.LogisticsSettlementMapper;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * LogisticsSettlementApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class LogisticsSettlementApplicationServiceTest {

    /**
     * 处理当前类型职责中的操作 {@code registerCloseExceptionGenerateAndPushFeeSource}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void registerCloseExceptionGenerateAndPushFeeSource() {
        Services services = servicesWithWaybill();
        LogisticsSettlementMapper.ExceptionRow exception = services.exceptionService.register(new LogisticsExceptionApplicationService.RegisterCommand("WB800001", "DAMAGED", "P1", "外包装破损", "CARRIER", 1001L, "idem-exc"));
        LogisticsSettlementMapper.ExceptionRow closed = services.exceptionService.close(exception.exceptionNo(), new LogisticsExceptionApplicationService.CloseCommand("已索赔", "CARRIER", exception.version(), 1001L, "idem-close"));
        LogisticsSettlementMapper.FeeSourceRow feeSource = services.feeSourceService.generate("WB800001", new LogisticsFeeSourceApplicationService.GenerateCommand("FREIGHT", new BigDecimal("12.30"), "CNY", "202607", "SHIPPER", 1001L, "idem-fee"));
        LogisticsSettlementMapper.FeeSourceRow repeated = services.feeSourceService.generate("WB800001", new LogisticsFeeSourceApplicationService.GenerateCommand("FREIGHT", new BigDecimal("99.99"), "CNY", "202607", "SHIPPER", 1001L, "idem-fee-repeat"));
        LogisticsSettlementMapper.FeeSourceRow pushed = services.feeSourceService.pushBms(feeSource.feeSourceNo(), new LogisticsFeeSourceApplicationService.PushCommand("BMS1", 1001L, "idem-push"));
        assertThat(closed.status()).isEqualTo(LogisticsExceptionAggregate.CLOSED);
        assertThat(repeated.feeSourceNo()).isEqualTo(feeSource.feeSourceNo());
        assertThat(pushed.pushStatus()).isEqualTo(LogisticsFeeSourceAggregate.PUSHED);
        assertThat(services.mapper.outbox).extracting(TransportTaskMapper.OutboxRow::eventType).contains("LogisticsExceptionRegistered", "LogisticsExceptionClosed", "LogisticsFeeSourceGenerated", "LogisticsFeeSourcePushed");
    }

    /**
     * 执行命令 {@code rejectCloseExceptionWithWrongVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectCloseExceptionWithWrongVersion() {
        Services services = servicesWithWaybill();
        LogisticsSettlementMapper.ExceptionRow exception = services.exceptionService.register(new LogisticsExceptionApplicationService.RegisterCommand("WB800001", "DAMAGED", "P1", "外包装破损", "CARRIER", 1001L, "idem-exc"));
        assertThatThrownBy(() -> services.exceptionService.close(exception.exceptionNo(), new LogisticsExceptionApplicationService.CloseCommand("已索赔", "CARRIER", 9, 1001L, "idem-close"))).isInstanceOf(IllegalStateException.class).hasMessageContaining("version conflict");
    }

    /**
     * 处理当前类型职责中的操作 {@code servicesWithWaybill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Services}
     */
    public static Services servicesWithWaybill() {
        WaybillApplicationServiceTest.Services base = WaybillApplicationServiceTest.servicesWithAcceptedTask();
        WaybillMapper.WaybillRow waybill = base.waybillService().createFromTask("TMS700001", new WaybillApplicationService.CreateCommand("SF", "顺丰", "SF123", "SF-EXPRESS", "ok", 1001L, "idem-wb"));
        if (!WB800001.equals(waybill.waybillNo())) {
            throw new IllegalStateException("unexpected test waybill number");
        }
        MemoryLogisticsSettlementMapper mapper = new MemoryLogisticsSettlementMapper();
        LogisticsExceptionApplicationService exceptionService = new LogisticsExceptionApplicationService(mapper, base.waybillService());
        LogisticsFeeSourceApplicationService feeSourceService = new LogisticsFeeSourceApplicationService(mapper, base.waybillService());
        return new Services(mapper, exceptionService, feeSourceService);
    }

    /**
     * Services。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Services(MemoryLogisticsSettlementMapper mapper, LogisticsExceptionApplicationService exceptionService, LogisticsFeeSourceApplicationService feeSourceService) {
    }

    /**
     * MemoryLogisticsSettlementMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public static class MemoryLogisticsSettlementMapper implements LogisticsSettlementMapper {

        /**
         * exceptions（类型：{@code Map<String,ExceptionRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, ExceptionRow> exceptions = new LinkedHashMap<>();

        /**
         * feeSources（类型：{@code Map<String,FeeSourceRow>}）。
         *
         * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
         */
        final Map<String, FeeSourceRow> feeSources = new LinkedHashMap<>();

        /**
         * outbox（类型：{@code List<TransportTaskMapper.OutboxRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<TransportTaskMapper.OutboxRow> outbox = new ArrayList<>();

        /**
         * logs（类型：{@code List<TransportTaskMapper.OperationLogRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<TransportTaskMapper.OperationLogRow> logs = new ArrayList<>();

        /**
         * 查询并返回 {@code findException}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param exceptionNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ExceptionRow}
         */
        @Override
        public ExceptionRow findException(String exceptionNo) {
            return exceptions.get(exceptionNo);
        }

        /**
         * 查询并返回 {@code listExceptions}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<ExceptionRow>}
         */
        @Override
        public List<ExceptionRow> listExceptions() {
            return new ArrayList<>(exceptions.values());
        }

        /**
         * 处理当前类型职责中的操作 {@code insertException}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ExceptionRow}
         */
        @Override
        public void insertException(ExceptionRow row) {
            exceptions.put(row.exceptionNo(), row);
        }

        /**
         * 执行命令 {@code updateException}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ExceptionRow}
         */
        @Override
        public void updateException(ExceptionRow row) {
            exceptions.put(row.exceptionNo(), row);
        }

        /**
         * 查询并返回 {@code findFeeSource}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param feeSourceNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code FeeSourceRow}
         */
        @Override
        public FeeSourceRow findFeeSource(String feeSourceNo) {
            return feeSources.get(feeSourceNo);
        }

        /**
         * 查询并返回 {@code findFeeSourceByWaybillAndItem}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param waybillNo 可追踪业务编码，类型为 {@code String}
         * @param feeItemCode 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code FeeSourceRow}
         */
        @Override
        public FeeSourceRow findFeeSourceByWaybillAndItem(String waybillNo, String feeItemCode) {
            return feeSources.values().stream().filter(row -> row.waybillNo().equals(waybillNo)).filter(row -> row.feeItemCode().equals(feeItemCode)).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code listFeeSources}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<FeeSourceRow>}
         */
        @Override
        public List<FeeSourceRow> listFeeSources() {
            return new ArrayList<>(feeSources.values());
        }

        /**
         * 处理当前类型职责中的操作 {@code insertFeeSource}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code FeeSourceRow}
         */
        @Override
        public void insertFeeSource(FeeSourceRow row) {
            feeSources.put(row.feeSourceNo(), row);
        }

        /**
         * 执行命令 {@code updateFeeSource}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code FeeSourceRow}
         */
        @Override
        public void updateFeeSource(FeeSourceRow row) {
            feeSources.put(row.feeSourceNo(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOutbox}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code TransportTaskMapper.OutboxRow}
         */
        @Override
        public void insertOutbox(TransportTaskMapper.OutboxRow row) {
            outbox.add(row);
        }

        /**
         * 查询并返回 {@code listOutbox}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<TransportTaskMapper.OutboxRow>}
         */
        @Override
        public List<TransportTaskMapper.OutboxRow> listOutbox() {
            return outbox;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOperationLog}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code TransportTaskMapper.OperationLogRow}
         */
        @Override
        public void insertOperationLog(TransportTaskMapper.OperationLogRow row) {
            logs.add(new TransportTaskMapper.OperationLogRow(row.operationType(), row.businessNo(), row.operatorId(), row.idempotencyKey(), LocalDateTime.now()));
        }

        /**
         * 查询并返回 {@code listOperationLogs}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<TransportTaskMapper.OperationLogRow>}
         */
        @Override
        public List<TransportTaskMapper.OperationLogRow> listOperationLogs() {
            return logs;
        }
    }

    /**
     * 业务常量 {@code WB800001}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String WB800001 = "WB800001";
}
