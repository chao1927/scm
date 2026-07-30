package com.chaobo.scm.supplier.infrastructure.persistence.quality;

import org.apache.ibatis.annotations.*;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * SupplierQualityEvidenceMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierQualityEvidenceMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long id, long issueId, String type, String url, String content, long createdBy, OffsetDateTime createdAt) {
    }

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param issueId 业务或技术标识，类型为 {@code long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param url 业务处理参数或成员，类型为 {@code String}
     * @param content 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_quality_issue_evidence(evidence_id,quality_issue_id,evidence_type,attachment_url,content,created_by) VALUES(#{id},#{issueId},#{type},#{url},#{content},#{operator})")
    void insert(@Param("id") long id, @Param("issueId") long issueId, @Param("type") String type, @Param("url") String url, @Param("content") String content, @Param("operator") long operator);

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param issueId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code List<Row>}
     */
    @Select("SELECT evidence_id id,quality_issue_id issueId,evidence_type type,attachment_url url,content,created_by createdBy,created_at createdAt FROM sup_quality_issue_evidence WHERE quality_issue_id=#{issueId} ORDER BY created_at,evidence_id")
    List<Row> list(long issueId);
}
