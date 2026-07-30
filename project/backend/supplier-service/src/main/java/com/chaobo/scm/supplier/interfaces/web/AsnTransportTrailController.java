package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.supplier.application.asn.AsnTransportQueryApplicationService;
import jakarta.servlet.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * AsnTransportTrailController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/supplier/v1/asns/{asnId}/transport-trails")
public class AsnTransportTrailController {

    /**
     * service（类型：{@code AsnTransportQueryApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final AsnTransportQueryApplicationService service;

    /**
     * 创建 AsnTransportTrailController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code AsnTransportQueryApplicationService}
     */
    public AsnTransportTrailController(AsnTransportQueryApplicationService service) {
        this.service = service;
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 查询并返回的结果，类型为 {@code ApiResponse<List<AsnTransportQueryApplicationService.Trail>>}
     */
    @GetMapping
    public ApiResponse<List<AsnTransportQueryApplicationService.Trail>> list(@PathVariable long asnId, @AuthenticationPrincipal Jwt jwt, HttpServletRequest r) {
        return ApiResponse.success(service.trails(asnId, scope(jwt)), r.getHeader("X-Request-Id"), r.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code scope}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    private Long scope(Jwt jwt) {
        Number n = jwt.hasClaim("supplier_id") ? jwt.getClaim("supplier_id") : null;
        return n == null ? null : n.longValue();
    }
}
