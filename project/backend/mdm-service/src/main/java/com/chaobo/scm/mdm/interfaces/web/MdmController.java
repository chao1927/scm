package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmApplicationService;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mdm/v1")
public class MdmController {
    private final MdmApplicationService service;

    public MdmController(MdmApplicationService service) {
        this.service = service;
    }

    @PostMapping("/master-data-types")
    public MdmMapper.TypeRow createType(@RequestBody MdmApplicationService.CreateType command) {
        return service.createType(command);
    }

    @PostMapping("/master-data-types/{typeCode}/enable")
    public MdmMapper.TypeRow enableType(@PathVariable String typeCode, @RequestBody MdmApplicationService.OperatorCommand command) {
        return service.enableType(typeCode, command);
    }

    @PostMapping("/master-data-types/{typeCode}/disable")
    public MdmMapper.TypeRow disableType(@PathVariable String typeCode, @RequestBody MdmApplicationService.ReasonCommand command) {
        return service.disableType(typeCode, command);
    }

    @GetMapping("/master-data-types")
    public List<MdmMapper.TypeRow> types() {
        return service.listTypes();
    }

    @PostMapping("/field-templates")
    public MdmMapper.TemplateRow createTemplate(@RequestBody MdmApplicationService.CreateTemplate command) {
        return service.createTemplate(command);
    }

    @PostMapping("/field-templates/{templateCode}/publish")
    public MdmMapper.TemplateRow publishTemplate(@PathVariable String templateCode, @RequestBody MdmApplicationService.OperatorCommand command) {
        return service.publishTemplate(templateCode, command);
    }

    @PostMapping("/field-templates/{templateCode}/disable")
    public MdmMapper.TemplateRow disableTemplate(@PathVariable String templateCode, @RequestBody MdmApplicationService.ReasonCommand command) {
        return service.disableTemplate(templateCode, command);
    }

    @GetMapping("/field-templates")
    public List<MdmMapper.TemplateRow> templates() {
        return service.listTemplates();
    }

    @PostMapping("/code-rules")
    public MdmMapper.CodeRuleRow createCodeRule(@RequestBody MdmApplicationService.CreateCodeRule command) {
        return service.createCodeRule(command);
    }

    @PostMapping("/code-rules/{ruleCode}/enable")
    public MdmMapper.CodeRuleRow enableCodeRule(@PathVariable String ruleCode, @RequestBody MdmApplicationService.OperatorCommand command) {
        return service.enableCodeRule(ruleCode, command);
    }

    @PostMapping("/code-rules/{ruleCode}/disable")
    public MdmMapper.CodeRuleRow disableCodeRule(@PathVariable String ruleCode, @RequestBody MdmApplicationService.ReasonCommand command) {
        return service.disableCodeRule(ruleCode, command);
    }

    @PostMapping("/code-rules/{ruleCode}/generate")
    public MdmApplicationService.GeneratedCode generateCode(@PathVariable String ruleCode, @RequestBody MdmApplicationService.OperatorCommand command) {
        return service.generateCode(ruleCode, command);
    }

    @GetMapping("/code-rules")
    public List<MdmMapper.CodeRuleRow> codeRules() {
        return service.listCodeRules();
    }
}
