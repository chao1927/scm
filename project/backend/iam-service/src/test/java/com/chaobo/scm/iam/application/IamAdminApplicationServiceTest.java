package com.chaobo.scm.iam.application;

import com.chaobo.scm.iam.infrastructure.persistence.IamAdminMapper;
import com.chaobo.scm.iam.infrastructure.persistence.IamPermissionOpenApiMapper;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * IamAdminApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class IamAdminApplicationServiceTest {

    /**
     * 处理当前类型职责中的操作 {@code appMenuAndSsoProduceOutboxEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void appMenuAndSsoProduceOutboxEvents() {
        MemoryAdminMapper mapper = new MemoryAdminMapper();
        IamAdminApplicationService service = new IamAdminApplicationService(mapper);
        IamAdminMapper.AppRow app = service.createApp(new IamAdminApplicationService.CreateAppCommand("OMS", "订单系统", "/oms", 1001L, "idem-1"));
        service.createMenu(new IamAdminApplicationService.CreateMenuCommand("OMS_ORDER", "OMS", null, "订单管理", "/orders", 1, 1001L, "idem-2"));
        IamAdminApplicationService.SsoSecret secret = service.configureSso(new IamAdminApplicationService.ConfigureSsoCommand("OMS-WEB", "OMS", "https://oms.example/callback", 1001L, "idem-3"));
        service.toggleApp("OMS", new IamAdminApplicationService.ToggleCommand(false, "下线", app.version(), 1001L, "idem-4"));
        assertThat(secret.plainSecret()).startsWith("SSO-");
        assertThat(service.listMenus("OMS")).hasSize(1);
        assertThat(mapper.outbox).extracting(IamPermissionOpenApiMapper.OutboxEventRow::eventType).contains("IamAppCreated", "IamMenuCreated", "SsoClientConfigured", "IamAppDisabled", "PermissionSnapshotInvalidated");
    }

    /**
     * 处理当前类型职责中的操作 {@code eventInboxIsIdempotentAndUnsupportedEventsAreIgnored}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void eventInboxIsIdempotentAndUnsupportedEventsAreIgnored() {
        MemoryAdminMapper mapper = new MemoryAdminMapper();
        IamAdminApplicationService service = new IamAdminApplicationService(mapper);
        IamAdminApplicationService.EventEnvelope event = new IamAdminApplicationService.EventEnvelope("evt-1", "PermissionResourceScanned", "GATEWAY", "API-1", "{}");
        IamAdminApplicationService.EventEnvelope unsupported = new IamAdminApplicationService.EventEnvelope("evt-2", "UnknownEvent", "SYS", "BIZ-1", "{}");
        IamAdminApplicationService.ConsumeResult first = service.consumeEvent(event);
        IamAdminApplicationService.ConsumeResult duplicate = service.consumeEvent(event);
        IamAdminApplicationService.ConsumeResult ignored = service.consumeEvent(unsupported);
        assertThat(first.consumeStatus()).isEqualTo("SUCCESS");
        assertThat(duplicate.idempotentHit()).isTrue();
        assertThat(ignored.consumeStatus()).isEqualTo("IGNORED");
        assertThat(mapper.inbox.get("evt-1").status()).isEqualTo(2);
        assertThat(mapper.inbox.get("evt-2").status()).isEqualTo(4);
    }

    /**
     * MemoryAdminMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class MemoryAdminMapper implements IamAdminMapper {

        /**
         * apps（类型：{@code Map<String,AppRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, AppRow> apps = new LinkedHashMap<>();

        /**
         * menus（类型：{@code Map<String,MenuRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, MenuRow> menus = new LinkedHashMap<>();

        /**
         * sso（类型：{@code Map<String,SsoRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, SsoRow> sso = new LinkedHashMap<>();

        /**
         * inbox（类型：{@code Map<String,EventInboxRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, EventInboxRow> inbox = new LinkedHashMap<>();

        /**
         * outbox（类型：{@code List<IamPermissionOpenApiMapper.OutboxEventRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<IamPermissionOpenApiMapper.OutboxEventRow> outbox = new ArrayList<>();

        /**
         * 查询并返回 {@code findApp}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param appCode 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code AppRow}
         */
        @Override
        public AppRow findApp(String appCode) {
            return apps.get(appCode);
        }

        /**
         * 查询并返回 {@code listApps}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<AppRow>}
         */
        @Override
        public List<AppRow> listApps() {
            return new ArrayList<>(apps.values());
        }

        /**
         * 处理当前类型职责中的操作 {@code insertApp}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code AppRow}
         */
        @Override
        public void insertApp(AppRow row) {
            apps.put(row.appCode(), row);
        }

        /**
         * 执行命令 {@code updateApp}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code AppRow}
         */
        @Override
        public void updateApp(AppRow row) {
            apps.put(row.appCode(), row);
        }

        /**
         * 查询并返回 {@code findMenu}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param menuCode 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code MenuRow}
         */
        @Override
        public MenuRow findMenu(String menuCode) {
            return menus.get(menuCode);
        }

        /**
         * 查询并返回 {@code listMenus}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param appCode 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code List<MenuRow>}
         */
        @Override
        public List<MenuRow> listMenus(String appCode) {
            return menus.values().stream().filter(row -> row.appCode().equals(appCode)).toList();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertMenu}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code MenuRow}
         */
        @Override
        public void insertMenu(MenuRow row) {
            menus.put(row.menuCode(), row);
        }

        /**
         * 执行命令 {@code updateMenu}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code MenuRow}
         */
        @Override
        public void updateMenu(MenuRow row) {
            menus.put(row.menuCode(), row);
        }

        /**
         * 查询并返回 {@code findSso}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param ssoCode 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code SsoRow}
         */
        @Override
        public SsoRow findSso(String ssoCode) {
            return sso.get(ssoCode);
        }

        /**
         * 查询并返回 {@code listSso}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<SsoRow>}
         */
        @Override
        public List<SsoRow> listSso() {
            return new ArrayList<>(sso.values());
        }

        /**
         * 处理当前类型职责中的操作 {@code insertSso}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code SsoRow}
         */
        @Override
        public void insertSso(SsoRow row) {
            sso.put(row.ssoCode(), row);
        }

        /**
         * 执行命令 {@code updateSso}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code SsoRow}
         */
        @Override
        public void updateSso(SsoRow row) {
            sso.put(row.ssoCode(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOutbox}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code IamPermissionOpenApiMapper.OutboxEventRow}
         */
        @Override
        public void insertOutbox(IamPermissionOpenApiMapper.OutboxEventRow row) {
            outbox.add(row);
        }

        /**
         * 处理当前类型职责中的操作 {@code claimEvent}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        @Override
        public int claimEvent(EventInboxRow row) {
            if (inbox.containsKey(row.eventId())) {
                return 0;
            }
            inbox.put(row.eventId(), row);
            return 1;
        }

        /**
         * 执行命令 {@code updateEvent}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
         */
        @Override
        public void updateEvent(EventInboxRow row) {
            inbox.put(row.eventId(), row);
        }

        /**
         * 查询并返回 {@code listInbox}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<EventInboxRow>}
         */
        @Override
        public List<EventInboxRow> listInbox() {
            return new ArrayList<>(inbox.values());
        }
    }
}
