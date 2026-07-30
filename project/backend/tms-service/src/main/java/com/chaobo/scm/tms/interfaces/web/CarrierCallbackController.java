package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.CarrierCallbackApplicationService;
import com.chaobo.scm.common.security.ScmAccessContexts;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

/**
 * CarrierCallbackController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/openapi/tms/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'tms:*', 'tms:carrier-callback:write')")
public class CarrierCallbackController {

    /**
     * service（类型：{@code CarrierCallbackApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final CarrierCallbackApplicationService service;

    /**
     * 创建 CarrierCallbackController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code CarrierCallbackApplicationService}
     */
    public CarrierCallbackController(CarrierCallbackApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param request 接口请求参数，类型为 {@code CarrierCallbackRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     */
    @PostMapping("/carrier-callbacks/{carrierCode}")
    public void consume(@PathVariable String carrierCode, @RequestBody CarrierCallbackRequest request, Authentication authentication) {
        ScmAccessContexts.require(authentication).requireApplication(carrierCode);
        service.consume(new CarrierCallbackApplicationService.CarrierEvent(request.eventId(), request.eventType(), carrierCode, request.waybillNo(), request.nodeCode(), request.description(), request.location(), request.occurredAt(), request.receiptResult(), request.signedBy(), request.rejectReason(), request.proofUrl(), request.operatorId(), request.payload()));
    }

    /**
     * CarrierCallbackRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CarrierCallbackRequest(String eventId, String eventType, String waybillNo, String nodeCode, String description, String location, LocalDateTime occurredAt, int receiptResult, String signedBy, String rejectReason, String proofUrl, Long operatorId, String payload) {
    }
}
