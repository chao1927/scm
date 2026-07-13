package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.iam.application.IamAdminApplicationService;
import com.chaobo.scm.iam.infrastructure.persistence.IamAdminMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/iam/v1")
public class IamAdminController {
    private final IamAdminApplicationService service;

    public IamAdminController(IamAdminApplicationService service) {
        this.service = service;
    }

    @PostMapping("/apps")
    public IamAdminMapper.AppRow createApp(@RequestBody IamAdminApplicationService.CreateAppCommand command) {
        return service.createApp(command);
    }

    @PutMapping("/apps/{appCode}")
    public IamAdminMapper.AppRow changeApp(@PathVariable String appCode,
                                           @RequestBody IamAdminApplicationService.ChangeAppCommand command) {
        return service.changeApp(appCode, command);
    }

    @PostMapping("/apps/{appCode}/toggle")
    public IamAdminMapper.AppRow toggleApp(@PathVariable String appCode,
                                           @RequestBody IamAdminApplicationService.ToggleCommand command) {
        return service.toggleApp(appCode, command);
    }

    @GetMapping("/apps")
    public List<IamAdminMapper.AppRow> apps() {
        return service.listApps();
    }

    @PostMapping("/menus")
    public IamAdminMapper.MenuRow createMenu(@RequestBody IamAdminApplicationService.CreateMenuCommand command) {
        return service.createMenu(command);
    }

    @PostMapping("/menus/{menuCode}/disable")
    public IamAdminMapper.MenuRow disableMenu(@PathVariable String menuCode,
                                              @RequestBody IamAdminApplicationService.DisableCommand command) {
        return service.disableMenu(menuCode, command);
    }

    @GetMapping("/menus")
    public List<IamAdminMapper.MenuRow> menus(@RequestParam String appCode) {
        return service.listMenus(appCode);
    }

    @PostMapping("/sso-clients")
    public IamAdminApplicationService.SsoSecret configureSso(@RequestBody IamAdminApplicationService.ConfigureSsoCommand command) {
        return service.configureSso(command);
    }

    @PostMapping("/sso-clients/{ssoCode}/reset-secret")
    public IamAdminApplicationService.SsoSecret resetSsoSecret(@PathVariable String ssoCode,
                                                               @RequestBody IamAdminApplicationService.ResetSsoSecretCommand command) {
        return service.resetSsoSecret(ssoCode, command);
    }

    @GetMapping("/sso-clients")
    public List<IamAdminMapper.SsoRow> ssoClients() {
        return service.listSso();
    }
}
