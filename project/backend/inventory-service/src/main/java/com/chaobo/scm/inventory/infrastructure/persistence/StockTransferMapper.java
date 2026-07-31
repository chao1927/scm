package com.chaobo.scm.inventory.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;
import java.util.List;

/**
 * StockTransferMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface StockTransferMapper {

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code Row}
     */
    @Insert("insert into inv_stock_transfer(transfer_id,transfer_no,idempotency_key,owner_id,source_warehouse_id," + "target_warehouse_id,sku_code,batch_no,requested_qty,reserved_qty,outbound_qty,received_qty," + "difference_qty,difference_reason,responsible_party,evidence_ref,transfer_status,version,created_at,updated_at) values(#{id},#{transferNo}," + "#{idempotencyKey},#{ownerId},#{sourceWarehouseId},#{targetWarehouseId},#{sku},#{batchNo}," + "#{requestedQty},#{reservedQty},#{outboundQty},#{receivedQty},#{differenceQty},#{differenceReason},#{responsibleParty},#{evidenceRef},#{status},#{version}," + "now(3),now(3))")
    void insert(Row row);

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("select transfer_id id,transfer_no transferNo,idempotency_key idempotencyKey,owner_id ownerId," + "source_warehouse_id sourceWarehouseId,target_warehouse_id targetWarehouseId,sku_code sku," + "batch_no batchNo,requested_qty requestedQty,reserved_qty reservedQty,outbound_qty outboundQty," + "received_qty receivedQty,difference_qty differenceQty,difference_reason differenceReason,responsible_party responsibleParty,evidence_ref evidenceRef,transfer_status status,version " + "from inv_stock_transfer where transfer_no=#{transferNo}")
    Row find(@Param("transferNo") String transferNo);

    /**
     * 查询并返回 {@code findByIdempotencyKey}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("select transfer_id id,transfer_no transferNo,idempotency_key idempotencyKey,owner_id ownerId," + "source_warehouse_id sourceWarehouseId,target_warehouse_id targetWarehouseId,sku_code sku," + "batch_no batchNo,requested_qty requestedQty,reserved_qty reservedQty,outbound_qty outboundQty," + "received_qty receivedQty,difference_qty differenceQty,difference_reason differenceReason,responsible_party responsibleParty,evidence_ref evidenceRef,transfer_status status,version " + "from inv_stock_transfer where idempotency_key=#{idempotencyKey}")
    Row findByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 查询并返回的结果，类型为 {@code List<Row>}
     */
    @Select("select transfer_id id,transfer_no transferNo,idempotency_key idempotencyKey,owner_id ownerId," + "source_warehouse_id sourceWarehouseId,target_warehouse_id targetWarehouseId,sku_code sku," + "batch_no batchNo,requested_qty requestedQty,reserved_qty reservedQty,outbound_qty outboundQty," + "received_qty receivedQty,difference_qty differenceQty,difference_reason differenceReason,responsible_party responsibleParty,evidence_ref evidenceRef,transfer_status status,version " + "from inv_stock_transfer order by updated_at desc limit #{limit}")
    List<Row> list(@Param("limit") int limit);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
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
    @Update("update inv_stock_transfer set reserved_qty=#{reservedQty},outbound_qty=#{outboundQty}," + "received_qty=#{receivedQty},difference_qty=#{differenceQty},difference_reason=#{differenceReason}," + "responsible_party=#{responsibleParty},evidence_ref=#{evidenceRef},transfer_status=#{status}," + "version=#{version},updated_at=now(3) where transfer_id=#{id} and version=#{oldVersion}")
    int update(@Param("id") long id, @Param("reservedQty") BigDecimal reservedQty, @Param("outboundQty") BigDecimal outboundQty, @Param("receivedQty") BigDecimal receivedQty, @Param("differenceQty") BigDecimal differenceQty, @Param("differenceReason") String differenceReason, @Param("responsibleParty") String responsibleParty, @Param("evidenceRef") String evidenceRef, @Param("status") int status, @Param("version") int version, @Param("oldVersion") int oldVersion);

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long id, String transferNo, String idempotencyKey, long ownerId, long sourceWarehouseId, long targetWarehouseId, String sku, String batchNo, BigDecimal requestedQty, BigDecimal reservedQty, BigDecimal outboundQty, BigDecimal receivedQty, BigDecimal differenceQty, String differenceReason, String responsibleParty, String evidenceRef, int status, int version) {
    }
}
