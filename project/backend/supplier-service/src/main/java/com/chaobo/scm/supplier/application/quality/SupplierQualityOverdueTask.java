package com.chaobo.scm.supplier.application.quality;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * SupplierQualityOverdueTask。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class SupplierQualityOverdueTask {

    /**
     * service（类型：{@code SupplierQualityIssueApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQualityIssueApplicationService service;

    /**
     * 创建 SupplierQualityOverdueTask。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code SupplierQualityIssueApplicationService}
     */
    public SupplierQualityOverdueTask(SupplierQualityIssueApplicationService service) {
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code mark}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Scheduled(cron = "${scm.quality.overdue-cron:0 30 0 * * *}")
    public void mark() {
        service.markOverdue();
    }
}
