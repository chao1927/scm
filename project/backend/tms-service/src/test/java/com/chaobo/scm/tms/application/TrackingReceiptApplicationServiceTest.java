package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.DeliveryReceiptAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TrackingMapper;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TrackingReceiptApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class TrackingReceiptApplicationServiceTest {

    /**
     * 处理当前类型职责中的操作 {@code appendTrackRecordReceiptAndIgnoreDuplicateCarrierEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void appendTrackRecordReceiptAndIgnoreDuplicateCarrierEvent() {
        Services services = servicesWithWaybill();
        LocalDateTime at = LocalDateTime.parse("2026-07-12T10:00:00");
        services.callbackService.consume(new CarrierCallbackApplicationService.CarrierEvent("evt-track-1", "TRACK", "SF", "WB800001", "ARRIVED", "到达杭州", "杭州", at, 0, null, null, null, 1001L, "{}"));
        services.callbackService.consume(new CarrierCallbackApplicationService.CarrierEvent("evt-track-1", "TRACK", "SF", "WB800001", "ARRIVED", "重复到达", "杭州", at, 0, null, null, null, 1001L, "{}"));
        services.callbackService.consume(new CarrierCallbackApplicationService.CarrierEvent("evt-sign-1", "SIGNED", "SF", "WB800001", null, null, null, LocalDateTime.parse("2026-07-12T12:00:00"), DeliveryReceiptAggregate.SIGNED, "李四", null, "oss://proof/RCP1.jpg", 1001L, "{}"));
        assertThat(services.trackingMapper.tracks).hasSize(1);
        assertThat(services.trackingMapper.receipts).hasSize(1);
        assertThat(services.trackingMapper.outbox).extracting(TransportTaskMapper.OutboxRow::eventType).contains("TrackingAppended", "TransportArrived", "TransportSigned");
        assertThat(services.trackingMapper.inbox.get("evt-track-1").status()).isEqualTo(2);
    }

    /**
     * 处理当前类型职责中的操作 {@code supplementTrackThroughApplicationService}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void supplementTrackThroughApplicationService() {
        Services services = servicesWithWaybill();
        services.trackingService.supplement("WB800001", new TrackingApplicationService.SupplementCommand("IN_TRANSIT", "人工补录在途", "嘉兴", LocalDateTime.parse("2026-07-12T11:00:00"), "承运商漏推", 1001L, "idem-supplement"));
        assertThat(services.trackingService.list("WB800001")).hasSize(1);
        assertThat(services.trackingMapper.outbox).extracting(TransportTaskMapper.OutboxRow::eventType).contains("TrackingSupplemented");
    }

    /**
     * 处理当前类型职责中的操作 {@code servicesWithWaybill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Services}
     */
    static Services servicesWithWaybill() {
        WaybillApplicationServiceTest.Services base = WaybillApplicationServiceTest.servicesWithAcceptedTask();
        WaybillMapper.WaybillRow waybill = base.waybillService().createFromTask("TMS700001", new WaybillApplicationService.CreateCommand("SF", "顺丰", "SF123", "SF-EXPRESS", "ok", 1001L, "idem-wb"));
        if (!WB800001.equals(waybill.waybillNo())) {
            throw new IllegalStateException("unexpected test waybill number");
        }
        MemoryTrackingMapper trackingMapper = new MemoryTrackingMapper();
        TrackingApplicationService trackingService = new TrackingApplicationService(trackingMapper, base.waybillService());
        DeliveryReceiptApplicationService receiptService = new DeliveryReceiptApplicationService(trackingMapper, base.waybillService());
        CarrierCallbackApplicationService callbackService = new CarrierCallbackApplicationService(trackingMapper, trackingService, receiptService);
        return new Services(trackingMapper, trackingService, receiptService, callbackService);
    }

    /**
     * Services。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Services(MemoryTrackingMapper trackingMapper, TrackingApplicationService trackingService, DeliveryReceiptApplicationService receiptService, CarrierCallbackApplicationService callbackService) {
    }

    /**
     * MemoryTrackingMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public static class MemoryTrackingMapper implements TrackingMapper {

        /**
         * tracks（类型：{@code Map<String,TrackRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, TrackRow> tracks = new LinkedHashMap<>();

        /**
         * receipts（类型：{@code Map<String,ReceiptRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, ReceiptRow> receipts = new LinkedHashMap<>();

        /**
         * inbox（类型：{@code Map<String,EventInboxRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, EventInboxRow> inbox = new LinkedHashMap<>();

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
         * 查询并返回 {@code findTrackDuplicate}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param waybillNo 可追踪业务编码，类型为 {@code String}
         * @param nodeCode 可追踪业务编码，类型为 {@code String}
         * @param trackAt 业务时间，类型为 {@code LocalDateTime}
         * @return 查询并返回的结果，类型为 {@code TrackRow}
         */
        @Override
        public TrackRow findTrackDuplicate(String waybillNo, String nodeCode, LocalDateTime trackAt) {
            return tracks.values().stream().filter(row -> row.waybillNo().equals(waybillNo)).filter(row -> row.nodeCode().equals(nodeCode)).filter(row -> row.trackAt().equals(trackAt)).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code listTracks}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param waybillNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code List<TrackRow>}
         */
        @Override
        public List<TrackRow> listTracks(String waybillNo) {
            return tracks.values().stream().filter(row -> row.waybillNo().equals(waybillNo)).toList();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertTrack}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code TrackRow}
         */
        @Override
        public void insertTrack(TrackRow row) {
            tracks.put(row.trackNo(), row);
        }

        /**
         * 查询并返回 {@code findReceiptByWaybill}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param waybillNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ReceiptRow}
         */
        @Override
        public ReceiptRow findReceiptByWaybill(String waybillNo) {
            return receipts.values().stream().filter(row -> row.waybillNo().equals(waybillNo)).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code findReceipt}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param receiptNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ReceiptRow}
         */
        @Override
        public ReceiptRow findReceipt(String receiptNo) {
            return receipts.get(receiptNo);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertReceipt}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ReceiptRow}
         */
        @Override
        public void insertReceipt(ReceiptRow row) {
            receipts.put(row.receiptNo(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code claimEvent}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        @Override
        public int claimEvent(EventInboxRow row) {
            if (inbox.containsKey(row.eventId())) {
                return 0;
            }
            inbox.put(row.eventId(), row);
            return 1;
        }

        /**
         * 执行命令 {@code updateEvent}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
         */
        @Override
        public void updateEvent(EventInboxRow row) {
            inbox.put(row.eventId(), row);
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
            logs.add(row);
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
