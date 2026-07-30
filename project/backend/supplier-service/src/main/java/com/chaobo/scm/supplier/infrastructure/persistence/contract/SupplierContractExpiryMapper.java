package com.chaobo.scm.supplier.infrastructure.persistence.contract;

import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * SupplierContractExpiryMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierContractExpiryMapper {

    /**
     * 处理当前类型职责中的操作 {@code expiredIds}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Long>}
     */
    @Select("SELECT contract_id FROM sup_supplier_contract WHERE contract_status=3 AND effective_to<CURDATE() AND deleted=0")
    List<Long> expiredIds();
}
