package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.TransportTaskApplicationService;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * TransportTaskController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/tms/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'tms:*', 'tms:task:manage')")
public class TransportTaskController {

    /**
     * service（类型：{@code TransportTaskApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final TransportTaskApplicationService service;

    /**
     * 创建 TransportTaskController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code TransportTaskApplicationService}
     */
    public TransportTaskController(TransportTaskApplicationService service) {
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code accept}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param request 接口请求参数，类型为 {@code AcceptRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransportTaskMapper.TaskRow}
     */
    @PostMapping("/transport-tasks/{taskNo}/accept")
    public TransportTaskMapper.TaskRow accept(@PathVariable String taskNo, @RequestBody AcceptRequest request) {
        return service.accept(taskNo, new TransportTaskApplicationService.AcceptCommand(request.carrierCode(), request.carrierName(), request.logisticsProductCode(), request.expectedVersion(), request.operatorId(), request.idempotencyKey()));
    }

    /**
     * 处理当前类型职责中的操作 {@code start}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param request 接口请求参数，类型为 {@code ChangeRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransportTaskMapper.TaskRow}
     */
    @PostMapping("/transport-tasks/{taskNo}/start")
    public TransportTaskMapper.TaskRow start(@PathVariable String taskNo, @RequestBody ChangeRequest request) {
        return service.start(taskNo, new TransportTaskApplicationService.ChangeCommand(request.expectedVersion(), request.operatorId(), request.idempotencyKey()));
    }

    /**
     * 处理当前类型职责中的操作 {@code deliver}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param request 接口请求参数，类型为 {@code ChangeRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransportTaskMapper.TaskRow}
     */
    @PostMapping("/transport-tasks/{taskNo}/deliver")
    public TransportTaskMapper.TaskRow deliver(@PathVariable String taskNo, @RequestBody ChangeRequest request) {
        return service.deliver(taskNo, new TransportTaskApplicationService.ChangeCommand(request.expectedVersion(), request.operatorId(), request.idempotencyKey()));
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param scenario 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param warehouseId 业务或技术标识，类型为 {@code Long}
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param pageNo 可追踪业务编码，类型为 {@code Integer}
     * @param pageSize 业务处理参数或成员，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code List<TransportTaskMapper.TaskRow>}
     */
    @GetMapping("/transport-tasks")
    public List<TransportTaskMapper.TaskRow> list(@RequestParam(required = false) String sourceSystem, @RequestParam(required = false) String scenario, @RequestParam(required = false) Integer status, @RequestParam(required = false) Long warehouseId, @RequestParam(required = false) String carrierCode, @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "20") Integer pageSize) {
        return service.list(new TransportTaskApplicationService.Query(sourceSystem, scenario, status, warehouseId, carrierCode, pageNo, pageSize));
    }

    /**
     * 查询并返回 {@code get}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code TransportTaskMapper.TaskRow}
     */
    @GetMapping("/transport-tasks/{taskNo}")
    public TransportTaskMapper.TaskRow get(@PathVariable String taskNo) {
        return service.get(taskNo);
    }

    /**
     * AcceptRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record AcceptRequest(String carrierCode, String carrierName, String logisticsProductCode, long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * ChangeRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ChangeRequest(long expectedVersion, Long operatorId, String idempotencyKey) {
    }
}
