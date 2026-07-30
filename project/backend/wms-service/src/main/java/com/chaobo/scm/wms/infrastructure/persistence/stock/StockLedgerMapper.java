package com.chaobo.scm.wms.infrastructure.persistence.stock;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;

/**
 * StockLedgerMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface StockLedgerMapper {

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param warehouse 业务处理参数或成员，类型为 {@code long}
     * @param location 业务处理参数或成员，类型为 {@code String}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param batch 业务处理参数或成员，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     */
    @Insert("""
        insert into wms_stock_ledger(
            ledger_id, warehouse_id, location_code, sku_code, batch_no,
            transaction_type, quantity, source_type, source_no, occurred_at, created_at
        )
        values(
            #{id}, #{warehouse}, #{location}, #{sku}, #{batch},
            #{type}, #{qty}, #{sourceType}, #{sourceNo}, now(3), now(3)
        )
        """)
    void insert(@Param("id") long id, @Param("warehouse") long warehouse, @Param("location") String location, @Param("sku") String sku, @Param("batch") String batch, @Param("type") String type, @Param("qty") BigDecimal qty, @Param("sourceType") String sourceType, @Param("sourceNo") String sourceNo);
}
