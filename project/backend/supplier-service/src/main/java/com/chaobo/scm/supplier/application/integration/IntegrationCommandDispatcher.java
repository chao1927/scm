package com.chaobo.scm.supplier.application.integration;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.common.integration.*;
import com.chaobo.scm.supplier.infrastructure.integration.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.time.*;
import java.util.*;

/**
 * IntegrationCommandDispatcher。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class IntegrationCommandDispatcher {

    /**
     * repo（类型：{@code IntegrationCommandRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final IntegrationCommandRepository repo;

    /**
     * dubbo（类型：{@code DubboReferenceFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final DubboReferenceFactory dubbo;

    /**
     * circuit（类型：{@code RemoteCallCircuitBreaker}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final RemoteCallCircuitBreaker circuit;

    /**
     * json（类型：{@code ObjectMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ObjectMapper json;

    /**
     * tx（类型：{@code TransactionTemplate}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final TransactionTemplate tx;

    /**
     * batchSize、maxRetries（类型：{@code int}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final int batchSize, maxRetries;

    /**
     * 创建 IntegrationCommandDispatcher。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repo 业务处理参数或成员，类型为 {@code IntegrationCommandRepository}
     * @param dubbo 业务处理参数或成员，类型为 {@code DubboReferenceFactory}
     * @param circuit 业务处理参数或成员，类型为 {@code RemoteCallCircuitBreaker}
     * @param json 业务处理参数或成员，类型为 {@code ObjectMapper}
     * @param manager 业务处理参数或成员，类型为 {@code PlatformTransactionManager}
     * @param batchSize 业务处理参数或成员，类型为 {@code int}
     * @param maxRetries 业务处理参数或成员，类型为 {@code int}
     */
    public IntegrationCommandDispatcher(IntegrationCommandRepository repo, DubboReferenceFactory dubbo, RemoteCallCircuitBreaker circuit, ObjectMapper json, PlatformTransactionManager manager, @Value("${scm.integration.batch-size:50}") int batchSize, @Value("${scm.integration.max-retries:8}") int maxRetries) {
        this.repo = repo;
        this.dubbo = dubbo;
        this.circuit = circuit;
        this.json = json;
        this.tx = new TransactionTemplate(manager);
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
    }

    /**
     * 执行命令 {@code dispatch}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Scheduled(fixedDelayString = "${scm.integration.fixed-delay:1000}")
    public void dispatch() {
        List<IntegrationCommand> commands = tx.execute(status -> {
            var rows = repo.lockDispatchable(batchSize);
            return rows.stream().filter(command -> repo.markExecuting(command.id())).toList();
        });
        if (commands != null) {
            commands.forEach(this::execute);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code execute}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param command 用例输入命令，类型为 {@code IntegrationCommand}
     */
    private void execute(IntegrationCommand command) {
        try {
            String reference = switch(command.type()) {
                case "WMS_CREATE_APPOINTMENT" ->
                    wmsAppointment(read(command, WmsCollaborationApi.InboundAppointmentCommand.class));
                case "WMS_CANCEL_APPOINTMENT" ->
                    wmsCancel(read(command, WmsCollaborationApi.CancelAppointmentCommand.class));
                case "WMS_CREATE_RETURN_OUTBOUND" ->
                    wmsOutbound(read(command, WmsCollaborationApi.ReturnOutboundCommand.class));
                case "TMS_CREATE_INBOUND_TRANSPORT" ->
                    tmsInbound(read(command, TmsCollaborationApi.InboundTransportCommand.class));
                case "TMS_CREATE_RETURN_TRANSPORT" ->
                    tmsReturn(read(command, TmsCollaborationApi.ReturnTransportCommand.class));
                case "TMS_CANCEL_TRANSPORT" ->
                    tmsCancel(read(command, TmsCollaborationApi.CancelTransportCommand.class));
                case "INVENTORY_LOCK_RETURN" ->
                    inventoryLock(read(command, InventoryCollaborationApi.ReturnLockCommand.class));
                case "INVENTORY_RELEASE_RETURN" ->
                    inventoryRelease(read(command, InventoryCollaborationApi.ReturnReleaseCommand.class));
                case "BMS_CREATE_RETURN_SETTLEMENT" ->
                    bmsSettlement(read(command, BmsCollaborationApi.ReturnSettlementCommand.class));
                case "MDM_CREATE_SUPPLIER" ->
                    mdmCreate(read(command, MasterDataCollaborationApi.CreateSupplierCommand.class));
                case "MDM_CHANGE_SUPPLIER_STATUS" ->
                    mdmStatus(read(command, MasterDataCollaborationApi.ChangeSupplierStatusCommand.class));
                case "IAM_UPDATE_SUPPLIER_SCOPE" ->
                    iamScope(read(command, IamCollaborationApi.UpdateSupplierScopeCommand.class));
                default ->
                    throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "不支持的集成命令: " + command.type());
            };
            tx.executeWithoutResult(status -> repo.markSucceeded(command.id(), reference));
        } catch (RuntimeException exception) {
            String reason = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
            OffsetDateTime next = OffsetDateTime.now().plusSeconds(Math.min(300L, 1L << Math.min(command.retryCount(), 8)));
            tx.executeWithoutResult(status -> repo.markRetry(command.id(), command.retryCount(), next, reason.substring(0, Math.min(1000, reason.length())), maxRetries));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code wmsAppointment}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param command 用例输入命令，类型为 {@code WmsCollaborationApi.InboundAppointmentCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String wmsAppointment(WmsCollaborationApi.InboundAppointmentCommand command) {
        var result = circuit.execute("WMS", () -> dubbo.client(WmsCollaborationApi.class).createOrAdjustInboundAppointment(command));
        accepted(result.accepted(), result.reason());
        return result.appointmentNo();
    }

    /**
     * 处理当前类型职责中的操作 {@code wmsCancel}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param command 用例输入命令，类型为 {@code WmsCollaborationApi.CancelAppointmentCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String wmsCancel(WmsCollaborationApi.CancelAppointmentCommand command) {
        circuit.execute("WMS", () -> {
            dubbo.client(WmsCollaborationApi.class).cancelInboundAppointment(command);
            return true;
        });
        return String.valueOf(command.asnId());
    }

    /**
     * 处理当前类型职责中的操作 {@code wmsOutbound}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param command 用例输入命令，类型为 {@code WmsCollaborationApi.ReturnOutboundCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String wmsOutbound(WmsCollaborationApi.ReturnOutboundCommand command) {
        var result = circuit.execute("WMS", () -> dubbo.client(WmsCollaborationApi.class).createSupplierReturnOutbound(command));
        accepted(result.accepted(), result.reason());
        return result.outboundNo();
    }

    /**
     * 处理当前类型职责中的操作 {@code tmsInbound}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param command 用例输入命令，类型为 {@code TmsCollaborationApi.InboundTransportCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String tmsInbound(TmsCollaborationApi.InboundTransportCommand command) {
        var result = circuit.execute("TMS", () -> dubbo.client(TmsCollaborationApi.class).createInboundTransport(command));
        accepted(result.accepted(), result.reason());
        return result.shipmentId();
    }

    /**
     * 处理当前类型职责中的操作 {@code tmsReturn}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param command 用例输入命令，类型为 {@code TmsCollaborationApi.ReturnTransportCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String tmsReturn(TmsCollaborationApi.ReturnTransportCommand command) {
        var result = circuit.execute("TMS", () -> dubbo.client(TmsCollaborationApi.class).createSupplierReturnTransport(command));
        accepted(result.accepted(), result.reason());
        return result.shipmentId();
    }

    /**
     * 处理当前类型职责中的操作 {@code tmsCancel}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param command 用例输入命令，类型为 {@code TmsCollaborationApi.CancelTransportCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String tmsCancel(TmsCollaborationApi.CancelTransportCommand command) {
        circuit.execute("TMS", () -> {
            dubbo.client(TmsCollaborationApi.class).cancelTransport(command);
            return true;
        });
        return String.valueOf(command.businessId());
    }

    /**
     * 处理当前类型职责中的操作 {@code inventoryLock}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param command 用例输入命令，类型为 {@code InventoryCollaborationApi.ReturnLockCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String inventoryLock(InventoryCollaborationApi.ReturnLockCommand command) {
        var result = circuit.execute("INVENTORY", () -> dubbo.client(InventoryCollaborationApi.class).lockSupplierReturn(command));
        accepted(result.accepted(), result.reason());
        return result.lockNo();
    }

    /**
     * 处理当前类型职责中的操作 {@code inventoryRelease}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param command 用例输入命令，类型为 {@code InventoryCollaborationApi.ReturnReleaseCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String inventoryRelease(InventoryCollaborationApi.ReturnReleaseCommand command) {
        circuit.execute("INVENTORY", () -> {
            dubbo.client(InventoryCollaborationApi.class).releaseSupplierReturn(command);
            return true;
        });
        return command.lockNo();
    }

    /**
     * 处理当前类型职责中的操作 {@code bmsSettlement}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param command 用例输入命令，类型为 {@code BmsCollaborationApi.ReturnSettlementCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String bmsSettlement(BmsCollaborationApi.ReturnSettlementCommand command) {
        var result = circuit.execute("BMS", () -> dubbo.client(BmsCollaborationApi.class).createSupplierReturnSettlement(command));
        accepted(result.accepted(), result.reason());
        return result.settlementRef();
    }

    /**
     * 处理当前类型职责中的操作 {@code mdmCreate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param command 用例输入命令，类型为 {@code MasterDataCollaborationApi.CreateSupplierCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String mdmCreate(MasterDataCollaborationApi.CreateSupplierCommand command) {
        var result = circuit.execute("MDM", () -> dubbo.client(MasterDataCollaborationApi.class).createSupplier(command));
        accepted(result.accepted(), result.reason());
        return String.valueOf(result.supplierId());
    }

    /**
     * 处理当前类型职责中的操作 {@code mdmStatus}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param command 用例输入命令，类型为 {@code MasterDataCollaborationApi.ChangeSupplierStatusCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String mdmStatus(MasterDataCollaborationApi.ChangeSupplierStatusCommand command) {
        circuit.execute("MDM", () -> {
            dubbo.client(MasterDataCollaborationApi.class).changeSupplierStatus(command);
            return true;
        });
        return String.valueOf(command.supplierId());
    }

    /**
     * 处理当前类型职责中的操作 {@code iamScope}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param command 用例输入命令，类型为 {@code IamCollaborationApi.UpdateSupplierScopeCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String iamScope(IamCollaborationApi.UpdateSupplierScopeCommand command) {
        circuit.execute("IAM", () -> {
            dubbo.client(IamCollaborationApi.class).updateSupplierDataScope(command);
            return true;
        });
        return String.valueOf(command.userId());
    }

    /**
     * 处理当前类型职责中的操作 {@code accepted}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param accepted 业务处理参数或成员，类型为 {@code boolean}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    private static void accepted(boolean accepted, String reason) {
        if (!accepted) {
            throw new BusinessException(ErrorCode.EXTERNAL_CALL_FAILED, reason == null ? "远程系统拒绝命令" : reason);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code read}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param command 用例输入命令，类型为 {@code IntegrationCommand}
     * @param type 业务处理参数或成员，类型为 {@code Class<T>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code T}
     */
    private <T> T read(IntegrationCommand command, Class<T> type) {
        try {
            return json.readValue(command.payloadJson(), type);
        } catch (JacksonException exception) {
            throw new IllegalStateException("集成命令反序列化失败", exception);
        }
    }
}
