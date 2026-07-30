package com.chaobo.scm.supplier.infrastructure.persistence.contract;

import org.apache.ibatis.annotations.*;
import java.time.*;

/**
 * SupplierContractMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierContractMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long id, String no, long supplier, Long quote, String agreement, String type, LocalDate from, LocalDate to, int status, String terms, String attachment, String reason, int version) {
    }

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("SELECT contract_id id,contract_no no,supplier_id supplier,quote_id quote,price_agreement_ref agreement,contract_type type,effective_from `from`,effective_to `to`,contract_status status,terms_json terms,attachment_url attachment,termination_reason reason,version FROM sup_supplier_contract WHERE contract_id=#{id} AND deleted=0")
    Row find(long id);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Row}
     * @param op 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_supplier_contract(contract_id,contract_no,supplier_id,quote_id,price_agreement_ref,contract_type,effective_from,effective_to,contract_status,terms_json,attachment_url,termination_reason,created_by,updated_by,version,deleted) VALUES(#{r.id},#{r.no},#{r.supplier},#{r.quote},#{r.agreement},#{r.type},#{r.from},#{r.to},#{r.status},CAST(#{r.terms} AS JSON),#{r.attachment},#{r.reason},#{op},#{op},#{r.version},0)")
    void insert(@Param("r") Row r, @Param("op") long op);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Row}
     * @param expected 业务处理参数或成员，类型为 {@code int}
     * @param op 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_supplier_contract SET effective_to=#{r.to},contract_status=#{r.status},terms_json=CAST(#{r.terms} AS JSON),attachment_url=#{r.attachment},termination_reason=#{r.reason},updated_by=#{op},version=#{r.version} WHERE contract_id=#{r.id} AND version=#{expected} AND deleted=0")
    int update(@Param("r") Row r, @Param("expected") int expected, @Param("op") long op);

    /**
     * 查询并返回 {@code count}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    @Select("<script>SELECT COUNT(*) FROM sup_supplier_contract WHERE deleted=0 <if test='supplierId!=null'>AND supplier_id=#{supplierId}</if><if test='status!=null'>AND contract_status=#{status}</if><if test='keyword!=null and keyword!=\"\"'>AND contract_no LIKE CONCAT('%',#{keyword},'%')</if></script>")
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
     * @return 处理当前类型职责中的操作的结果，类型为 {@code java.util.List<Row>}
     */
    @Select("<script>SELECT contract_id id,contract_no no,supplier_id supplier,quote_id quote,price_agreement_ref agreement,contract_type type,effective_from `from`,effective_to `to`,contract_status status,terms_json terms,attachment_url attachment,termination_reason reason,version FROM sup_supplier_contract WHERE deleted=0 <if test='supplierId!=null'>AND supplier_id=#{supplierId}</if><if test='status!=null'>AND contract_status=#{status}</if><if test='keyword!=null and keyword!=\"\"'>AND contract_no LIKE CONCAT('%',#{keyword},'%')</if> ORDER BY updated_at DESC LIMIT #{offset},#{size}</script>")
    java.util.List<Row> page(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);
}
