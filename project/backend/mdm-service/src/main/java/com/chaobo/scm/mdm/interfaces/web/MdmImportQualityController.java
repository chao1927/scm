package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmImportQualityApplicationService;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmImportQualityMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mdm/v1")
public class MdmImportQualityController {
    private final MdmImportQualityApplicationService service;

    public MdmImportQualityController(MdmImportQualityApplicationService service) {
        this.service = service;
    }

    @PostMapping("/import-tasks")
    public MdmImportQualityMapper.ImportTaskRow createImportTask(@RequestBody MdmImportQualityApplicationService.CreateImportTaskCommand command) {
        return service.createImportTask(command);
    }

    @PostMapping("/import-tasks/{importTaskNo}/validate")
    public MdmImportQualityMapper.ImportTaskRow validateImportTask(@PathVariable String importTaskNo,
                                                                   @RequestBody MdmImportQualityApplicationService.ValidateImportTaskCommand command) {
        return service.validateImportTask(importTaskNo, command);
    }

    @PostMapping("/import-tasks/{importTaskNo}/execute")
    public MdmImportQualityMapper.ImportTaskRow executeImportTask(@PathVariable String importTaskNo,
                                                                  @RequestBody MdmImportQualityApplicationService.StateCommand command) {
        return service.executeImportTask(importTaskNo, command);
    }

    @PostMapping("/import-tasks/{importTaskNo}/cancel")
    public MdmImportQualityMapper.ImportTaskRow cancelImportTask(@PathVariable String importTaskNo,
                                                                 @RequestBody MdmImportQualityApplicationService.CancelCommand command) {
        return service.cancelImportTask(importTaskNo, command);
    }

    @GetMapping("/import-tasks")
    public List<MdmImportQualityMapper.ImportTaskRow> importTasks(@RequestParam(required = false) String typeCode,
                                                                  @RequestParam(required = false) Integer status) {
        return service.listImportTasks(typeCode, status);
    }

    @GetMapping("/import-tasks/{importTaskNo}")
    public MdmImportQualityMapper.ImportTaskRow importTask(@PathVariable String importTaskNo) {
        return service.getImportTask(importTaskNo);
    }

    @GetMapping("/import-tasks/{importTaskNo}/errors")
    public List<MdmImportQualityMapper.ImportErrorRow> importErrors(@PathVariable String importTaskNo) {
        return service.listImportErrors(importTaskNo);
    }

    @GetMapping("/import-templates/{typeCode}")
    public MdmImportQualityApplicationService.ImportTemplate importTemplate(@PathVariable String typeCode) {
        return service.template(typeCode);
    }

    @PostMapping("/records/export")
    public MdmImportQualityMapper.ExportTaskRow createExportTask(@RequestBody MdmImportQualityApplicationService.CreateExportTaskCommand command) {
        return service.createExportTask(command);
    }

    @GetMapping("/exports")
    public List<MdmImportQualityMapper.ExportTaskRow> exports() {
        return service.listExportTasks();
    }

    @PostMapping("/data-quality-issues")
    public MdmImportQualityMapper.QualityIssueRow raiseIssue(@RequestBody MdmImportQualityApplicationService.RaiseQualityIssueCommand command) {
        return service.raiseQualityIssue(command);
    }

    @PostMapping("/data-quality-issues/{issueNo}/assign")
    public MdmImportQualityMapper.QualityIssueRow assignIssue(@PathVariable String issueNo,
                                                              @RequestBody MdmImportQualityApplicationService.AssignIssueCommand command) {
        return service.assignQualityIssue(issueNo, command);
    }

    @PostMapping("/data-quality-issues/{issueNo}/fix")
    public MdmImportQualityMapper.QualityIssueRow fixIssue(@PathVariable String issueNo,
                                                           @RequestBody MdmImportQualityApplicationService.FixIssueCommand command) {
        return service.fixQualityIssue(issueNo, command);
    }

    @PostMapping("/data-quality-issues/{issueNo}/verify")
    public MdmImportQualityMapper.QualityIssueRow verifyIssue(@PathVariable String issueNo,
                                                              @RequestBody MdmImportQualityApplicationService.StateCommand command) {
        return service.verifyQualityIssue(issueNo, command);
    }

    @PostMapping("/data-quality-issues/{issueNo}/close")
    public MdmImportQualityMapper.QualityIssueRow closeIssue(@PathVariable String issueNo,
                                                             @RequestBody MdmImportQualityApplicationService.StateCommand command) {
        return service.closeQualityIssue(issueNo, command);
    }

    @GetMapping("/data-quality-issues")
    public List<MdmImportQualityMapper.QualityIssueRow> issues(@RequestParam(required = false) String typeCode,
                                                               @RequestParam(required = false) Integer status) {
        return service.listQualityIssues(typeCode, status);
    }
}
