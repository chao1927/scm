package com.chaobo.scm.inventory.application;

import com.chaobo.scm.inventory.domain.StockTransferAggregate;
import com.chaobo.scm.inventory.infrastructure.persistence.StockTransferMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * StockTransferApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class StockTransferApplicationServiceTest {

    /**
     * 执行命令 {@code createsIdempotentlyAndMovesApprovedTransferToTransitWithOutboxFacts}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createsIdempotentlyAndMovesApprovedTransferToTransitWithOutboxFacts() {
        MemoryMapper mapper = new MemoryMapper();
        MemoryStock stock = new MemoryStock();
        List<String> events = new ArrayList<>();
        StockTransferApplicationService service = new StockTransferApplicationService(mapper, stock, (type, transferNo, payload) -> events.add(type));
        var command = new StockTransferApplicationService.CreateCommand(1, 10, 20, "SKU-1", null, new BigDecimal("5"));
        var created = service.create(command, "transfer-create-1");
        var duplicate = service.create(command, "transfer-create-1");
        var submitted = service.submit(created.transferNo(), created.version());
        var approved = service.approve(created.transferNo(), submitted.version());
        var reserved = service.reserve(created.transferNo(), approved.version());
        var outbound = service.recordOutbound(created.transferNo(), new BigDecimal("5"), reserved.version());
        var inTransit = service.markInTransit(created.transferNo(), outbound.version());
        var received = service.receive(created.transferNo(), new BigDecimal("4"), true, inTransit.version());
        var confirmed = service.confirmDifference(created.transferNo(), received.version());
        assertThat(duplicate.duplicated()).isTrue();
        assertThat(confirmed.status()).isEqualTo(StockTransferAggregate.DIFFERENCE_CONFIRMED);
        assertThat(confirmed.differenceQty()).isEqualByComparingTo("1");
        assertThat(stock.reservedTransferNo).isEqualTo(created.transferNo());
        assertThat(stock.outboundQty).isEqualByComparingTo("5");
        assertThat(stock.inboundQty).isEqualByComparingTo("4");
        assertThat(events).containsExactly("TransferCreated", "TransferSubmitted", "TransferApproved", "TransferStockReserved", "TransferOutboundCompleted", "TransferInTransit", "TransferDifferenceRaised", "TransferDifferenceConfirmed");
    }

    /**
     * MemoryStock。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static final class MemoryStock implements StockTransferPorts.StockReservation {

        /**
         * reservedTransferNo（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        String reservedTransferNo;

        /**
         * outboundQty（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
         */
        BigDecimal outboundQty;

        /**
         * inboundQty（类型：{@code BigDecimal}）。
         *
         * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
         */
        BigDecimal inboundQty;

        /**
         * 执行命令 {@code reserve}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param ownerId 业务或技术标识，类型为 {@code long}
         * @param warehouseId 业务或技术标识，类型为 {@code long}
         * @param sku 业务处理参数或成员，类型为 {@code String}
         * @param batchNo 可追踪业务编码，类型为 {@code String}
         * @param qty 数量值，类型为 {@code BigDecimal}
         * @param transferNo 可追踪业务编码，类型为 {@code String}
         * @return 执行命令的结果，类型为 {@code String}
         */
        @Override
        public String reserve(long ownerId, long warehouseId, String sku, String batchNo, BigDecimal qty, String transferNo) {
            reservedTransferNo = transferNo;
            return "RSV-1";
        }

        /**
         * 执行命令 {@code releaseForTransfer}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param transferNo 可追踪业务编码，类型为 {@code String}
         */
        @Override
        public void releaseForTransfer(String transferNo) {
        }

        /**
         * 处理当前类型职责中的操作 {@code outboundForTransfer}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param transferNo 可追踪业务编码，类型为 {@code String}
         * @param qty 数量值，类型为 {@code BigDecimal}
         */
        @Override
        public void outboundForTransfer(String transferNo, BigDecimal qty) {
            outboundQty = qty;
        }

        /**
         * 处理当前类型职责中的操作 {@code inboundForTransfer}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param ownerId 业务或技术标识，类型为 {@code long}
         * @param warehouseId 业务或技术标识，类型为 {@code long}
         * @param sku 业务处理参数或成员，类型为 {@code String}
         * @param batchNo 可追踪业务编码，类型为 {@code String}
         * @param qty 数量值，类型为 {@code BigDecimal}
         * @param transferNo 可追踪业务编码，类型为 {@code String}
         */
        @Override
        public void inboundForTransfer(long ownerId, long warehouseId, String sku, String batchNo, BigDecimal qty, String transferNo) {
            inboundQty = qty;
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
    static final class MemoryMapper implements StockTransferMapper {

        /**
         * row（类型：{@code Row}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        Row row;

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

        /**
         * 查询并返回 {@code find}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param transferNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Row}
         */
        @Override
        public Row find(String transferNo) {
            return row != null && row.transferNo().equals(transferNo) ? row : null;
        }

        /**
         * 查询并返回 {@code findByIdempotencyKey}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param key 业务处理参数或成员，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Row}
         */
        @Override
        public Row findByIdempotencyKey(String key) {
            return row != null && row.idempotencyKey().equals(key) ? row : null;
        }

        /**
         * 查询并返回 {@code list}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 查询并返回的结果，类型为 {@code List<Row>}
         */
        @Override
        public List<Row> list(int limit) {
            return row == null ? List.of() : List.of(row);
        }

        /**
         * 执行命令 {@code update}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param reservedQty 数量值，类型为 {@code BigDecimal}
         * @param outboundQty 数量值，类型为 {@code BigDecimal}
         * @param receivedQty 数量值，类型为 {@code BigDecimal}
         * @param differenceQty 数量值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
         * @return 执行命令的结果，类型为 {@code int}
         */
        @Override
        public int update(long id, BigDecimal reservedQty, BigDecimal outboundQty, BigDecimal receivedQty, BigDecimal differenceQty, int status, int version, int oldVersion) {
            if (row == null || row.version() != oldVersion) {
                return 0;
            }
            row = new Row(row.id(), row.transferNo(), row.idempotencyKey(), row.ownerId(), row.sourceWarehouseId(), row.targetWarehouseId(), row.sku(), row.batchNo(), row.requestedQty(), reservedQty, outboundQty, receivedQty, differenceQty, status, version);
            return 1;
        }
    }
}
