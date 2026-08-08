package com.chaobo.scm.supplier.infrastructure.persistence.score;

import com.chaobo.scm.supplier.application.score.ScoreViews;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

/**
 * SupplierScoreMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierScoreMapper {

    /**
     * 处理当前类型职责中的操作 {@code insertFact}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param dimension 业务处理参数或成员，类型为 {@code String}
     * @param metric 业务处理参数或成员，类型为 {@code String}
     * @param value 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param occurredAt 业务时间，类型为 {@code OffsetDateTime}
     * @param source 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Insert("INSERT INTO sup_performance_fact(fact_id,event_code,supplier_id,dimension_code,metric_code,metric_value,occurred_at,source_system,source_no,payload_json) VALUES(#{id},#{eventCode},#{supplierId},#{dimension},#{metric},#{value},#{occurredAt},#{source},#{sourceNo},CAST(#{payload} AS JSON)) ON DUPLICATE KEY UPDATE event_code=event_code")
    int insertFact(@Param("id") long id, @Param("eventCode") String eventCode, @Param("supplierId") long supplierId, @Param("dimension") String dimension, @Param("metric") String metric, @Param("value") BigDecimal value, @Param("occurredAt") OffsetDateTime occurredAt, @Param("source") String source, @Param("sourceNo") String sourceNo, @Param("payload") String payload);

    /**
     * 处理当前类型职责中的操作 {@code facts}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param from 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param to 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<ScoreViews.Fact>}
     */
    @Select("SELECT fact_id id,event_code eventCode,supplier_id supplierId,dimension_code dimensionCode,metric_code metricCode,metric_value metricValue,occurred_at occurredAt,source_system sourceSystem,source_no sourceNo FROM sup_performance_fact WHERE supplier_id=#{supplierId} AND occurred_at>=#{from} AND occurred_at<#{to} ORDER BY occurred_at")
    List<ScoreViews.Fact> facts(@Param("supplierId") long supplierId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    /**
     * 处理当前类型职责中的操作 {@code suppliersWithFacts}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param from 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param to 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Long>}
     */
    @Select("SELECT DISTINCT supplier_id FROM sup_performance_fact WHERE occurred_at>=#{from} AND occurred_at<#{to}")
    List<Long> suppliersWithFacts(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    /**
     * 处理当前类型职责中的操作 {@code insertRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param dimension 业务处理参数或成员，类型为 {@code String}
     * @param metric 业务处理参数或成员，类型为 {@code String}
     * @param weight 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param target 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param direction 业务处理参数或成员，类型为 {@code int}
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_score_rule(rule_id,rule_name,dimension_code,metric_code,weight,target_value,score_direction,status,effective_from,effective_to,created_by,updated_by,version) VALUES(#{id},#{name},#{dimension},#{metric},#{weight},#{target},#{direction},1,#{from},#{to},#{operator},#{operator},0)")
    void insertRule(@Param("id") long id, @Param("name") String name, @Param("dimension") String dimension, @Param("metric") String metric, @Param("weight") BigDecimal weight, @Param("target") BigDecimal target, @Param("direction") int direction, @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("operator") long operator);

    /**
     * 执行命令 {@code publishRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_score_rule SET status=2,updated_by=#{operator},version=version+1 WHERE rule_id=#{id} AND version=#{version} AND status=1")
    int publishRule(@Param("id") long id, @Param("version") int version, @Param("operator") long operator);

    /**
     * 执行命令 {@code disableRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_score_rule SET status=3,updated_by=#{operator},version=version+1 WHERE rule_id=#{id} AND version=#{version} AND status=2")
    int disableRule(@Param("id") long id, @Param("version") int version, @Param("operator") long operator);

    /**
     * 处理当前类型职责中的操作 {@code effectiveRules}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param date 业务时间，类型为 {@code LocalDate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<ScoreViews.Rule>}
     */
    @Select("SELECT rule_id id,rule_name name,dimension_code dimensionCode,metric_code metricCode,weight,target_value targetValue,score_direction direction,status,effective_from effectiveFrom,effective_to effectiveTo,version FROM sup_score_rule WHERE status=2 AND effective_from<=#{date} AND (effective_to IS NULL OR effective_to>=#{date}) ORDER BY rule_id")
    List<ScoreViews.Rule> effectiveRules(LocalDate date);

    /**
     * 处理当前类型职责中的操作 {@code rules}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<ScoreViews.Rule>}
     */
    @Select("SELECT rule_id id,rule_name name,dimension_code dimensionCode,metric_code metricCode,weight,target_value targetValue,score_direction direction,status,effective_from effectiveFrom,effective_to effectiveTo,version FROM sup_score_rule ORDER BY updated_at DESC")
    List<ScoreViews.Rule> rules();

    /**
     * 处理当前类型职责中的操作 {@code upsertResult}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param period 业务处理参数或成员，类型为 {@code String}
     * @param total 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param dimensions 业务处理参数或成员，类型为 {@code String}
     * @param summary 业务处理参数或成员，类型为 {@code String}
     * @param adjustment 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    @Insert("INSERT INTO sup_score_result(score_result_id,supplier_id,period_code,total_score,dimension_scores_json,fact_summary_json,manual_adjustment,adjustment_reason,status,version) VALUES(#{id},#{supplierId},#{period},#{total},CAST(#{dimensions} AS JSON),CAST(#{summary} AS JSON),#{adjustment},#{reason},1,0) ON DUPLICATE KEY UPDATE total_score=IF(status=1,VALUES(total_score),total_score),dimension_scores_json=IF(status=1,VALUES(dimension_scores_json),dimension_scores_json),fact_summary_json=IF(status=1,VALUES(fact_summary_json),fact_summary_json),manual_adjustment=IF(status=1,VALUES(manual_adjustment),manual_adjustment),adjustment_reason=IF(status=1,VALUES(adjustment_reason),adjustment_reason),version=IF(status=1,version+1,version)")
    void upsertResult(@Param("id") long id, @Param("supplierId") long supplierId, @Param("period") String period, @Param("total") BigDecimal total, @Param("dimensions") String dimensions, @Param("summary") String summary, @Param("adjustment") BigDecimal adjustment, @Param("reason") String reason);

    /**
     * 处理当前类型职责中的操作 {@code resultByPeriod}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param period 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ScoreViews.Result}
     */
    @Select("SELECT score_result_id id,supplier_id supplierId,period_code periodCode,total_score totalScore,dimension_scores_json dimensionScoresJson,fact_summary_json factSummaryJson,manual_adjustment manualAdjustment,adjustment_reason adjustmentReason,status,published_at publishedAt,version FROM sup_score_result WHERE supplier_id=#{supplierId} AND period_code=#{period}")
    ScoreViews.Result resultByPeriod(@Param("supplierId") long supplierId, @Param("period") String period);

    /**
     * 处理当前类型职责中的操作 {@code result}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ScoreViews.Result}
     */
    @Select("SELECT score_result_id id,supplier_id supplierId,period_code periodCode,total_score totalScore,dimension_scores_json dimensionScoresJson,fact_summary_json factSummaryJson,manual_adjustment manualAdjustment,adjustment_reason adjustmentReason,status,published_at publishedAt,version FROM sup_score_result WHERE score_result_id=#{id}")
    ScoreViews.Result result(long id);

    @Select("<script>SELECT score_result_id id,supplier_id supplierId,period_code periodCode,total_score totalScore,dimension_scores_json dimensionScoresJson,fact_summary_json factSummaryJson,manual_adjustment manualAdjustment,adjustment_reason adjustmentReason,status,published_at publishedAt,version FROM sup_score_result <if test='supplierId!=null'>WHERE supplier_id=#{supplierId}</if> ORDER BY period_code DESC,supplier_id LIMIT #{limit}</script>")
    List<ScoreViews.Result> results(@Param("supplierId") Long supplierId, @Param("limit") int limit);

    /**
     * 执行命令 {@code publishResult}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_score_result SET status=2,published_at=NOW(3),version=version+1 WHERE score_result_id=#{id} AND version=#{version} AND status=1")
    int publishResult(@Param("id") long id, @Param("version") int version);

    /**
     * 处理当前类型职责中的操作 {@code insertRisk}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param resultId 业务或技术标识，类型为 {@code long}
     * @param level 业务处理参数或成员，类型为 {@code int}
     * @param type 业务处理参数或成员，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    @Insert("INSERT INTO sup_risk_recommendation(recommendation_id,supplier_id,score_result_id,risk_level,recommendation_type,reason,status,version) VALUES(#{id},#{supplierId},#{resultId},#{level},#{type},#{reason},1,0)")
    void insertRisk(@Param("id") long id, @Param("supplierId") long supplierId, @Param("resultId") long resultId, @Param("level") int level, @Param("type") int type, @Param("reason") String reason);

    /**
     * 处理当前类型职责中的操作 {@code risks}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<ScoreViews.Risk>}
     */
    @Select("SELECT recommendation_id id,supplier_id supplierId,score_result_id scoreResultId,risk_level riskLevel,recommendation_type recommendationType,reason,status,version FROM sup_risk_recommendation WHERE supplier_id=#{supplierId} ORDER BY created_at DESC")
    List<ScoreViews.Risk> risks(long supplierId);

    /**
     * 处理当前类型职责中的操作 {@code processRisk}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param status 生命周期状态，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_risk_recommendation SET status=#{status},version=version+1 WHERE recommendation_id=#{id} AND version=#{version} AND status=1")
    int processRisk(@Param("id") long id, @Param("version") int version, @Param("status") int status);
}
