package com.chaobo.scm.supplier.infrastructure.persistence.profile;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.domain.profile.*;
import org.springframework.stereotype.Repository;
import java.util.*;

/**
 * MyBatisSupplierAdmissionRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisSupplierAdmissionRepository implements SupplierAdmissionRepository {

    /**
     * m（类型：{@code SupplierAdmissionMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierAdmissionMapper m;

    /**
     * 创建 MyBatisSupplierAdmissionRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param m 业务处理参数或成员，类型为 {@code SupplierAdmissionMapper}
     */
    public MyBatisSupplierAdmissionRepository(SupplierAdmissionMapper m) {
        this.m = m;
    }

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<SupplierAdmissionAggregate>}
     */
    public Optional<SupplierAdmissionAggregate> findById(long id) {
        var r = m.find(id);
        return r == null ? Optional.empty() : Optional.of(SupplierAdmissionAggregate.rehydrate(r.id(), r.no(), r.code(), r.name(), r.taxNo(), r.type(), r.contact(), r.mobile(), r.settlement(), r.status(), r.reject(), r.version()));
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param a 业务处理参数或成员，类型为 {@code SupplierAdmissionAggregate}
     * @param op 业务处理参数或成员，类型为 {@code long}
     */
    public void save(SupplierAdmissionAggregate a, long op) {
        var r = new SupplierAdmissionMapper.Row(a.id(), a.no(), a.code(), a.name(), a.taxNo(), a.type(), a.contactName(), a.contactMobile(), a.settlementJson(), a.status().code(), a.rejectReason(), a.version());
        if (m.find(a.id()) == null) {
            m.insert(r, op);
        } else if (m.update(r, a.version() - 1, op) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "准入单已更新");
        }
    }
}
