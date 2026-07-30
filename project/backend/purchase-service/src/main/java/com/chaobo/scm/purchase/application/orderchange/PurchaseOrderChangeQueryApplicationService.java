package com.chaobo.scm.purchase.application.orderchange;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PurchaseOrderChangeQueryApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class PurchaseOrderChangeQueryApplicationService {

    /**
     * readModel（类型：{@code PurchaseOrderChangeReadModelPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseOrderChangeReadModelPort readModel;

    /**
     * 创建 PurchaseOrderChangeQueryApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param readModel 业务处理参数或成员，类型为 {@code PurchaseOrderChangeReadModelPort}
     */
    public PurchaseOrderChangeQueryApplicationService(PurchaseOrderChangeReadModelPort readModel) {
        this.readModel = readModel;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<PurchaseOrderChangeView>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<PurchaseOrderChangeView> page(String orderNo, Integer status, int pageNo, int pageSize) {
        if (pageNo < 1 || pageSize < 1 || pageSize > PAGE_VALUE_100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        return readModel.page(orderNo, status, pageNo, pageSize);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param changeNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchaseOrderChangeView}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PurchaseOrderChangeView detail(String changeNo) {
        return readModel.detail(changeNo).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购订单变更单不存在"));
    }

    /**
     * 业务常量 {@code PAGE_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PAGE_VALUE_100 = 100;
}
