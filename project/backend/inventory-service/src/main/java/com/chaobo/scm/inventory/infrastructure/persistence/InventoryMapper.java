package com.chaobo.scm.inventory.infrastructure.persistence;

import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * InventoryMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface InventoryMapper {

    /**
     * AccountRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record AccountRow(long id, long ownerId, long warehouseId, String sku, String batchNo, BigDecimal onHandQty, BigDecimal availableQty, BigDecimal reservedQty, BigDecimal frozenQty, int version) {
    }

    /**
     * LedgerRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record LedgerRow(long id, String ledgerNo, long accountId, String type, BigDecimal qtyDelta, String sourceSystem, String sourceNo) {
    }

    /**
     * ReservationRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ReservationRow(long id, String reservationNo, long accountId, String sourceSystem, String sourceNo, BigDecimal reservedQty, BigDecimal releasedQty, int status, int version) {
    }

    /**
     * 查询并返回 {@code findAccount}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param ownerId 业务或技术标识，类型为 {@code long}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param batchNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code AccountRow}
     */
    @Select("select * from inv_stock_balance where owner_id=#{ownerId} and warehouse_id=#{warehouseId} and sku_code=#{sku} and ifnull(batch_no,'')=ifnull(#{batchNo},'')")
    AccountRow findAccount(@Param("ownerId") long ownerId, @Param("warehouseId") long warehouseId, @Param("sku") String sku, @Param("batchNo") String batchNo);

    /**
     * 查询并返回 {@code findAccountById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code AccountRow}
     */
    @Select("select * from inv_stock_balance where stock_id=#{id}")
    AccountRow findAccountById(@Param("id") long id);

    /**
     * 处理当前类型职责中的操作 {@code accounts}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<AccountRow>}
     */
    @Select("select * from inv_stock_balance order by updated_at desc limit #{limit}")
    List<AccountRow> accounts(@Param("limit") int limit);

    /**
     * 处理当前类型职责中的操作 {@code insertAccount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param ownerId 业务或技术标识，类型为 {@code long}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param batchNo 可追踪业务编码，类型为 {@code String}
     * @param onHand 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param available 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param reserved 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param frozen 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    @Insert("insert into inv_stock_balance(stock_id,owner_id,warehouse_id,sku_code,batch_no,on_hand_qty,available_qty,reserved_qty,frozen_qty,version,created_at,updated_at) values(#{id},#{ownerId},#{warehouseId},#{sku},#{batchNo},#{onHand},#{available},#{reserved},#{frozen},#{version},now(3),now(3))")
    void insertAccount(@Param("id") long id, @Param("ownerId") long ownerId, @Param("warehouseId") long warehouseId, @Param("sku") String sku, @Param("batchNo") String batchNo, @Param("onHand") BigDecimal onHand, @Param("available") BigDecimal available, @Param("reserved") BigDecimal reserved, @Param("frozen") BigDecimal frozen, @Param("version") int version);

    /**
     * 执行命令 {@code updateAccount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param onHand 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param available 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param reserved 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param frozen 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("update inv_stock_balance set on_hand_qty=#{onHand},available_qty=#{available},reserved_qty=#{reserved},frozen_qty=#{frozen},version=#{version},updated_at=now(3) where stock_id=#{id} and version=#{oldVersion}")
    int updateAccount(@Param("id") long id, @Param("onHand") BigDecimal onHand, @Param("available") BigDecimal available, @Param("reserved") BigDecimal reserved, @Param("frozen") BigDecimal frozen, @Param("version") int version, @Param("oldVersion") int oldVersion);

    /**
     * 处理当前类型职责中的操作 {@code insertLedger}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param accountId 业务或技术标识，类型为 {@code long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     */
    @Insert("insert into inv_stock_ledger(ledger_id,ledger_no,stock_id,ledger_type,qty_delta,source_system,source_order_no,created_at) values(#{id},#{no},#{accountId},#{type},#{qty},#{sourceSystem},#{sourceNo},now(3))")
    void insertLedger(@Param("id") long id, @Param("no") String no, @Param("accountId") long accountId, @Param("type") String type, @Param("qty") BigDecimal qty, @Param("sourceSystem") String sourceSystem, @Param("sourceNo") String sourceNo);

    /**
     * 处理当前类型职责中的操作 {@code ledgers}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<LedgerRow>}
     */
    @Select("select ledger_id id,ledger_no ledgerNo,stock_id accountId,ledger_type type,qty_delta qtyDelta,source_system sourceSystem,source_order_no sourceNo from inv_stock_ledger order by ledger_id desc limit #{limit}")
    List<LedgerRow> ledgers(@Param("limit") int limit);

    /**
     * 查询并返回 {@code findReservation}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param reservationNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ReservationRow}
     */
    @Select("select * from inv_reservation where reservation_no=#{reservationNo}")
    ReservationRow findReservation(@Param("reservationNo") String reservationNo);

    /**
     * 查询并返回 {@code findReservationBySource}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ReservationRow}
     */
    @Select("select * from inv_reservation where source_system=#{sourceSystem} and source_order_no=#{sourceNo}")
    ReservationRow findReservationBySource(@Param("sourceSystem") String sourceSystem, @Param("sourceNo") String sourceNo);

    /**
     * 处理当前类型职责中的操作 {@code insertReservation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param accountId 业务或技术标识，类型为 {@code long}
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param reserved 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param released 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    @Insert("insert into inv_reservation(reservation_id,reservation_no,stock_id,source_system,source_order_no,reserved_qty,released_qty,reservation_status,version,created_at,updated_at) values(#{id},#{no},#{accountId},#{sourceSystem},#{sourceNo},#{reserved},#{released},#{status},#{version},now(3),now(3))")
    void insertReservation(@Param("id") long id, @Param("no") String no, @Param("accountId") long accountId, @Param("sourceSystem") String sourceSystem, @Param("sourceNo") String sourceNo, @Param("reserved") BigDecimal reserved, @Param("released") BigDecimal released, @Param("status") int status, @Param("version") int version);

    /**
     * 执行命令 {@code updateReservation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param released 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("update inv_reservation set released_qty=#{released},reservation_status=#{status},version=#{version},updated_at=now(3) where reservation_id=#{id} and version=#{oldVersion}")
    int updateReservation(@Param("id") long id, @Param("released") BigDecimal released, @Param("status") int status, @Param("version") int version, @Param("oldVersion") int oldVersion);
}
