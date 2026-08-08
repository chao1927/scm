package com.chaobo.scm.iam.application;

import com.chaobo.scm.iam.domain.IamApplicationAggregate;
import com.chaobo.scm.iam.domain.IamSsoClientAggregate;
import com.chaobo.scm.iam.infrastructure.persistence.IamAdminMapper;
import com.chaobo.scm.iam.infrastructure.persistence.IamPermissionOpenApiMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IamAdminApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class IamAdminApplicationService {

    /**
     * mapper（类型：{@code IamAdminMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final IamAdminMapper mapper;

    /**
     * eventIds（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong eventIds = new AtomicLong(System.currentTimeMillis());

    /**
     * 创建 IamAdminApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code IamAdminMapper}
     */
    public IamAdminApplicationService(IamAdminMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 执行命令 {@code createApp}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateAppCommand}
     * @return 执行命令的结果，类型为 {@code IamAdminMapper.AppRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public IamAdminMapper.AppRow createApp(CreateAppCommand command) {
        IamAdminMapper.AppRow existing = mapper.findApp(command.appCode());
        if (existing != null) {
            return existing;
        }
        IamApplicationAggregate aggregate = IamApplicationAggregate.create(command.appCode(), command.appName(), command.homeUrl());
        IamAdminMapper.AppRow row = toRow(aggregate);
        mapper.insertApp(row);
        outbox("IamAppCreated", row.appCode(), row.appName());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code changeApp}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ChangeAppCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code IamAdminMapper.AppRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public IamAdminMapper.AppRow changeApp(String appCode, ChangeAppCommand command) {
        IamApplicationAggregate aggregate = loadApp(appCode);
        aggregate.change(command.appName(), command.homeUrl(), command.expectedVersion());
        mapper.updateApp(toRow(aggregate));
        outbox("IamAppChanged", appCode, command.appName());
        return mapper.findApp(appCode);
    }

    /**
     * 转换数据模型 {@code toggleApp}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ToggleCommand}
     * @return 转换数据模型的结果，类型为 {@code IamAdminMapper.AppRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public IamAdminMapper.AppRow toggleApp(String appCode, ToggleCommand command) {
        IamApplicationAggregate aggregate = loadApp(appCode);
        if (command.enabled()) {
            aggregate.enable(command.expectedVersion());
            mapper.updateApp(toRow(aggregate));
            outbox("IamAppEnabled", appCode, command.reason());
        } else {
            aggregate.disable(command.expectedVersion());
            mapper.updateApp(toRow(aggregate));
            outbox("IamAppDisabled", appCode, command.reason());
            outbox("PermissionSnapshotInvalidated", appCode, "app disabled");
        }
        return mapper.findApp(appCode);
    }

    /**
     * 查询并返回 {@code listApps}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<IamAdminMapper.AppRow>}
     */
    public List<IamAdminMapper.AppRow> listApps() {
        return mapper.listApps();
    }

    /**
     * 执行命令 {@code createMenu}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateMenuCommand}
     * @return 执行命令的结果，类型为 {@code IamAdminMapper.MenuRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public IamAdminMapper.MenuRow createMenu(CreateMenuCommand command) {
        ensureApp(command.appCode());
        if (mapper.findMenu(command.menuCode()) != null) {
            throw new IllegalStateException("menu code already exists");
        }
        IamAdminMapper.MenuRow row = new IamAdminMapper.MenuRow(null, command.menuCode(), command.appCode(), command.parentCode(), command.menuName(), command.routePath(), command.sortNo(), 1, 1);
        mapper.insertMenu(row);
        outbox("IamMenuCreated", command.menuCode(), command.appCode());
        return row;
    }

    /**
     * 执行命令 {@code disableMenu}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param menuCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code DisableCommand}
     * @return 执行命令的结果，类型为 {@code IamAdminMapper.MenuRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public IamAdminMapper.MenuRow disableMenu(String menuCode, DisableCommand command) {
        IamAdminMapper.MenuRow row = mapper.findMenu(menuCode);
        if (row == null) {
            throw new IllegalArgumentException("menu not found");
        }
        if (row.version() != command.expectedVersion()) {
            throw new IllegalStateException("menu version conflict");
        }
        IamAdminMapper.MenuRow disabled = new IamAdminMapper.MenuRow(null, row.menuCode(), row.appCode(), row.parentCode(), row.menuName(), row.routePath(), row.sortNo(), 2, row.version() + 1);
        mapper.updateMenu(disabled);
        outbox("IamMenuDisabled", menuCode, command.reason());
        outbox("PermissionSnapshotInvalidated", row.appCode(), "menu disabled");
        return mapper.findMenu(menuCode);
    }

    /**
     * 查询并返回 {@code listMenus}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code List<IamAdminMapper.MenuRow>}
     */
    public List<IamAdminMapper.MenuRow> listMenus(String appCode) {
        return mapper.listMenus(appCode);
    }

    /**
     * 处理当前类型职责中的操作 {@code configureSso}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code ConfigureSsoCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SsoSecret}
     */
    @Transactional(rollbackFor = Exception.class)
    public SsoSecret configureSso(ConfigureSsoCommand command) {
        ensureApp(command.appCode());
        String plainSecret = "SSO-" + eventIds.incrementAndGet();
        IamSsoClientAggregate aggregate = IamSsoClientAggregate.configure(command.ssoCode(), command.appCode(), command.redirectUrl(), hash(plainSecret));
        mapper.insertSso(toRow(aggregate));
        outbox("SsoClientConfigured", command.ssoCode(), command.appCode());
        return new SsoSecret(command.ssoCode(), plainSecret);
    }

    /**
     * 处理当前类型职责中的操作 {@code resetSsoSecret}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ssoCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ResetSsoSecretCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SsoSecret}
     */
    @Transactional(rollbackFor = Exception.class)
    public SsoSecret resetSsoSecret(String ssoCode, ResetSsoSecretCommand command) {
        IamSsoClientAggregate aggregate = loadSso(ssoCode);
        String plainSecret = "SSO-" + eventIds.incrementAndGet();
        aggregate.resetSecret(hash(plainSecret), command.expectedVersion());
        mapper.updateSso(toRow(aggregate));
        outbox("SsoClientSecretReset", ssoCode, aggregate.appCode());
        return new SsoSecret(ssoCode, plainSecret);
    }

    /**
     * 查询并返回 {@code listSso}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<IamAdminMapper.SsoRow>}
     */
    public List<IamAdminMapper.SsoRow> listSso() {
        return mapper.listSso();
    }

    /**
     * 执行命令 {@code consumeEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code EventEnvelope}
     * @return 执行命令的结果，类型为 {@code ConsumeResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsumeResult consumeEvent(EventEnvelope event) {
        int claimed = mapper.claimEvent(new IamAdminMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessNo(), event.payload(), 1, null));
        if (claimed == 0) {
            return new ConsumeResult(event.eventId(), "DUPLICATE", true, "idempotent hit");
        }
        IamAdminMapper.EventBusinessProjectionRow projection = toBusinessProjection(event);
        if (projection != null) {
            mapper.upsertBusinessProjection(projection);
            mapper.updateEvent(new IamAdminMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessNo(), event.payload(), 2, null));
            outbox("IamExternalEventProjected", event.businessNo(), event.eventType());
            return new ConsumeResult(event.eventId(), "SUCCESS", false,
                "business projection updated");
        }
        mapper.updateEvent(new IamAdminMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessNo(), event.payload(), 4, "unsupported event type"));
        return new ConsumeResult(event.eventId(), "IGNORED", false, "unsupported event type");
    }

    /**
     * 把外部事实映射成 IAM 拥有的授权快照或处置待办。
     * Inbox 只解决投递幂等，本方法形成可被后续授权、激活和人工补偿流程使用的业务状态。
     */
    private static IamAdminMapper.EventBusinessProjectionRow toBusinessProjection(
            EventEnvelope event) {
        String projectionType;
        String status;
        switch (event.eventType()) {
            case "MasterDataChanged", MASTER_DATA_PUBLISHED,
                    "WarehouseEnabled" -> {
                projectionType = "AUTHORIZABLE_OBJECT";
                status = "ACTIVE";
            }
            case "LocationFrozen" -> {
                projectionType = "AUTHORIZABLE_OBJECT";
                status = "FROZEN";
            }
            case "SupplierEnabled" -> {
                projectionType = "EXTERNAL_SUBJECT";
                status = "ACTIVE";
            }
            case "SupplierFrozen" -> {
                projectionType = "EXTERNAL_SUBJECT";
                status = "RISK_RESTRICTED";
            }
            case "EmployeeOnboarded" -> {
                projectionType = "USER_LIFECYCLE";
                status = "PENDING_ACTIVATION";
            }
            case "EmployeeOffboarded" -> {
                projectionType = "USER_LIFECYCLE";
                status = "DISABLE_PENDING";
            }
            case "ApiResourceScanned", PERMISSION_RESOURCE_SCANNED -> {
                projectionType = "PERMISSION_SUGGESTION";
                status = "PENDING_CONFIRMATION";
            }
            case "SensitiveOperationOccurred", SECURITY_RISK_DETECTED -> {
                projectionType = "SECURITY_RISK";
                status = "OPEN";
            }
            case "ApprovalCallbackFailed" -> {
                projectionType = "APPROVAL_COMPENSATION";
                status = "PENDING_RETRY";
            }
            default -> {
                return null;
            }
        }
        return new IamAdminMapper.EventBusinessProjectionRow(
            projectionType, event.businessNo(), event.sourceSystem(), event.eventId(),
            event.eventType(), status, event.payload());
    }

    /**
     * 查询并返回 {@code listInbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<IamAdminMapper.EventInboxRow>}
     */
    public List<IamAdminMapper.EventInboxRow> listInbox() {
        return mapper.listInbox();
    }

    /**
     * 查询并返回 {@code loadApp}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code IamApplicationAggregate}
     */
    private IamApplicationAggregate loadApp(String appCode) {
        IamAdminMapper.AppRow row = mapper.findApp(appCode);
        if (row == null) {
            throw new IllegalArgumentException("application not found");
        }
        return IamApplicationAggregate.restore(row.appCode(), row.appName(), row.homeUrl(), row.status(), row.version());
    }

    /**
     * 查询并返回 {@code loadSso}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param ssoCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code IamSsoClientAggregate}
     */
    private IamSsoClientAggregate loadSso(String ssoCode) {
        IamAdminMapper.SsoRow row = mapper.findSso(ssoCode);
        if (row == null) {
            throw new IllegalArgumentException("sso client not found");
        }
        return IamSsoClientAggregate.restore(row.ssoCode(), row.appCode(), row.redirectUrl(), row.secretHash(), row.status(), row.version());
    }

    /**
     * 校验业务约束 {@code ensureApp}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param appCode 可追踪业务编码，类型为 {@code String}
     */
    private void ensureApp(String appCode) {
        IamAdminMapper.AppRow app = mapper.findApp(appCode);
        if (app == null || app.status() != 1) {
            throw new IllegalStateException("application is not enabled");
        }
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code IamApplicationAggregate}
     * @return 转换数据模型的结果，类型为 {@code IamAdminMapper.AppRow}
     */
    private IamAdminMapper.AppRow toRow(IamApplicationAggregate aggregate) {
        return new IamAdminMapper.AppRow(null, aggregate.appCode(), aggregate.appName(), aggregate.homeUrl(), aggregate.status(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code IamSsoClientAggregate}
     * @return 转换数据模型的结果，类型为 {@code IamAdminMapper.SsoRow}
     */
    private IamAdminMapper.SsoRow toRow(IamSsoClientAggregate aggregate) {
        return new IamAdminMapper.SsoRow(null, aggregate.ssoCode(), aggregate.appCode(), aggregate.redirectUrl(), aggregate.secretHash(), aggregate.status(), aggregate.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code outbox}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     */
    private void outbox(String eventType, String businessNo, String payload) {
        mapper.insertOutbox(new IamPermissionOpenApiMapper.OutboxEventRow(eventIds.incrementAndGet(), eventType, businessNo, payload == null ? "" : payload, 1, LocalDateTime.now()));
    }

    /**
     * 处理当前类型职责中的操作 {@code hash}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String hash(String value) {
        return "HASH:" + value;
    }

    /**
     * CreateAppCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateAppCommand(String appCode, String appName, String homeUrl, Long operatorId, String idempotencyKey) {
    }

    /**
     * ChangeAppCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ChangeAppCommand(String appName, String homeUrl, long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * ToggleCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ToggleCommand(boolean enabled, String reason, long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * CreateMenuCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateMenuCommand(String menuCode, String appCode, String parentCode, String menuName, String routePath, int sortNo, Long operatorId, String idempotencyKey) {
    }

    /**
     * DisableCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record DisableCommand(String reason, long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * ConfigureSsoCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ConfigureSsoCommand(String ssoCode, String appCode, String redirectUrl, Long operatorId, String idempotencyKey) {
    }

    /**
     * ResetSsoSecretCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ResetSsoSecretCommand(long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * SsoSecret。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record SsoSecret(String ssoCode, String plainSecret) {
    }

    /**
     * EventEnvelope。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record EventEnvelope(String eventId, String eventType, String sourceSystem, String businessNo, String payload) {
    }

    /**
     * ConsumeResult。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ConsumeResult(String consumeId, String consumeStatus, boolean idempotentHit, String message) {
    }

    /**
     * 业务常量 {@code MASTER_DATA_PUBLISHED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String MASTER_DATA_PUBLISHED = "MasterDataPublished";

    /**
     * 业务常量 {@code PERMISSION_RESOURCE_SCANNED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String PERMISSION_RESOURCE_SCANNED = "PermissionResourceScanned";

    /**
     * 业务常量 {@code SECURITY_RISK_DETECTED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String SECURITY_RISK_DETECTED = "SecurityRiskDetected";
}
