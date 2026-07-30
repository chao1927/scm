package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmApplicationService;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * MdmController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/mdm/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'mdm:*', 'mdm:model:manage')")
public class MdmController {

    /**
     * service（类型：{@code MdmApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final MdmApplicationService service;

    /**
     * 创建 MdmController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code MdmApplicationService}
     */
    public MdmController(MdmApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code createType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code MdmApplicationService.CreateType}
     * @return 执行命令的结果，类型为 {@code MdmMapper.TypeRow}
     */
    @PostMapping("/master-data-types")
    public MdmMapper.TypeRow createType(@RequestBody MdmApplicationService.CreateType command) {
        return service.createType(command);
    }

    /**
     * 执行命令 {@code enableType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmApplicationService.OperatorCommand}
     * @return 执行命令的结果，类型为 {@code MdmMapper.TypeRow}
     */
    @PostMapping("/master-data-types/{typeCode}/enable")
    public MdmMapper.TypeRow enableType(@PathVariable String typeCode, @RequestBody MdmApplicationService.OperatorCommand command) {
        return service.enableType(typeCode, command);
    }

    /**
     * 执行命令 {@code disableType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmApplicationService.ReasonCommand}
     * @return 执行命令的结果，类型为 {@code MdmMapper.TypeRow}
     */
    @PostMapping("/master-data-types/{typeCode}/disable")
    public MdmMapper.TypeRow disableType(@PathVariable String typeCode, @RequestBody MdmApplicationService.ReasonCommand command) {
        return service.disableType(typeCode, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code types}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<MdmMapper.TypeRow>}
     */
    @GetMapping("/master-data-types")
    public List<MdmMapper.TypeRow> types() {
        return service.listTypes();
    }

    /**
     * 执行命令 {@code createTemplate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code MdmApplicationService.CreateTemplate}
     * @return 执行命令的结果，类型为 {@code MdmMapper.TemplateRow}
     */
    @PostMapping("/field-templates")
    public MdmMapper.TemplateRow createTemplate(@RequestBody MdmApplicationService.CreateTemplate command) {
        return service.createTemplate(command);
    }

    /**
     * 执行命令 {@code publishTemplate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param templateCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmApplicationService.OperatorCommand}
     * @return 执行命令的结果，类型为 {@code MdmMapper.TemplateRow}
     */
    @PostMapping("/field-templates/{templateCode}/publish")
    public MdmMapper.TemplateRow publishTemplate(@PathVariable String templateCode, @RequestBody MdmApplicationService.OperatorCommand command) {
        return service.publishTemplate(templateCode, command);
    }

    /**
     * 执行命令 {@code disableTemplate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param templateCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmApplicationService.ReasonCommand}
     * @return 执行命令的结果，类型为 {@code MdmMapper.TemplateRow}
     */
    @PostMapping("/field-templates/{templateCode}/disable")
    public MdmMapper.TemplateRow disableTemplate(@PathVariable String templateCode, @RequestBody MdmApplicationService.ReasonCommand command) {
        return service.disableTemplate(templateCode, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code templates}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<MdmMapper.TemplateRow>}
     */
    @GetMapping("/field-templates")
    public List<MdmMapper.TemplateRow> templates() {
        return service.listTemplates();
    }

    /**
     * 执行命令 {@code createCodeRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code MdmApplicationService.CreateCodeRule}
     * @return 执行命令的结果，类型为 {@code MdmMapper.CodeRuleRow}
     */
    @PostMapping("/code-rules")
    public MdmMapper.CodeRuleRow createCodeRule(@RequestBody MdmApplicationService.CreateCodeRule command) {
        return service.createCodeRule(command);
    }

    /**
     * 执行命令 {@code enableCodeRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ruleCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmApplicationService.OperatorCommand}
     * @return 执行命令的结果，类型为 {@code MdmMapper.CodeRuleRow}
     */
    @PostMapping("/code-rules/{ruleCode}/enable")
    public MdmMapper.CodeRuleRow enableCodeRule(@PathVariable String ruleCode, @RequestBody MdmApplicationService.OperatorCommand command) {
        return service.enableCodeRule(ruleCode, command);
    }

    /**
     * 执行命令 {@code disableCodeRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ruleCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmApplicationService.ReasonCommand}
     * @return 执行命令的结果，类型为 {@code MdmMapper.CodeRuleRow}
     */
    @PostMapping("/code-rules/{ruleCode}/disable")
    public MdmMapper.CodeRuleRow disableCodeRule(@PathVariable String ruleCode, @RequestBody MdmApplicationService.ReasonCommand command) {
        return service.disableCodeRule(ruleCode, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code generateCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ruleCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmApplicationService.OperatorCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MdmApplicationService.GeneratedCode}
     */
    @PostMapping("/code-rules/{ruleCode}/generate")
    public MdmApplicationService.GeneratedCode generateCode(@PathVariable String ruleCode, @RequestBody MdmApplicationService.OperatorCommand command) {
        return service.generateCode(ruleCode, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code codeRules}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<MdmMapper.CodeRuleRow>}
     */
    @GetMapping("/code-rules")
    public List<MdmMapper.CodeRuleRow> codeRules() {
        return service.listCodeRules();
    }
}
