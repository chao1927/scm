package com.chaobo.scm.iam.application;

import com.chaobo.scm.iam.infrastructure.persistence.IamAdminMapper;
import com.chaobo.scm.iam.infrastructure.persistence.IamPermissionOpenApiMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IamAdminApplicationServiceTest {
    @Test
    void appMenuAndSsoProduceOutboxEvents() {
        MemoryAdminMapper mapper = new MemoryAdminMapper();
        IamAdminApplicationService service = new IamAdminApplicationService(mapper);

        IamAdminMapper.AppRow app = service.createApp(new IamAdminApplicationService.CreateAppCommand(
                "OMS", "订单系统", "/oms", 1001L, "idem-1"));
        service.createMenu(new IamAdminApplicationService.CreateMenuCommand(
                "OMS_ORDER", "OMS", null, "订单管理", "/orders", 1, 1001L, "idem-2"));
        IamAdminApplicationService.SsoSecret secret = service.configureSso(
                new IamAdminApplicationService.ConfigureSsoCommand(
                        "OMS-WEB", "OMS", "https://oms.example/callback", 1001L, "idem-3"));
        service.toggleApp("OMS", new IamAdminApplicationService.ToggleCommand(false, "下线", app.version(), 1001L, "idem-4"));

        assertThat(secret.plainSecret()).startsWith("SSO-");
        assertThat(service.listMenus("OMS")).hasSize(1);
        assertThat(mapper.outbox).extracting(IamPermissionOpenApiMapper.OutboxEventRow::eventType)
                .contains("IamAppCreated", "IamMenuCreated", "SsoClientConfigured", "IamAppDisabled",
                        "PermissionSnapshotInvalidated");
    }

    @Test
    void eventInboxIsIdempotentAndUnsupportedEventsAreIgnored() {
        MemoryAdminMapper mapper = new MemoryAdminMapper();
        IamAdminApplicationService service = new IamAdminApplicationService(mapper);
        IamAdminApplicationService.EventEnvelope event = new IamAdminApplicationService.EventEnvelope(
                "evt-1", "PermissionResourceScanned", "GATEWAY", "API-1", "{}");
        IamAdminApplicationService.EventEnvelope unsupported = new IamAdminApplicationService.EventEnvelope(
                "evt-2", "UnknownEvent", "SYS", "BIZ-1", "{}");

        IamAdminApplicationService.ConsumeResult first = service.consumeEvent(event);
        IamAdminApplicationService.ConsumeResult duplicate = service.consumeEvent(event);
        IamAdminApplicationService.ConsumeResult ignored = service.consumeEvent(unsupported);

        assertThat(first.consumeStatus()).isEqualTo("SUCCESS");
        assertThat(duplicate.idempotentHit()).isTrue();
        assertThat(ignored.consumeStatus()).isEqualTo("IGNORED");
        assertThat(mapper.inbox.get("evt-1").status()).isEqualTo(2);
        assertThat(mapper.inbox.get("evt-2").status()).isEqualTo(4);
    }

    static class MemoryAdminMapper implements IamAdminMapper {
        final Map<String, AppRow> apps = new LinkedHashMap<>();
        final Map<String, MenuRow> menus = new LinkedHashMap<>();
        final Map<String, SsoRow> sso = new LinkedHashMap<>();
        final Map<String, EventInboxRow> inbox = new LinkedHashMap<>();
        final List<IamPermissionOpenApiMapper.OutboxEventRow> outbox = new ArrayList<>();

        @Override
        public AppRow findApp(String appCode) { return apps.get(appCode); }

        @Override
        public List<AppRow> listApps() { return new ArrayList<>(apps.values()); }

        @Override
        public void insertApp(AppRow row) { apps.put(row.appCode(), row); }

        @Override
        public void updateApp(AppRow row) { apps.put(row.appCode(), row); }

        @Override
        public MenuRow findMenu(String menuCode) { return menus.get(menuCode); }

        @Override
        public List<MenuRow> listMenus(String appCode) {
            return menus.values().stream().filter(row -> row.appCode().equals(appCode)).toList();
        }

        @Override
        public void insertMenu(MenuRow row) { menus.put(row.menuCode(), row); }

        @Override
        public void updateMenu(MenuRow row) { menus.put(row.menuCode(), row); }

        @Override
        public SsoRow findSso(String ssoCode) { return sso.get(ssoCode); }

        @Override
        public List<SsoRow> listSso() { return new ArrayList<>(sso.values()); }

        @Override
        public void insertSso(SsoRow row) { sso.put(row.ssoCode(), row); }

        @Override
        public void updateSso(SsoRow row) { sso.put(row.ssoCode(), row); }

        @Override
        public void insertOutbox(IamPermissionOpenApiMapper.OutboxEventRow row) { outbox.add(row); }

        @Override
        public int claimEvent(EventInboxRow row) {
            if (inbox.containsKey(row.eventId())) {
                return 0;
            }
            inbox.put(row.eventId(), row);
            return 1;
        }

        @Override
        public void updateEvent(EventInboxRow row) { inbox.put(row.eventId(), row); }

        @Override
        public List<EventInboxRow> listInbox() { return new ArrayList<>(inbox.values()); }
    }
}
