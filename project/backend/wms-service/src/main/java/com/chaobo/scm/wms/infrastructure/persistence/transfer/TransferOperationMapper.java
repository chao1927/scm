package com.chaobo.scm.wms.infrastructure.persistence.transfer;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;

/**
 * TransferOperationMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface TransferOperationMapper {

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("select operation_id id,transfer_no transferNo,owner_id ownerId,source_warehouse_id sourceWarehouseId,target_warehouse_id targetWarehouseId,sku_code sku,batch_no batchNo,requested_qty requestedQty,outbound_qty outboundQty,received_qty receivedQty,operation_status status,version from wms_transfer_operation where transfer_no=#{transferNo}")
    Row find(String transferNo);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code Row}
     */
    @Insert("insert into wms_transfer_operation(operation_id,transfer_no,owner_id,source_warehouse_id,target_warehouse_id,sku_code,batch_no,requested_qty,outbound_qty,received_qty,operation_status,version,created_at,updated_at) values(#{id},#{transferNo},#{ownerId},#{sourceWarehouseId},#{targetWarehouseId},#{sku},#{batchNo},#{requestedQty},#{outboundQty},#{receivedQty},#{status},#{version},now(3),now(3))")
    void insert(Row row);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param outboundQty 数量值，类型为 {@code BigDecimal}
     * @param receivedQty 数量值，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("update wms_transfer_operation set outbound_qty=#{outboundQty},received_qty=#{receivedQty},operation_status=#{status},version=#{version},updated_at=now(3) where operation_id=#{id} and version=#{oldVersion}")
    int update(@Param("id") long id, @Param("outboundQty") BigDecimal outboundQty, @Param("receivedQty") BigDecimal receivedQty, @Param("status") int status, @Param("version") int version, @Param("oldVersion") int oldVersion);

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long id, String transferNo, long ownerId, long sourceWarehouseId, long targetWarehouseId, String sku, String batchNo, BigDecimal requestedQty, BigDecimal outboundQty, BigDecimal receivedQty, int status, int version) {
    }
}
