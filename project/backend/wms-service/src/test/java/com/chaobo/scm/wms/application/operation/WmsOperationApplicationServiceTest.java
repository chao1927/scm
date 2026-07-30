package com.chaobo.scm.wms.application.operation;

import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.infrastructure.persistence.operation.ShipmentHandoverMapper;
import com.chaobo.scm.wms.infrastructure.persistence.operation.StocktakeMapper;
import com.chaobo.scm.wms.infrastructure.persistence.operation.WarehouseExceptionMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * WmsOperationApplicationServiceTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class WmsOperationApplicationServiceTest {

    /**
     * handovers（类型：{@code HandoverMemory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final HandoverMemory handovers = new HandoverMemory();

    /**
     * stocktakes（类型：{@code StocktakeMemory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final StocktakeMemory stocktakes = new StocktakeMemory();

    /**
     * exceptions（类型：{@code ExceptionMemory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ExceptionMemory exceptions = new ExceptionMemory();

    /**
     * events（类型：{@code RecordingEventPublisher}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final RecordingEventPublisher events = new RecordingEventPublisher();

    /**
     * service（类型：{@code WmsOperationApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final WmsOperationApplicationService service = new WmsOperationApplicationService(handovers, stocktakes, exceptions, events);

    /**
     * 执行命令 {@code confirmHandoverStocktakeAndExceptionPublishesEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void confirmHandoverStocktakeAndExceptionPublishesEvents() {
        service.createHandover("HO-001", 10L);
        service.confirmHandover("HO-001", 0);
        service.createStocktake("ST-001", 1L, 2L, "SKU-001", BigDecimal.ONE);
        service.confirmStocktake("ST-001", 1L, 2L, 0);
        service.createException("EX-001", 1L, 2L, "少拣");
        service.closeException("EX-001", 1L, 2L, 0);
        assertThat(events.types()).containsExactly("WmsShipmentHandedOver", "WmsStocktakeDifferenceConfirmed", "WmsWarehouseExceptionCreated", "WmsWarehouseExceptionClosed");
    }

    /**
     * 处理当前类型职责中的操作 {@code repeatedCreateIsIdempotent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void repeatedCreateIsIdempotent() {
        service.createHandover("HO-002", 10L);
        service.createStocktake("ST-002", 1L, 2L, "SKU-001", BigDecimal.ONE);
        service.createException("EX-002", 1L, 2L, "错货");
        assertThat(service.createHandover("HO-002", 10L).duplicated()).isTrue();
        assertThat(service.createStocktake("ST-002", 1L, 2L, "SKU-001", BigDecimal.ONE).duplicated()).isTrue();
        assertThat(service.createException("EX-002", 1L, 2L, "错货").duplicated()).isTrue();
    }

    /**
     * HandoverMemory。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class HandoverMemory implements ShipmentHandoverMapper {

        /**
         * rows（类型：{@code List<Row>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<Row> rows = new ArrayList<>();

        /**
         * 查询并返回 {@code find}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param no 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Row}
         */
        public Row find(String no) {
            return rows.stream().filter(row -> row.no().equals(no)).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code insert}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param no 可追踪业务编码，类型为 {@code String}
         * @param outboundId 业务或技术标识，类型为 {@code long}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         */
        public void insert(long id, String no, long outboundId, int status, int version) {
            rows.add(new Row(id, no, outboundId, status, version));
        }

        /**
         * 执行命令 {@code update}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
         * @return 执行命令的结果，类型为 {@code int}
         */
        public int update(long id, int status, int version, int oldVersion) {
            var row = rows.stream().filter(v -> v.id() == id && v.version() == oldVersion).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(row.id(), row.no(), row.outboundId(), status, version));
            return 1;
        }
    }

    /**
     * StocktakeMemory。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class StocktakeMemory implements StocktakeMapper {

        /**
         * rows（类型：{@code List<Row>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<Row> rows = new ArrayList<>();

        /**
         * 查询并返回 {@code find}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param no 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Row}
         */
        public Row find(String no) {
            return rows.stream().filter(row -> row.no().equals(no)).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code insert}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param no 可追踪业务编码，类型为 {@code String}
         * @param warehouseId 业务或技术标识，类型为 {@code long}
         * @param sku 业务处理参数或成员，类型为 {@code String}
         * @param differenceQty 数量值，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         */
        public void insert(long id, String no, long warehouseId, long ownerId, String sku,
                           BigDecimal differenceQty, int status, int version) {
            rows.add(new Row(id, no, warehouseId, ownerId, sku, differenceQty, status, version));
        }

        /**
         * 执行命令 {@code update}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
         * @return 执行命令的结果，类型为 {@code int}
         */
        public int update(long id, int status, int version, int oldVersion) {
            var row = rows.stream().filter(v -> v.id() == id && v.version() == oldVersion).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(row.id(), row.no(), row.warehouseId(),
                row.ownerId(), row.sku(), row.differenceQty(), status, version));
            return 1;
        }
    }

    /**
     * ExceptionMemory。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class ExceptionMemory implements WarehouseExceptionMapper {

        /**
         * rows（类型：{@code List<Row>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<Row> rows = new ArrayList<>();

        /**
         * 查询并返回 {@code find}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param no 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Row}
         */
        public Row find(String no) {
            return rows.stream().filter(row -> row.no().equals(no)).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code insert}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param no 可追踪业务编码，类型为 {@code String}
         * @param reason 业务处理参数或成员，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         */
        public void insert(long id, String no, long warehouseId, long ownerId,
                           String reason, int status, int version) {
            rows.add(new Row(id, no, warehouseId, ownerId, reason, status, version));
        }

        /**
         * 执行命令 {@code update}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
         * @return 执行命令的结果，类型为 {@code int}
         */
        public int update(long id, int status, int version, int oldVersion) {
            var row = rows.stream().filter(v -> v.id() == id && v.version() == oldVersion).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(row.id(), row.no(), row.warehouseId(),
                row.ownerId(), row.reason(), status, version));
            return 1;
        }
    }

    /**
     * RecordingEventPublisher。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class RecordingEventPublisher implements WmsEventPublisher {

        /**
         * eventTypes（类型：{@code List<String>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<String> eventTypes = new ArrayList<>();

        /**
         * 执行命令 {@code publish}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param eventType 业务处理参数或成员，类型为 {@code String}
         * @param aggregateType 业务处理参数或成员，类型为 {@code String}
         * @param aggregateId 业务或技术标识，类型为 {@code String}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param payload 业务处理参数或成员，类型为 {@code String}
         */
        public void publish(String eventType, String aggregateType, String aggregateId, int version, String payload) {
            eventTypes.add(eventType);
        }

        /**
         * 处理当前类型职责中的操作 {@code types}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<String>}
         */
        List<String> types() {
            return eventTypes;
        }
    }
}
