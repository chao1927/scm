package com.chaobo.scm.supplier.infrastructure.persistence.item;

import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

/**
 * SupplierItemMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierItemMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long supplierItemId, long supplierId, String skuCode, String supplierSkuCode, BigDecimal moq, BigDecimal mpq, int leadTimeDays, String purchaseUnit, LocalDate effectiveFrom, LocalDate effectiveTo, int supplyStatus, String pauseReason, int version, OffsetDateTime updatedAt) {
    }

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("SELECT supplier_item_id,supplier_id,sku_code,supplier_sku_code,moq,mpq,lead_time_days,purchase_unit,effective_from,effective_to,supply_status,pause_reason,version,updated_at FROM sup_supplier_item WHERE supplier_item_id=#{id} AND deleted=0")
    Row find(long id);

    /**
     * 处理当前类型职责中的操作 {@code availableBySupplier}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Row>}
     */
    @Select("SELECT supplier_item_id,supplier_id,sku_code,supplier_sku_code,moq,mpq,lead_time_days,purchase_unit,effective_from,effective_to,supply_status,pause_reason,version,updated_at FROM sup_supplier_item WHERE supplier_id=#{supplierId} AND supply_status=1 AND deleted=0")
    List<Row> availableBySupplier(long supplierId);

    /**
     * 处理当前类型职责中的操作 {@code availableBySku}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Row>}
     */
    @Select("SELECT supplier_item_id,supplier_id,sku_code,supplier_sku_code,moq,mpq,lead_time_days,purchase_unit,effective_from,effective_to,supply_status,pause_reason,version,updated_at FROM sup_supplier_item WHERE sku_code=#{skuCode} AND supply_status=1 AND deleted=0")
    List<Row> availableBySku(String skuCode);

    /**
     * 查询并返回 {@code exists}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    @Select("SELECT COUNT(*) FROM sup_supplier_item WHERE supplier_id=#{supplierId} AND sku_code=#{sku} AND deleted=0")
    boolean exists(@Param("supplierId") long supplierId, @Param("sku") String sku);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Row}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_supplier_item(supplier_item_id,supplier_id,sku_code,supplier_sku_code,moq,mpq,lead_time_days,purchase_unit,effective_from,effective_to,supply_status,pause_reason,created_by,updated_by,version,deleted) VALUES(#{r.supplierItemId},#{r.supplierId},#{r.skuCode},#{r.supplierSkuCode},#{r.moq},#{r.mpq},#{r.leadTimeDays},#{r.purchaseUnit},#{r.effectiveFrom},#{r.effectiveTo},#{r.supplyStatus},#{r.pauseReason},#{operator},#{operator},#{r.version},0)")
    void insert(@Param("r") Row r, @Param("operator") long operator);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Row}
     * @param expected 业务处理参数或成员，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_supplier_item SET supplier_sku_code=#{r.supplierSkuCode},moq=#{r.moq},mpq=#{r.mpq},lead_time_days=#{r.leadTimeDays},purchase_unit=#{r.purchaseUnit},effective_from=#{r.effectiveFrom},effective_to=#{r.effectiveTo},supply_status=#{r.supplyStatus},pause_reason=#{r.pauseReason},updated_by=#{operator},version=#{r.version} WHERE supplier_item_id=#{r.supplierItemId} AND version=#{expected} AND deleted=0")
    int update(@Param("r") Row r, @Param("expected") int expected, @Param("operator") long operator);

    /**
     * 查询并返回 {@code count}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    @Select("<script>SELECT COUNT(*) FROM sup_supplier_item WHERE deleted=0 <if test='supplierId != null'>AND supplier_id=#{supplierId}</if><if test='status != null'>AND supply_status=#{status}</if><if test='keyword != null and keyword != \"\"'>AND (sku_code LIKE CONCAT('%',#{keyword},'%') OR supplier_sku_code LIKE CONCAT('%',#{keyword},'%'))</if></script>")
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
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Row>}
     */
    @Select("<script>SELECT supplier_item_id,supplier_id,sku_code,supplier_sku_code,moq,mpq,lead_time_days,purchase_unit,effective_from,effective_to,supply_status,pause_reason,version,updated_at FROM sup_supplier_item WHERE deleted=0 <if test='supplierId != null'>AND supplier_id=#{supplierId}</if><if test='status != null'>AND supply_status=#{status}</if><if test='keyword != null and keyword != \"\"'>AND (sku_code LIKE CONCAT('%',#{keyword},'%') OR supplier_sku_code LIKE CONCAT('%',#{keyword},'%'))</if> ORDER BY updated_at DESC LIMIT #{offset},#{size}</script>")
    List<Row> page(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);
}
