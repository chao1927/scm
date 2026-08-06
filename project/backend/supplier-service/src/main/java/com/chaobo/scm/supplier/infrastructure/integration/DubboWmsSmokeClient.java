package com.chaobo.scm.supplier.infrastructure.integration;

import com.chaobo.scm.common.integration.ScmDubboContract;
import com.chaobo.scm.common.integration.WmsCollaborationApi;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 独立 JVM Dubbo 冒烟客户端，用于验证 Nacos 注册发现、Provider 重启恢复和未注册失败关闭。
 *
 * <p>该类不启动 Supplier Spring 容器，确保 Consumer 与 WMS Provider 处于不同 JVM。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class DubboWmsSmokeClient {

    private DubboWmsSmokeClient() {
    }

    /**
     * 运行一次真实 WMS 入库预约命令。
     *
     * @param args 依次为注册中心地址、业务标识和幂等键
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("usage: <registry-address> <business-id> <idempotency-key>");
            System.exit(64);
        }
        System.setProperty("dubbo.qos.enable", "false");
        ReferenceConfig<WmsCollaborationApi> reference = new ReferenceConfig<>();
        try {
            long businessId = Long.parseLong(args[1]);
            reference.setApplication(new ApplicationConfig("scm-dubbo-smoke-consumer"));
            reference.setRegistry(new RegistryConfig(args[0]));
            reference.setInterface(WmsCollaborationApi.class);
            reference.setProtocol("tri");
            reference.setGroup(ScmDubboContract.GROUP);
            reference.setVersion(ScmDubboContract.VERSION);
            reference.setTimeout(ScmDubboContract.TIMEOUT_MILLIS);
            reference.setRetries(0);
            reference.setCheck(true);
            WmsCollaborationApi client = reference.get();
            WmsCollaborationApi.AppointmentResult result = client.createOrAdjustInboundAppointment(
                    new WmsCollaborationApi.InboundAppointmentCommand(args[2], businessId,
                            "ASN-SMOKE-" + businessId, 9001L, 8001L,
                            OffsetDateTime.parse("2099-01-02T09:00:00+08:00"),
                            List.of(new WmsCollaborationApi.Line(1L, "SKU-SMOKE", null,
                                    BigDecimal.ONE))));
            if (!result.accepted() || result.appointmentNo() == null
                    || result.appointmentNo().isBlank()) {
                throw new IllegalStateException("WMS Provider rejected smoke command: " + result.reason());
            }
            System.out.println("DUBBO_SMOKE_OK " + result.appointmentNo());
            System.exit(0);
        } catch (RuntimeException exception) {
            System.err.println("DUBBO_SMOKE_FAILED " + exception.getClass().getSimpleName()
                    + ": " + exception.getMessage());
            System.exit(2);
        } finally {
            reference.destroy();
        }
    }
}
