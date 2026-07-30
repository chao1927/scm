package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.ShippingLabelAggregate;
import com.chaobo.scm.tms.domain.TransportTaskAggregate;
import com.chaobo.scm.tms.domain.WaybillAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * WaybillApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class WaybillApplicationServiceTest {

    /**
     * 执行命令 {@code createWaybillGenerateAndPrintLabel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createWaybillGenerateAndPrintLabel() {
        Services services = servicesWithAcceptedTask();
        WaybillMapper.WaybillRow waybill = services.waybillService.createFromTask("TMS700001", new WaybillApplicationService.CreateCommand("SF", "顺丰", "SF123", "SF-EXPRESS", "ok", 1001L, "idem-wb"));
        WaybillMapper.WaybillRow repeated = services.waybillService.createFromTask("TMS700001", new WaybillApplicationService.CreateCommand("SF", "顺丰", "SF999", "SF-EXPRESS", "repeat", 1001L, "idem-repeat"));
        WaybillMapper.LabelRow label = services.labelService.generate(waybill.waybillNo(), new ShippingLabelApplicationService.GenerateCommand("PKG1", "SF-V1", "oss://labels/LBL1.pdf", 1001L, "idem-label"));
        WaybillMapper.LabelRow printed = services.labelService.print(label.labelNo(), new ShippingLabelApplicationService.PrintCommand("PRINTER-1", 1001L, "idem-print"));
        assertThat(repeated.waybillNo()).isEqualTo(waybill.waybillNo());
        assertThat(waybill.status()).isEqualTo(WaybillAggregate.CREATED);
        assertThat(printed.status()).isEqualTo(ShippingLabelAggregate.PRINTED);
        assertThat(services.waybillMapper.outbox).extracting(TransportTaskMapper.OutboxRow::eventType).contains("WaybillCreated", "ShippingLabelGenerated", "ShippingLabelPrinted");
        assertThat(services.waybillMapper.logs).extracting(TransportTaskMapper.OperationLogRow::operationType).contains("CREATE_WAYBILL", "GENERATE_SHIPPING_LABEL", "PRINT_SHIPPING_LABEL");
    }

    /**
     * 处理当前类型职责中的操作 {@code voidWaybillRejectsStaleVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void voidWaybillRejectsStaleVersion() {
        Services services = servicesWithAcceptedTask();
        WaybillMapper.WaybillRow waybill = services.waybillService.createFromTask("TMS700001", new WaybillApplicationService.CreateCommand("SF", "顺丰", "SF123", "SF-EXPRESS", "ok", 1001L, "idem-wb"));
        assertThatThrownBy(() -> services.waybillService.voidWaybill(waybill.waybillNo(), new WaybillApplicationService.VoidCommand("客户取消", "APR1", 9, 1001L, "idem-void"))).isInstanceOf(IllegalStateException.class).hasMessageContaining("version conflict");
    }

    /**
     * 处理当前类型职责中的操作 {@code servicesWithAcceptedTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Services}
     */
    public static Services servicesWithAcceptedTask() {
        TransportTaskApplicationServiceTest.MemoryTransportTaskMapper taskMapper = new TransportTaskApplicationServiceTest.MemoryTransportTaskMapper();
        TransportTaskApplicationService transportTaskService = new TransportTaskApplicationService(taskMapper);
        TransportTaskMapper.TaskRow created = transportTaskService.createFromSource(TransportTaskApplicationServiceTest.createCommand("idem-task"));
        transportTaskService.accept(created.taskNo(), new TransportTaskApplicationService.AcceptCommand("SF", "顺丰", "SF-EXPRESS", created.version(), 1001L, "idem-accept"));
        MemoryWaybillMapper waybillMapper = new MemoryWaybillMapper();
        WaybillApplicationService waybillService = new WaybillApplicationService(waybillMapper, transportTaskService);
        ShippingLabelApplicationService labelService = new ShippingLabelApplicationService(waybillMapper, waybillService);
        return new Services(waybillMapper, waybillService, labelService);
    }

    /**
     * Services。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Services(MemoryWaybillMapper waybillMapper, WaybillApplicationService waybillService, ShippingLabelApplicationService labelService) {
    }

    /**
     * MemoryWaybillMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public static class MemoryWaybillMapper implements WaybillMapper {

        /**
         * waybills（类型：{@code Map<String,WaybillRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, WaybillRow> waybills = new LinkedHashMap<>();

        /**
         * labels（类型：{@code Map<String,LabelRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, LabelRow> labels = new LinkedHashMap<>();

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
         * 查询并返回 {@code findWaybill}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param waybillNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code WaybillRow}
         */
        @Override
        public WaybillRow findWaybill(String waybillNo) {
            return waybills.get(waybillNo);
        }

        /**
         * 查询并返回 {@code findActiveWaybillByTask}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param taskNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code WaybillRow}
         */
        @Override
        public WaybillRow findActiveWaybillByTask(String taskNo) {
            return waybills.values().stream().filter(row -> row.taskNo().equals(taskNo)).filter(row -> row.status() != WaybillAggregate.VOIDED).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code listWaybills}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<WaybillRow>}
         */
        @Override
        public List<WaybillRow> listWaybills() {
            return new ArrayList<>(waybills.values());
        }

        /**
         * 处理当前类型职责中的操作 {@code insertWaybill}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code WaybillRow}
         */
        @Override
        public void insertWaybill(WaybillRow row) {
            waybills.put(row.waybillNo(), row);
        }

        /**
         * 执行命令 {@code updateWaybill}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code WaybillRow}
         */
        @Override
        public void updateWaybill(WaybillRow row) {
            waybills.put(row.waybillNo(), row);
        }

        /**
         * 查询并返回 {@code findLabel}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param labelNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code LabelRow}
         */
        @Override
        public LabelRow findLabel(String labelNo) {
            return labels.get(labelNo);
        }

        /**
         * 查询并返回 {@code findActiveLabel}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param waybillNo 可追踪业务编码，类型为 {@code String}
         * @param packageNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code LabelRow}
         */
        @Override
        public LabelRow findActiveLabel(String waybillNo, String packageNo) {
            return labels.values().stream().filter(row -> row.waybillNo().equals(waybillNo)).filter(row -> row.packageNo().equals(packageNo)).filter(row -> row.status() != ShippingLabelAggregate.VOIDED).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code listLabelsByWaybill}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param waybillNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code List<LabelRow>}
         */
        @Override
        public List<LabelRow> listLabelsByWaybill(String waybillNo) {
            return labels.values().stream().filter(row -> row.waybillNo().equals(waybillNo)).toList();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertLabel}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code LabelRow}
         */
        @Override
        public void insertLabel(LabelRow row) {
            labels.put(row.labelNo(), row);
        }

        /**
         * 执行命令 {@code updateLabel}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code LabelRow}
         */
        @Override
        public void updateLabel(LabelRow row) {
            labels.put(row.labelNo(), row);
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
}
