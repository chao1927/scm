package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.DeliveryReceiptApplicationService;
import com.chaobo.scm.tms.infrastructure.persistence.TrackingMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

/**
 * DeliveryReceiptController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/tms/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'tms:*', 'tms:receipt:manage')")
public class DeliveryReceiptController {

    /**
     * service（类型：{@code DeliveryReceiptApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final DeliveryReceiptApplicationService service;

    /**
     * 创建 DeliveryReceiptController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code DeliveryReceiptApplicationService}
     */
    public DeliveryReceiptController(DeliveryReceiptApplicationService service) {
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code record}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param request 接口请求参数，类型为 {@code RecordReceiptRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TrackingMapper.ReceiptRow}
     */
    @PostMapping("/delivery-receipts")
    public TrackingMapper.ReceiptRow record(@RequestBody RecordReceiptRequest request) {
        return service.record(new DeliveryReceiptApplicationService.RecordCommand(request.waybillNo(), request.result(), request.signedBy(), request.signedAt(), request.rejectReason(), request.proofUrl(), request.operatorId(), request.idempotencyKey()));
    }

    /**
     * 查询并返回 {@code get}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param receiptNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code TrackingMapper.ReceiptRow}
     */
    @GetMapping("/delivery-receipts/{receiptNo}")
    public TrackingMapper.ReceiptRow get(@PathVariable String receiptNo) {
        return service.get(receiptNo);
    }

    /**
     * RecordReceiptRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RecordReceiptRequest(String waybillNo, int result, String signedBy, LocalDateTime signedAt, String rejectReason, String proofUrl, Long operatorId, String idempotencyKey) {
    }
}
