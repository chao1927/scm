package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmOpenApiApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * MdmMasterDataOpenApiController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/openapi/mdm/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'mdm:*', 'mdm:openapi:read')")
public class MdmMasterDataOpenApiController {

    /**
     * service（类型：{@code MdmOpenApiApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final MdmOpenApiApplicationService service;

    /**
     * 创建 MdmMasterDataOpenApiController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code MdmOpenApiApplicationService}
     */
    public MdmMasterDataOpenApiController(MdmOpenApiApplicationService service) {
        this.service = service;
    }

    /**
     * 查询并返回 {@code query}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param request 接口请求参数，类型为 {@code MdmOpenApiApplicationService.QueryRequest}
     * @return 查询并返回的结果，类型为 {@code MdmOpenApiApplicationService.QueryResponse}
     */
    @PostMapping("/master-data/query")
    public MdmOpenApiApplicationService.QueryResponse query(@RequestBody MdmOpenApiApplicationService.QueryRequest request) {
        return service.query(request);
    }

    /**
     * 校验业务约束 {@code validate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param request 接口请求参数，类型为 {@code MdmOpenApiApplicationService.ValidateRequest}
     * @return 校验业务约束的结果，类型为 {@code MdmOpenApiApplicationService.ValidateResponse}
     */
    @PostMapping("/master-data/validate")
    public MdmOpenApiApplicationService.ValidateResponse validate(@RequestBody MdmOpenApiApplicationService.ValidateRequest request) {
        return service.validate(request);
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param dataCode 可追踪业务编码，类型为 {@code String}
     * @param includeDisabled 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MdmOpenApiApplicationService.Snapshot}
     */
    @GetMapping("/master-data/{typeCode}/{dataCode}")
    public MdmOpenApiApplicationService.Snapshot snapshot(@PathVariable String typeCode, @PathVariable String dataCode, @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return service.snapshot(typeCode, dataCode, includeDisabled);
    }
}
