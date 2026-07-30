package com.chaobo.scm.supplier.application.contract;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

/**
 * PriceAgreementQueryApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class PriceAgreementQueryApplicationService {

    /**
     * read（类型：{@code PriceAgreementReadModelPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PriceAgreementReadModelPort read;

    /**
     * 创建 PriceAgreementQueryApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param read 业务处理参数或成员，类型为 {@code PriceAgreementReadModelPort}
     */
    public PriceAgreementQueryApplicationService(PriceAgreementReadModelPort read) {
        this.read = read;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<PriceAgreementView>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<PriceAgreementView> page(Long supplierId, Long scope, String skuCode, int pageNo, int pageSize) {
        check(pageNo, pageSize);
        return read.page(scope == null ? supplierId : scope, skuCode, pageNo, pageSize);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PriceAgreementView}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PriceAgreementView detail(long id, Long scope) {
        var value = read.detail(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "价格协议不存在"));
        if (scope != null && scope != value.supplierId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "价格协议不存在");
        }
        return value;
    }

    /**
     * 校验业务约束 {@code validate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param date 业务时间，类型为 {@code LocalDate}
     * @return 校验业务约束的结果，类型为 {@code PriceAgreementView.Line}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PriceAgreementView.Line validate(long supplierId, String skuCode, String currency, LocalDate date) {
        return read.activeLine(supplierId, skuCode, currency, date == null ? LocalDate.now() : date).orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "不存在有效价格协议或价格协议行"));
    }

    /**
     * 校验业务约束 {@code check}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param page 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     */
    private static void check(int page, int size) {
        if (page < 1 || size < 1 || size > CHECK_VALUE_100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
    }

    /**
     * 业务常量 {@code CHECK_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int CHECK_VALUE_100 = 100;
}
