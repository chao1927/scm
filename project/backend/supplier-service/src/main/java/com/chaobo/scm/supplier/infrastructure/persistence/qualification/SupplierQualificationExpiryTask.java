package com.chaobo.scm.supplier.infrastructure.persistence.qualification;

import com.chaobo.scm.supplier.application.qualification.SupplierQualificationApplicationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * SupplierQualificationExpiryTask。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class SupplierQualificationExpiryTask {

    /**
     * repository（类型：{@code MyBatisSupplierQualificationAdapter}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final MyBatisSupplierQualificationAdapter repository;

    /**
     * service（类型：{@code SupplierQualificationApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQualificationApplicationService service;

    /**
     * 创建 SupplierQualificationExpiryTask。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repository 持久化访问依赖，类型为 {@code MyBatisSupplierQualificationAdapter}
     * @param service 应用或外部协作依赖，类型为 {@code SupplierQualificationApplicationService}
     */
    public SupplierQualificationExpiryTask(MyBatisSupplierQualificationAdapter repository, SupplierQualificationApplicationService service) {
        this.repository = repository;
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code expire}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Scheduled(cron = "${scm.qualification.expire-cron:0 10 0 * * *}")
    public void expire() {
        for (long id : repository.expiredIds()) {
            service.expire(id);
        }
    }
}
