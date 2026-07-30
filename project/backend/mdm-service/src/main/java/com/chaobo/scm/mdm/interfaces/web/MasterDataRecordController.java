package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MasterDataRecordApplicationService;
import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * MasterDataRecordController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/mdm/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'mdm:*', 'mdm:record:manage')")
public class MasterDataRecordController {

    /**
     * service（类型：{@code MasterDataRecordApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataRecordApplicationService service;

    /**
     * 创建 MasterDataRecordController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code MasterDataRecordApplicationService}
     */
    public MasterDataRecordController(MasterDataRecordApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code MasterDataRecordApplicationService.CreateRecordCommand}
     * @return 执行命令的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    @PostMapping("/master-data-records")
    public MasterDataRecordMapper.RecordRow create(@RequestBody MasterDataRecordApplicationService.CreateRecordCommand command) {
        return service.create(command);
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MasterDataRecordApplicationService.ChangeRecordCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    @PutMapping("/master-data-records/{recordNo}")
    public MasterDataRecordMapper.RecordRow change(@PathVariable String recordNo, @RequestBody MasterDataRecordApplicationService.ChangeRecordCommand command) {
        return service.change(recordNo, command);
    }

    /**
     * 执行命令 {@code submitReview}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MasterDataRecordApplicationService.StateCommand}
     * @return 执行命令的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    @PostMapping("/master-data-records/{recordNo}/submit-review")
    public MasterDataRecordMapper.RecordRow submitReview(@PathVariable String recordNo, @RequestBody MasterDataRecordApplicationService.StateCommand command) {
        return service.submitReview(recordNo, command);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MasterDataRecordApplicationService.StateCommand}
     * @return 执行命令的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    @PostMapping("/master-data-records/{recordNo}/approve")
    public MasterDataRecordMapper.RecordRow approve(@PathVariable String recordNo, @RequestBody MasterDataRecordApplicationService.StateCommand command) {
        return service.approve(recordNo, command);
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MasterDataRecordApplicationService.StateCommand}
     * @return 执行命令的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    @PostMapping("/master-data-records/{recordNo}/reject")
    public MasterDataRecordMapper.RecordRow reject(@PathVariable String recordNo, @RequestBody MasterDataRecordApplicationService.StateCommand command) {
        return service.reject(recordNo, command);
    }

    /**
     * 执行命令 {@code freeze}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MasterDataRecordApplicationService.StateCommand}
     * @return 执行命令的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    @PostMapping("/master-data-records/{recordNo}/freeze")
    public MasterDataRecordMapper.RecordRow freeze(@PathVariable String recordNo, @RequestBody MasterDataRecordApplicationService.StateCommand command) {
        return service.freeze(recordNo, command);
    }

    /**
     * 执行命令 {@code disable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MasterDataRecordApplicationService.StateCommand}
     * @return 执行命令的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    @PostMapping("/master-data-records/{recordNo}/disable")
    public MasterDataRecordMapper.RecordRow disable(@PathVariable String recordNo, @RequestBody MasterDataRecordApplicationService.StateCommand command) {
        return service.disable(recordNo, command);
    }

    /**
     * 查询并返回 {@code get}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code MasterDataRecordMapper.RecordRow}
     */
    @GetMapping("/master-data-records/{recordNo}")
    public MasterDataRecordMapper.RecordRow get(@PathVariable String recordNo) {
        return service.get(recordNo);
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code Integer}
     * @param pageSize 业务处理参数或成员，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code List<MasterDataRecordMapper.RecordRow>}
     */
    @GetMapping("/master-data-records")
    public List<MasterDataRecordMapper.RecordRow> list(@RequestParam(required = false) String typeCode, @RequestParam(required = false) Integer status, @RequestParam(required = false) Integer pageNo, @RequestParam(required = false) Integer pageSize) {
        return service.list(new MasterDataRecordApplicationService.Query(typeCode, status, pageNo, pageSize));
    }

    /**
     * 处理当前类型职责中的操作 {@code versions}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<MasterDataRecordMapper.VersionRow>}
     */
    @GetMapping("/master-data-records/{recordNo}/versions")
    public List<MasterDataRecordMapper.VersionRow> versions(@PathVariable String recordNo) {
        return service.listVersions(recordNo);
    }
}
