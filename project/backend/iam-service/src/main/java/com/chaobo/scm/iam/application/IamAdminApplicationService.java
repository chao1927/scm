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

@Service
public class IamAdminApplicationService {
    private final IamAdminMapper mapper;
    private final AtomicLong eventIds = new AtomicLong(System.currentTimeMillis());

    public IamAdminApplicationService(IamAdminMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional
    public IamAdminMapper.AppRow createApp(CreateAppCommand command) {
        IamAdminMapper.AppRow existing = mapper.findApp(command.appCode());
        if (existing != null) {
            return existing;
        }
        IamApplicationAggregate aggregate = IamApplicationAggregate.create(command.appCode(), command.appName(),
                command.homeUrl());
        IamAdminMapper.AppRow row = toRow(aggregate);
        mapper.insertApp(row);
        outbox("IamAppCreated", row.appCode(), row.appName());
        return row;
    }

    @Transactional
    public IamAdminMapper.AppRow changeApp(String appCode, ChangeAppCommand command) {
        IamApplicationAggregate aggregate = loadApp(appCode);
        aggregate.change(command.appName(), command.homeUrl(), command.expectedVersion());
        mapper.updateApp(toRow(aggregate));
        outbox("IamAppChanged", appCode, command.appName());
        return mapper.findApp(appCode);
    }

    @Transactional
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

    public List<IamAdminMapper.AppRow> listApps() {
        return mapper.listApps();
    }

    @Transactional
    public IamAdminMapper.MenuRow createMenu(CreateMenuCommand command) {
        ensureApp(command.appCode());
        if (mapper.findMenu(command.menuCode()) != null) {
            throw new IllegalStateException("menu code already exists");
        }
        IamAdminMapper.MenuRow row = new IamAdminMapper.MenuRow(null, command.menuCode(), command.appCode(),
                command.parentCode(), command.menuName(), command.routePath(), command.sortNo(), 1, 1);
        mapper.insertMenu(row);
        outbox("IamMenuCreated", command.menuCode(), command.appCode());
        return row;
    }

    @Transactional
    public IamAdminMapper.MenuRow disableMenu(String menuCode, DisableCommand command) {
        IamAdminMapper.MenuRow row = mapper.findMenu(menuCode);
        if (row == null) {
            throw new IllegalArgumentException("menu not found");
        }
        if (row.version() != command.expectedVersion()) {
            throw new IllegalStateException("menu version conflict");
        }
        IamAdminMapper.MenuRow disabled = new IamAdminMapper.MenuRow(null, row.menuCode(), row.appCode(),
                row.parentCode(), row.menuName(), row.routePath(), row.sortNo(), 2, row.version() + 1);
        mapper.updateMenu(disabled);
        outbox("IamMenuDisabled", menuCode, command.reason());
        outbox("PermissionSnapshotInvalidated", row.appCode(), "menu disabled");
        return mapper.findMenu(menuCode);
    }

    public List<IamAdminMapper.MenuRow> listMenus(String appCode) {
        return mapper.listMenus(appCode);
    }

    @Transactional
    public SsoSecret configureSso(ConfigureSsoCommand command) {
        ensureApp(command.appCode());
        String plainSecret = "SSO-" + eventIds.incrementAndGet();
        IamSsoClientAggregate aggregate = IamSsoClientAggregate.configure(command.ssoCode(), command.appCode(),
                command.redirectUrl(), hash(plainSecret));
        mapper.insertSso(toRow(aggregate));
        outbox("SsoClientConfigured", command.ssoCode(), command.appCode());
        return new SsoSecret(command.ssoCode(), plainSecret);
    }

    @Transactional
    public SsoSecret resetSsoSecret(String ssoCode, ResetSsoSecretCommand command) {
        IamSsoClientAggregate aggregate = loadSso(ssoCode);
        String plainSecret = "SSO-" + eventIds.incrementAndGet();
        aggregate.resetSecret(hash(plainSecret), command.expectedVersion());
        mapper.updateSso(toRow(aggregate));
        outbox("SsoClientSecretReset", ssoCode, aggregate.appCode());
        return new SsoSecret(ssoCode, plainSecret);
    }

    public List<IamAdminMapper.SsoRow> listSso() {
        return mapper.listSso();
    }

    @Transactional
    public ConsumeResult consumeEvent(EventEnvelope event) {
        int claimed = mapper.claimEvent(new IamAdminMapper.EventInboxRow(event.eventId(), event.eventType(),
                event.businessNo(), event.payload(), 1, null));
        if (claimed == 0) {
            return new ConsumeResult(event.eventId(), "DUPLICATE", true, "idempotent hit");
        }
        if ("PermissionResourceScanned".equals(event.eventType())
                || "MasterDataPublished".equals(event.eventType())
                || "SecurityRiskDetected".equals(event.eventType())) {
            mapper.updateEvent(new IamAdminMapper.EventInboxRow(event.eventId(), event.eventType(),
                    event.businessNo(), event.payload(), 2, null));
            outbox("IamExternalEventConsumed", event.businessNo(), event.eventType());
            return new ConsumeResult(event.eventId(), "SUCCESS", false, "consumed");
        }
        mapper.updateEvent(new IamAdminMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessNo(),
                event.payload(), 4, "unsupported event type"));
        return new ConsumeResult(event.eventId(), "IGNORED", false, "unsupported event type");
    }

    public List<IamAdminMapper.EventInboxRow> listInbox() {
        return mapper.listInbox();
    }

    private IamApplicationAggregate loadApp(String appCode) {
        IamAdminMapper.AppRow row = mapper.findApp(appCode);
        if (row == null) {
            throw new IllegalArgumentException("application not found");
        }
        return IamApplicationAggregate.restore(row.appCode(), row.appName(), row.homeUrl(), row.status(),
                row.version());
    }

    private IamSsoClientAggregate loadSso(String ssoCode) {
        IamAdminMapper.SsoRow row = mapper.findSso(ssoCode);
        if (row == null) {
            throw new IllegalArgumentException("sso client not found");
        }
        return IamSsoClientAggregate.restore(row.ssoCode(), row.appCode(), row.redirectUrl(), row.secretHash(),
                row.status(), row.version());
    }

    private void ensureApp(String appCode) {
        IamAdminMapper.AppRow app = mapper.findApp(appCode);
        if (app == null || app.status() != 1) {
            throw new IllegalStateException("application is not enabled");
        }
    }

    private IamAdminMapper.AppRow toRow(IamApplicationAggregate aggregate) {
        return new IamAdminMapper.AppRow(null, aggregate.appCode(), aggregate.appName(), aggregate.homeUrl(),
                aggregate.status(), aggregate.version());
    }

    private IamAdminMapper.SsoRow toRow(IamSsoClientAggregate aggregate) {
        return new IamAdminMapper.SsoRow(null, aggregate.ssoCode(), aggregate.appCode(), aggregate.redirectUrl(),
                aggregate.secretHash(), aggregate.status(), aggregate.version());
    }

    private void outbox(String eventType, String businessNo, String payload) {
        mapper.insertOutbox(new IamPermissionOpenApiMapper.OutboxEventRow(eventIds.incrementAndGet(), eventType,
                businessNo, payload == null ? "" : payload, 1, LocalDateTime.now()));
    }

    private static String hash(String value) {
        return "HASH:" + value;
    }

    public record CreateAppCommand(String appCode, String appName, String homeUrl, Long operatorId,
                                   String idempotencyKey) {}
    public record ChangeAppCommand(String appName, String homeUrl, long expectedVersion, Long operatorId,
                                   String idempotencyKey) {}
    public record ToggleCommand(boolean enabled, String reason, long expectedVersion, Long operatorId,
                                String idempotencyKey) {}
    public record CreateMenuCommand(String menuCode, String appCode, String parentCode, String menuName,
                                    String routePath, int sortNo, Long operatorId, String idempotencyKey) {}
    public record DisableCommand(String reason, long expectedVersion, Long operatorId, String idempotencyKey) {}
    public record ConfigureSsoCommand(String ssoCode, String appCode, String redirectUrl, Long operatorId,
                                      String idempotencyKey) {}
    public record ResetSsoSecretCommand(long expectedVersion, Long operatorId, String idempotencyKey) {}
    public record SsoSecret(String ssoCode, String plainSecret) {}
    public record EventEnvelope(String eventId, String eventType, String sourceSystem, String businessNo,
                                String payload) {}
    public record ConsumeResult(String consumeId, String consumeStatus, boolean idempotentHit, String message) {}
}
