package com.chaobo.scm.supplier.application.account;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.common.integration.IamCollaborationApi;
import com.chaobo.scm.supplier.application.integration.IntegrationCommandEnqueuer;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import com.chaobo.scm.supplier.infrastructure.persistence.account.SupplierAccessMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * SupplierAccessApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierAccessApplicationService {

    /**
     * mapper（类型：{@code SupplierAccessMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierAccessMapper mapper;

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids;

    /**
     * audit（类型：{@code AuditLogRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AuditLogRepository audit;

    /**
     * executor（类型：{@code TransactionalCommandExecutor}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final TransactionalCommandExecutor executor;

    /**
     * integrations（类型：{@code IntegrationCommandEnqueuer}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final IntegrationCommandEnqueuer integrations;

    /**
     * 创建 SupplierAccessApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code SupplierAccessMapper}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param executor 业务处理参数或成员，类型为 {@code TransactionalCommandExecutor}
     * @param integrations 业务处理参数或成员，类型为 {@code IntegrationCommandEnqueuer}
     */
    public SupplierAccessApplicationService(SupplierAccessMapper mapper, IdentifierGenerator ids, AuditLogRepository audit, TransactionalCommandExecutor executor, IntegrationCommandEnqueuer integrations) {
        this.mapper = mapper;
        this.ids = ids;
        this.audit = audit;
        this.executor = executor;
        this.integrations = integrations;
    }

    /**
     * 处理当前类型职责中的操作 {@code contacts}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<SupplierContactView>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<SupplierContactView> contacts(long supplierId, CommandContext context) {
        context.requirePermission("supplier:contact:view");
        context.requireSupplierScope(supplierId);
        return mapper.contacts(supplierId).stream().map(row -> new SupplierContactView(row.id(), row.supplierId(), row.name(), row.mobile(), row.email(), row.role(), row.primary(), row.status(), row.version())).toList();
    }

    /**
     * 处理当前类型职责中的操作 {@code contact}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierContactView}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SupplierContactView contact(long id, CommandContext context) {
        var row = mapper.contact(id);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "联系人不存在");
        }
        context.requirePermission("supplier:contact:view");
        context.requireSupplierScope(row.supplierId());
        return new SupplierContactView(row.id(), row.supplierId(), row.name(), row.mobile(), row.email(), row.role(), row.primary(), row.status(), row.version());
    }

    /**
     * 执行命令 {@code bindings}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code List<SupplierUserBindingView>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<SupplierUserBindingView> bindings(long supplierId, CommandContext context) {
        context.requirePermission("supplier:account:view");
        context.requireSupplierScope(supplierId);
        return mapper.bindings(supplierId).stream().map(row -> new SupplierUserBindingView(row.id(), row.supplierId(), row.userId(), row.role(), row.primary(), row.status(), row.boundAt(), row.version())).toList();
    }

    /** 查询当前数据范围内的供应商账号绑定。 */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<SupplierUserBindingView> bindings(CommandContext context) {
        context.requirePermission("supplier:account:view");
        return mapper.allBindings(context.supplierScopeId()).stream()
                .map(row -> new SupplierUserBindingView(row.id(), row.supplierId(), row.userId(), row.role(),
                        row.primary(), row.status(), row.boundAt(), row.version()))
                .toList();
    }

    /**
     * 执行命令 {@code saveContact}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code Long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param mobile 业务处理参数或成员，类型为 {@code String}
     * @param email 业务处理参数或成员，类型为 {@code String}
     * @param role 业务处理参数或成员，类型为 {@code String}
     * @param primary 业务处理参数或成员，类型为 {@code boolean}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult saveContact(Long id, long supplierId, String name, String mobile, String email, String role, boolean primary, int version, CommandContext context) {
        context.requirePermission("supplier:contact:manage");
        context.requireSupplierScope(supplierId);
        return executor.execute("supplier:contact", context, supplierId + name + mobile, () -> {
            if (primary) {
                mapper.clearPrimaryContact(supplierId);
            }
            if (id == null) {
                long newId = ids.nextId();
                mapper.insertContact(new SupplierAccessMapper.ContactRow(newId, supplierId, name, mobile, email, role, primary, 1, 0), context.operatorId());
                audit.save(context, "CREATE_CONTACT", "SUPPLIER_CONTACT", newId, Long.toString(newId), null, "{\"primary\":" + primary + "}");
                return new CommandResult(newId, Long.toString(newId), 1, "启用", 0, null, false);
            }
            var old = mapper.contact(id);
            if (old == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "联系人不存在");
            }
            if (old.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "联系人已更新");
            }
            if (mapper.updateContact(new SupplierAccessMapper.ContactRow(id, supplierId, name, mobile, email, role, primary, 1, version), version, context.operatorId()) != 1) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "联系人已更新");
            }
            audit.save(context, "UPDATE_CONTACT", "SUPPLIER_CONTACT", id, Long.toString(id), null, "{\"primary\":" + primary + "}");
            return new CommandResult(id, Long.toString(id), 1, "启用", version + 1, null, false);
        });
    }

    /**
     * 执行命令 {@code bind}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param userId 业务或技术标识，类型为 {@code long}
     * @param role 业务处理参数或成员，类型为 {@code String}
     * @param primary 业务处理参数或成员，类型为 {@code boolean}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult bind(long supplierId, long userId, String role, boolean primary, CommandContext context) {
        context.requirePermission("supplier:account:bind");
        context.requireSupplierScope(supplierId);
        return executor.execute("supplier:binding", context, supplierId + ":" + userId, () -> {
            if (mapper.activeBinding(supplierId, userId)) {
                throw new BusinessException(ErrorCode.STATE_CONFLICT, "用户已绑定该供应商");
            }
            long id = ids.nextId();
            mapper.insertBinding(new SupplierAccessMapper.BindingRow(id, supplierId, userId, role, primary, 1, null, 0), context.operatorId());
            syncScope(userId, id, 0, "绑定供应商账号");
            audit.save(context, "BIND_SUPPLIER_USER", "SUPPLIER_USER_BINDING", id, Long.toString(id), null, "{\"userId\":" + userId + "}");
            return new CommandResult(id, Long.toString(id), 1, "启用", 0, null, false);
        });
    }

    /**
     * 执行命令 {@code unbind}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult unbind(long id, int version, CommandContext context) {
        context.requirePermission("supplier:account:bind");
        return executor.execute("supplier:binding", context, id + ":" + version, () -> {
            var binding = mapper.binding(id);
            if (binding == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "供应商用户绑定不存在");
            }
            context.requireSupplierScope(binding.supplierId());
            if (binding.version() != version || mapper.disableBinding(id, version) != 1) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "绑定已更新");
            }
            syncScope(binding.userId(), id, version + 1, "解除供应商账号");
            audit.save(context, "UNBIND_SUPPLIER_USER", "SUPPLIER_USER_BINDING", id, Long.toString(id), null, "{\"status\":2}");
            return new CommandResult(id, Long.toString(id), 2, "停用", version + 1, null, false);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code syncScope}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param userId 业务或技术标识，类型为 {@code long}
     * @param bindingId 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    private void syncScope(long userId, long bindingId, int version, String reason) {
        integrations.enqueue("IAM_UPDATE_SUPPLIER_SCOPE", "SUPPLIER_USER_BINDING", bindingId, version, "IAM", new IamCollaborationApi.UpdateSupplierScopeCommand("IAM-SCOPE-" + bindingId + "-" + version, userId, mapper.activeSupplierIds(userId), reason));
    }
}
