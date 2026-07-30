package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.WaybillApplicationService;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * WaybillController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/tms/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'tms:*', 'tms:waybill:manage')")
public class WaybillController {

    /**
     * service（类型：{@code WaybillApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final WaybillApplicationService service;

    /**
     * 创建 WaybillController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code WaybillApplicationService}
     */
    public WaybillController(WaybillApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param request 接口请求参数，类型为 {@code CreateWaybillRequest}
     * @return 执行命令的结果，类型为 {@code WaybillMapper.WaybillRow}
     */
    @PostMapping("/transport-tasks/{taskNo}/waybills")
    public WaybillMapper.WaybillRow create(@PathVariable String taskNo, @RequestBody CreateWaybillRequest request) {
        return service.createFromTask(taskNo, new WaybillApplicationService.CreateCommand(request.carrierCode(), request.carrierName(), request.carrierWaybillNo(), request.logisticsProductCode(), request.receiptPayload(), request.operatorId(), request.idempotencyKey()));
    }

    /**
     * 处理当前类型职责中的操作 {@code voidWaybill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param request 接口请求参数，类型为 {@code VoidWaybillRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code WaybillMapper.WaybillRow}
     */
    @PostMapping("/waybills/{waybillNo}/void")
    public WaybillMapper.WaybillRow voidWaybill(@PathVariable String waybillNo, @RequestBody VoidWaybillRequest request) {
        return service.voidWaybill(waybillNo, new WaybillApplicationService.VoidCommand(request.reason(), request.approvalNo(), request.expectedVersion(), request.operatorId(), request.idempotencyKey()));
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<WaybillMapper.WaybillRow>}
     */
    @GetMapping("/waybills")
    public List<WaybillMapper.WaybillRow> list() {
        return service.list();
    }

    /**
     * 查询并返回 {@code get}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code WaybillMapper.WaybillRow}
     */
    @GetMapping("/waybills/{waybillNo}")
    public WaybillMapper.WaybillRow get(@PathVariable String waybillNo) {
        return service.get(waybillNo);
    }

    /**
     * CreateWaybillRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateWaybillRequest(String carrierCode, String carrierName, String carrierWaybillNo, String logisticsProductCode, String receiptPayload, Long operatorId, String idempotencyKey) {
    }

    /**
     * VoidWaybillRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record VoidWaybillRequest(String reason, String approvalNo, long expectedVersion, Long operatorId, String idempotencyKey) {
    }
}
