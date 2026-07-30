package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmPublicationApplicationService;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmPublicationMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * MdmPublicationController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/mdm/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'mdm:*', 'mdm:publication:manage')")
public class MdmPublicationController {

    /**
     * service（类型：{@code MdmPublicationApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final MdmPublicationApplicationService service;

    /**
     * 创建 MdmPublicationController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code MdmPublicationApplicationService}
     */
    public MdmPublicationController(MdmPublicationApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code createSubscription}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code MdmPublicationApplicationService.CreateSubscriptionCommand}
     * @return 执行命令的结果，类型为 {@code MdmPublicationMapper.SubscriptionRow}
     */
    @PostMapping("/publication-subscriptions")
    public MdmPublicationMapper.SubscriptionRow createSubscription(@RequestBody MdmPublicationApplicationService.CreateSubscriptionCommand command) {
        return service.createSubscription(command);
    }

    /**
     * 执行命令 {@code disableSubscription}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param subscriptionNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmPublicationApplicationService.DisableSubscriptionCommand}
     * @return 执行命令的结果，类型为 {@code MdmPublicationMapper.SubscriptionRow}
     */
    @PostMapping("/publication-subscriptions/{subscriptionNo}/disable")
    public MdmPublicationMapper.SubscriptionRow disableSubscription(@PathVariable String subscriptionNo, @RequestBody MdmPublicationApplicationService.DisableSubscriptionCommand command) {
        return service.disableSubscription(subscriptionNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code subscriptions}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<MdmPublicationMapper.SubscriptionRow>}
     */
    @GetMapping("/publication-subscriptions")
    public List<MdmPublicationMapper.SubscriptionRow> subscriptions() {
        return service.listSubscriptions();
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code MdmPublicationApplicationService.PublishCommand}
     * @return 执行命令的结果，类型为 {@code List<MdmPublicationMapper.PublicationRow>}
     */
    @PostMapping("/publications")
    public List<MdmPublicationMapper.PublicationRow> publish(@RequestBody MdmPublicationApplicationService.PublishCommand command) {
        return service.publish(command);
    }

    /**
     * 执行命令 {@code retry}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param publicationNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code MdmPublicationApplicationService.RetryCommand}
     * @return 执行命令的结果，类型为 {@code MdmPublicationMapper.PublicationRow}
     */
    @PostMapping("/publications/{publicationNo}/retry")
    public MdmPublicationMapper.PublicationRow retry(@PathVariable String publicationNo, @RequestBody MdmPublicationApplicationService.RetryCommand command) {
        return service.retry(publicationNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code publications}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<MdmPublicationMapper.PublicationRow>}
     */
    @GetMapping("/publications")
    public List<MdmPublicationMapper.PublicationRow> publications() {
        return service.listPublications();
    }
}
