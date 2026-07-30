package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmImportQualityApplicationService;
import com.chaobo.scm.mdm.application.file.MdmFileTaskExecutionService;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmImportQualityMapper;
import com.chaobo.scm.common.security.ScmAccessContexts;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * MdmImportQualityController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/mdm/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'mdm:*', 'mdm:quality:manage')")
public class MdmImportQualityController {

    /**
     * service（类型：{@code MdmImportQualityApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final MdmImportQualityApplicationService service;
    private final MdmFileTaskExecutionService fileTaskExecutionService;

    /**
     * 创建 MdmImportQualityController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code MdmImportQualityApplicationService}
     */
    public MdmImportQualityController(MdmImportQualityApplicationService service) {
        this(service, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public MdmImportQualityController(MdmImportQualityApplicationService service,
                                      MdmFileTaskExecutionService fileTaskExecutionService) {
        this.service = service;
        this.fileTaskExecutionService = fileTaskExecutionService;
    }

    /**
     * 执行命令 {@code createImportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code MdmImportQualityApplicationService.CreateImportTaskCommand}
     * @return 执行命令的结果，类型为 {@code MdmImportQualityMapper.ImportTaskRow}
     */
    @PostMapping("/import-tasks")
    public MdmImportQualityMapper.ImportTaskRow createImportTask(@RequestBody MdmImportQualityApplicationService.CreateImportTaskCommand command) {
        return service.createImportTask(command);
    }

    /**
     * 校验业务约束 {@code validateImportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmImportQualityApplicationService.ValidateImportTaskCommand}
     * @return 校验业务约束的结果，类型为 {@code MdmImportQualityMapper.ImportTaskRow}
     */
    @PostMapping("/import-tasks/{importTaskNo}/validate")
    public MdmImportQualityMapper.ImportTaskRow validateImportTask(@PathVariable String importTaskNo, @RequestBody MdmImportQualityApplicationService.ValidateImportTaskCommand command) {
        return service.validateImportTask(importTaskNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code executeImportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmImportQualityApplicationService.StateCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MdmImportQualityMapper.ImportTaskRow}
     */
    @PostMapping("/import-tasks/{importTaskNo}/execute")
    public MdmImportQualityMapper.ImportTaskRow executeImportTask(@PathVariable String importTaskNo, @RequestBody MdmImportQualityApplicationService.StateCommand command) {
        if (fileTaskExecutionService != null) {
            fileTaskExecutionService.claimAndApplyValidatedRows(importTaskNo, command.expectedVersion());
            return service.getImportTask(importTaskNo);
        }
        return service.executeImportTask(importTaskNo, command);
    }

    /**
     * 执行命令 {@code cancelImportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmImportQualityApplicationService.CancelCommand}
     * @return 执行命令的结果，类型为 {@code MdmImportQualityMapper.ImportTaskRow}
     */
    @PostMapping("/import-tasks/{importTaskNo}/cancel")
    public MdmImportQualityMapper.ImportTaskRow cancelImportTask(@PathVariable String importTaskNo, @RequestBody MdmImportQualityApplicationService.CancelCommand command) {
        return service.cancelImportTask(importTaskNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code importTasks}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<MdmImportQualityMapper.ImportTaskRow>}
     */
    @GetMapping("/import-tasks")
    public List<MdmImportQualityMapper.ImportTaskRow> importTasks(@RequestParam(required = false) String typeCode, @RequestParam(required = false) Integer status) {
        return service.listImportTasks(typeCode, status);
    }

    /**
     * 处理当前类型职责中的操作 {@code importTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MdmImportQualityMapper.ImportTaskRow}
     */
    @GetMapping("/import-tasks/{importTaskNo}")
    public MdmImportQualityMapper.ImportTaskRow importTask(@PathVariable String importTaskNo) {
        return service.getImportTask(importTaskNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code importErrors}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<MdmImportQualityMapper.ImportErrorRow>}
     */
    @GetMapping("/import-tasks/{importTaskNo}/errors")
    public List<MdmImportQualityMapper.ImportErrorRow> importErrors(@PathVariable String importTaskNo) {
        return service.listImportErrors(importTaskNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code importTemplate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MdmImportQualityApplicationService.ImportTemplate}
     */
    @GetMapping("/import-templates/{typeCode}")
    public MdmImportQualityApplicationService.ImportTemplate importTemplate(@PathVariable String typeCode) {
        return service.template(typeCode);
    }

    /**
     * 执行命令 {@code createExportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code MdmImportQualityApplicationService.CreateExportTaskCommand}
     * @return 执行命令的结果，类型为 {@code MdmImportQualityMapper.ExportTaskRow}
     */
    @PostMapping("/records/export")
    @org.springframework.security.access.prepost.PreAuthorize(
        "hasAnyAuthority('*', 'mdm:*', 'master-data:importexport:export')")
    public MdmImportQualityMapper.ExportTaskRow createExportTask(
            @RequestBody MdmImportQualityApplicationService.CreateExportTaskCommand command,
            Authentication authentication) {
        return service.createExportTask(command,
            ScmAccessContexts.require(authentication));
    }

    /**
     * 处理当前类型职责中的操作 {@code exports}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<MdmImportQualityMapper.ExportTaskRow>}
     */
    @GetMapping("/exports")
    public List<MdmImportQualityMapper.ExportTaskRow> exports() {
        return service.listExportTasks();
    }

    /**
     * 处理当前类型职责中的操作 {@code raiseIssue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code MdmImportQualityApplicationService.RaiseQualityIssueCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MdmImportQualityMapper.QualityIssueRow}
     */
    @PostMapping("/data-quality-issues")
    public MdmImportQualityMapper.QualityIssueRow raiseIssue(@RequestBody MdmImportQualityApplicationService.RaiseQualityIssueCommand command) {
        return service.raiseQualityIssue(command);
    }

    /**
     * 处理当前类型职责中的操作 {@code assignIssue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param issueNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmImportQualityApplicationService.AssignIssueCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MdmImportQualityMapper.QualityIssueRow}
     */
    @PostMapping("/data-quality-issues/{issueNo}/assign")
    public MdmImportQualityMapper.QualityIssueRow assignIssue(@PathVariable String issueNo, @RequestBody MdmImportQualityApplicationService.AssignIssueCommand command) {
        return service.assignQualityIssue(issueNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code fixIssue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param issueNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmImportQualityApplicationService.FixIssueCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MdmImportQualityMapper.QualityIssueRow}
     */
    @PostMapping("/data-quality-issues/{issueNo}/fix")
    public MdmImportQualityMapper.QualityIssueRow fixIssue(@PathVariable String issueNo, @RequestBody MdmImportQualityApplicationService.FixIssueCommand command) {
        return service.fixQualityIssue(issueNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code verifyIssue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param issueNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmImportQualityApplicationService.StateCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MdmImportQualityMapper.QualityIssueRow}
     */
    @PostMapping("/data-quality-issues/{issueNo}/verify")
    public MdmImportQualityMapper.QualityIssueRow verifyIssue(@PathVariable String issueNo, @RequestBody MdmImportQualityApplicationService.StateCommand command) {
        return service.verifyQualityIssue(issueNo, command);
    }

    /**
     * 执行命令 {@code closeIssue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param issueNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmImportQualityApplicationService.StateCommand}
     * @return 执行命令的结果，类型为 {@code MdmImportQualityMapper.QualityIssueRow}
     */
    @PostMapping("/data-quality-issues/{issueNo}/close")
    public MdmImportQualityMapper.QualityIssueRow closeIssue(@PathVariable String issueNo, @RequestBody MdmImportQualityApplicationService.StateCommand command) {
        return service.closeQualityIssue(issueNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code issues}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<MdmImportQualityMapper.QualityIssueRow>}
     */
    @GetMapping("/data-quality-issues")
    public List<MdmImportQualityMapper.QualityIssueRow> issues(@RequestParam(required = false) String typeCode, @RequestParam(required = false) Integer status) {
        return service.listQualityIssues(typeCode, status);
    }
}
