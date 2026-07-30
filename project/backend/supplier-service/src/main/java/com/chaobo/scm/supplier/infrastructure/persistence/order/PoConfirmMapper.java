package com.chaobo.scm.supplier.infrastructure.persistence.order;

import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

/**
 * PoConfirmMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface PoConfirmMapper {

    /**
     * Head。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Head(long orderId, String confirmNo, long purchaseOrderId, String purchaseOrderNo, long supplierId, int confirmStatus, OffsetDateTime confirmDeadline, OffsetDateTime confirmedAt, Integer diffType, Integer reasonCode, String remark, int sourceVersion, int version, OffsetDateTime updatedAt) {
    }

    /**
     * Line。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Line(long orderLineId, long orderId, String skuCode, BigDecimal orderQty, BigDecimal confirmedQty, LocalDate requestedDeliveryDate, LocalDate confirmedDeliveryDate, int lineStatus, String diffReason) {
    }

    /**
     * HEAD（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    String HEAD = "SELECT order_id,confirm_no,purchase_order_id,purchase_order_no,supplier_id,confirm_status,confirm_deadline,confirmed_at,diff_type,reason_code,remark,source_version,version,updated_at FROM sup_order ";

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Head}
     */
    @Select(HEAD + "WHERE order_id=#{id} AND deleted=0")
    Head find(long id);

    /**
     * 查询并返回 {@code findByPo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Head}
     */
    @Select(HEAD + "WHERE purchase_order_id=#{id} AND deleted=0")
    Head findByPo(long id);

    /**
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Line>}
     */
    @Select("SELECT order_line_id,order_id,sku_code,order_qty,confirmed_qty,requested_delivery_date,confirmed_delivery_date,line_status,diff_reason FROM sup_order_line WHERE order_id=#{id} AND deleted=0 ORDER BY order_line_id")
    List<Line> lines(long id);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Head}
     * @param op 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_order(order_id,confirm_no,purchase_order_id,purchase_order_no,supplier_id,confirm_status,confirm_deadline,confirmed_at,diff_type,reason_code,remark,source_version,created_by,updated_by,version,deleted)VALUES(#{r.orderId},#{r.confirmNo},#{r.purchaseOrderId},#{r.purchaseOrderNo},#{r.supplierId},#{r.confirmStatus},#{r.confirmDeadline},#{r.confirmedAt},#{r.diffType},#{r.reasonCode},#{r.remark},#{r.sourceVersion},#{op},#{op},#{r.version},0)")
    void insert(@Param("r") Head r, @Param("op") long op);

    /**
     * 处理当前类型职责中的操作 {@code insertLine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Line}
     * @param op 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_order_line(order_line_id,order_id,sku_code,order_qty,confirmed_qty,requested_delivery_date,confirmed_delivery_date,line_status,diff_reason,created_by,updated_by,version,deleted)VALUES(#{r.orderLineId},#{r.orderId},#{r.skuCode},#{r.orderQty},#{r.confirmedQty},#{r.requestedDeliveryDate},#{r.confirmedDeliveryDate},#{r.lineStatus},#{r.diffReason},#{op},#{op},0,0)")
    void insertLine(@Param("r") Line r, @Param("op") long op);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Head}
     * @param expected 业务处理参数或成员，类型为 {@code int}
     * @param op 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_order SET confirm_status=#{r.confirmStatus},confirm_deadline=#{r.confirmDeadline},confirmed_at=#{r.confirmedAt},diff_type=#{r.diffType},reason_code=#{r.reasonCode},remark=#{r.remark},source_version=#{r.sourceVersion},updated_by=#{op},version=#{r.version} WHERE order_id=#{r.orderId} AND version=#{expected} AND deleted=0")
    int update(@Param("r") Head r, @Param("expected") int expected, @Param("op") long op);

    /**
     * 执行命令 {@code updateLine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Line}
     * @param op 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_order_line SET order_qty=#{r.orderQty},confirmed_qty=#{r.confirmedQty},requested_delivery_date=#{r.requestedDeliveryDate},confirmed_delivery_date=#{r.confirmedDeliveryDate},line_status=#{r.lineStatus},diff_reason=#{r.diffReason},updated_by=#{op},version=version+1,deleted=0 WHERE order_line_id=#{r.orderLineId} AND order_id=#{r.orderId}")
    int updateLine(@Param("r") Line r, @Param("op") long op);

    /**
     * 执行命令 {@code deleteMissingLines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderId 业务或技术标识，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code List<Long>}
     * @param op 业务处理参数或成员，类型为 {@code long}
     */
    @Update("<script>UPDATE sup_order_line SET deleted=1,updated_by=#{op},version=version+1 WHERE order_id=#{orderId} AND deleted=0 <if test='ids!=null and !ids.isEmpty()'>AND order_line_id NOT IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></if></script>")
    void deleteMissingLines(@Param("orderId") long orderId, @Param("ids") List<Long> ids, @Param("op") long op);

    /**
     * 查询并返回 {@code count}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    @Select("<script>SELECT COUNT(*) FROM sup_order WHERE deleted=0 <if test='supplierId != null'>AND supplier_id=#{supplierId}</if><if test='status != null'>AND confirm_status=#{status}</if><if test='keyword != null and keyword != \"\"'>AND purchase_order_no LIKE CONCAT('%',#{keyword},'%')</if></script>")
    long count(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("keyword") String keyword);

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Head>}
     */
    @Select("<script>" + HEAD + "WHERE deleted=0 <if test='supplierId != null'>AND supplier_id=#{supplierId}</if><if test='status != null'>AND confirm_status=#{status}</if><if test='keyword != null and keyword != \"\"'>AND purchase_order_no LIKE CONCAT('%',#{keyword},'%')</if> ORDER BY updated_at DESC LIMIT #{offset},#{size}</script>")
    List<Head> page(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);
}
