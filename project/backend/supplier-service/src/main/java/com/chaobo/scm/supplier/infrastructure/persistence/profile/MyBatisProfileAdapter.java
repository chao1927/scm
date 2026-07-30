package com.chaobo.scm.supplier.infrastructure.persistence.profile;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.application.profile.*;
import com.chaobo.scm.supplier.domain.profile.*;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Optional;

/**
 * MyBatisProfileAdapter。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisProfileAdapter implements ProfileChangeRepository, ProfileReadModelPort {

    /**
     * mapper（类型：{@code ProfilePersistenceMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final ProfilePersistenceMapper mapper;

    /**
     * json（类型：{@code ObjectMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ObjectMapper json;

    /**
     * 创建 MyBatisProfileAdapter。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code ProfilePersistenceMapper}
     * @param json 业务处理参数或成员，类型为 {@code ObjectMapper}
     */
    public MyBatisProfileAdapter(ProfilePersistenceMapper mapper, ObjectMapper json) {
        this.mapper = mapper;
        this.json = json;
    }

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<ProfileChangeAggregate>}
     */
    @Override
    public Optional<ProfileChangeAggregate> findById(long id) {
        var row = mapper.findChange(id);
        if (row == null) {
            return Optional.empty();
        }
        return Optional.of(ProfileChangeAggregate.rehydrate(row.changeId(), row.changeNo(), row.supplierId(), row.profileVersion(), row.changeReason(), fields(row.changedFieldsJson()), ProfileChangeStatus.fromCode(row.changeStatus()), row.withdrawReason(), row.version()));
    }

    /**
     * 查询并返回 {@code existsPending}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    @Override
    public boolean existsPending(long supplierId) {
        return mapper.existsPending(supplierId);
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param aggregate 业务处理参数或成员，类型为 {@code ProfileChangeAggregate}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Override
    public void save(ProfileChangeAggregate aggregate, long operatorId) {
        var row = row(aggregate);
        if (mapper.findChange(aggregate.changeId()) == null) {
            mapper.insert(row, operatorId);
        } else if (mapper.update(row, aggregate.version() - 1, operatorId) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "资料变更已被更新");
        }
    }

    /**
     * 查询并返回 {@code findProfile}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<ProfileViews.Profile>}
     */
    @Override
    public Optional<ProfileViews.Profile> findProfile(long supplierId) {
        var row = mapper.findProfile(supplierId);
        if (row == null) {
            return Optional.empty();
        }
        return Optional.of(new ProfileViews.Profile(row.supplierId(), row.supplierCode(), row.supplierName(), row.lifecycleStatus(), row.riskLevel(), row.profileJson(), row.version(), row.updatedAt()));
    }

    /**
     * 处理当前类型职责中的操作 {@code pageChanges}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<ProfileViews.Change>}
     */
    @Override
    public PageResult<ProfileViews.Change> pageChanges(long supplierId, Integer status, int pageNo, int pageSize) {
        long total = mapper.countChanges(supplierId, status);
        var rows = mapper.pageChanges(supplierId, status, (pageNo - 1) * pageSize, pageSize).stream().map(this::view).toList();
        return new PageResult<>(pageNo, pageSize, total, rows);
    }

    /**
     * 处理当前类型职责中的操作 {@code row}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code ProfileChangeAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ProfilePersistenceMapper.ChangeRow}
     */
    private ProfilePersistenceMapper.ChangeRow row(ProfileChangeAggregate a) {
        return new ProfilePersistenceMapper.ChangeRow(a.changeId(), a.changeNo(), a.supplierId(), a.profileVersion(), a.reason(), json.writeValueAsString(a.changes()), a.status().code(), a.withdrawReason(), a.version(), null);
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code ProfilePersistenceMapper.ChangeRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ProfileViews.Change}
     */
    private ProfileViews.Change view(ProfilePersistenceMapper.ChangeRow row) {
        var status = ProfileChangeStatus.fromCode(row.changeStatus());
        return new ProfileViews.Change(row.changeId(), row.changeNo(), row.supplierId(), status.code(), status.label(), row.changeReason(), row.withdrawReason(), row.profileVersion(), row.version(), row.createdAt(), fields(row.changedFieldsJson()));
    }

    /**
     * 处理当前类型职责中的操作 {@code fields}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code java.util.List<ProfileFieldChange>}
     */
    private java.util.List<ProfileFieldChange> fields(String value) {
        try {
            return Arrays.asList(json.readValue(value, ProfileFieldChange[].class));
        } catch (RuntimeException exception) {
            throw new IllegalStateException("资料变更字段解析失败", exception);
        }
    }
}
