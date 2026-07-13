package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.iam.application.IamPermissionOpenApiApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/openapi/iam/v1")
public class IamPermissionOpenApiController {
    private final IamPermissionOpenApiApplicationService service;

    public IamPermissionOpenApiController(IamPermissionOpenApiApplicationService service) {
        this.service = service;
    }

    @PostMapping("/tokens/validate")
    public IamPermissionOpenApiApplicationService.TokenValidationResult validate(@RequestBody IamPermissionOpenApiApplicationService.TokenValidationCommand command) {
        return service.validateToken(command);
    }

    @GetMapping("/users/me/permissions")
    public IamPermissionOpenApiApplicationService.PermissionSnapshot permissions(@RequestHeader("Authorization") String authorization,
                                                                                @RequestHeader(value = "X-App-Code", required = false) String appCode) {
        return service.snapshot(authorization.replace("Bearer ", ""), appCode);
    }

    @PostMapping("/permissions/check")
    public IamPermissionOpenApiApplicationService.PermissionCheckResult check(@RequestBody IamPermissionOpenApiApplicationService.PermissionCheckCommand command) {
        return service.checkPermission(command);
    }

    @PostMapping("/data-scopes/resolve")
    public IamPermissionOpenApiApplicationService.DataScopeResolveResult resolve(@RequestBody IamPermissionOpenApiApplicationService.DataScopeResolveCommand command) {
        return service.resolveDataScope(command);
    }
}
