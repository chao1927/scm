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

/**
 * IamAdminController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/iam/v1")
public class IamAdminController {

    /**
     * service（类型：{@code IamAdminApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final IamAdminApplicationService service;

    /**
     * 创建 IamAdminController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code IamAdminApplicationService}
     */
    public IamAdminController(IamAdminApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code createApp}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code IamAdminApplicationService.CreateAppCommand}
     * @return 执行命令的结果，类型为 {@code IamAdminMapper.AppRow}
     */
    @PostMapping("/apps")
    public IamAdminMapper.AppRow createApp(@RequestBody IamAdminApplicationService.CreateAppCommand command) {
        return service.createApp(command);
    }

    /**
     * 处理当前类型职责中的操作 {@code changeApp}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code IamAdminApplicationService.ChangeAppCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code IamAdminMapper.AppRow}
     */
    @PutMapping("/apps/{appCode}")
    public IamAdminMapper.AppRow changeApp(@PathVariable String appCode, @RequestBody IamAdminApplicationService.ChangeAppCommand command) {
        return service.changeApp(appCode, command);
    }

    /**
     * 转换数据模型 {@code toggleApp}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code IamAdminApplicationService.ToggleCommand}
     * @return 转换数据模型的结果，类型为 {@code IamAdminMapper.AppRow}
     */
    @PostMapping("/apps/{appCode}/toggle")
    public IamAdminMapper.AppRow toggleApp(@PathVariable String appCode, @RequestBody IamAdminApplicationService.ToggleCommand command) {
        return service.toggleApp(appCode, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code apps}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<IamAdminMapper.AppRow>}
     */
    @GetMapping("/apps")
    public List<IamAdminMapper.AppRow> apps() {
        return service.listApps();
    }

    /**
     * 执行命令 {@code createMenu}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code IamAdminApplicationService.CreateMenuCommand}
     * @return 执行命令的结果，类型为 {@code IamAdminMapper.MenuRow}
     */
    @PostMapping("/menus")
    public IamAdminMapper.MenuRow createMenu(@RequestBody IamAdminApplicationService.CreateMenuCommand command) {
        return service.createMenu(command);
    }

    /**
     * 执行命令 {@code disableMenu}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param menuCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code IamAdminApplicationService.DisableCommand}
     * @return 执行命令的结果，类型为 {@code IamAdminMapper.MenuRow}
     */
    @PostMapping("/menus/{menuCode}/disable")
    public IamAdminMapper.MenuRow disableMenu(@PathVariable String menuCode, @RequestBody IamAdminApplicationService.DisableCommand command) {
        return service.disableMenu(menuCode, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code menus}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<IamAdminMapper.MenuRow>}
     */
    @GetMapping("/menus")
    public List<IamAdminMapper.MenuRow> menus(@RequestParam String appCode) {
        return service.listMenus(appCode);
    }

    /**
     * 处理当前类型职责中的操作 {@code configureSso}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code IamAdminApplicationService.ConfigureSsoCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code IamAdminApplicationService.SsoSecret}
     */
    @PostMapping("/sso-clients")
    public IamAdminApplicationService.SsoSecret configureSso(@RequestBody IamAdminApplicationService.ConfigureSsoCommand command) {
        return service.configureSso(command);
    }

    /**
     * 处理当前类型职责中的操作 {@code resetSsoSecret}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ssoCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code IamAdminApplicationService.ResetSsoSecretCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code IamAdminApplicationService.SsoSecret}
     */
    @PostMapping("/sso-clients/{ssoCode}/reset-secret")
    public IamAdminApplicationService.SsoSecret resetSsoSecret(@PathVariable String ssoCode, @RequestBody IamAdminApplicationService.ResetSsoSecretCommand command) {
        return service.resetSsoSecret(ssoCode, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code ssoClients}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<IamAdminMapper.SsoRow>}
     */
    @GetMapping("/sso-clients")
    public List<IamAdminMapper.SsoRow> ssoClients() {
        return service.listSso();
    }
}
