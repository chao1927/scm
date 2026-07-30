package com.chaobo.scm.wms.infrastructure.persistence.receiving;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.domain.receiving.ReceiptAggregate;
import com.chaobo.scm.wms.domain.receiving.ReceiptRepository;
import com.chaobo.scm.wms.domain.receiving.ReceiptStatus;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * MyBatisReceiptRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisReceiptRepository implements ReceiptRepository {

    /**
     * mapper（类型：{@code ReceiptMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final ReceiptMapper mapper;

    /**
     * 创建 MyBatisReceiptRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code ReceiptMapper}
     */
    public MyBatisReceiptRepository(ReceiptMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<ReceiptAggregate>}
     */
    public Optional<ReceiptAggregate> findByNo(String no) {
        return Optional.ofNullable(mapper.findByNo(no)).map(this::map);
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param receipt 业务处理参数或成员，类型为 {@code ReceiptAggregate}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    public void save(ReceiptAggregate receipt, long operator) {
        var current = mapper.findByNo(receipt.receiptNo());
        if (current == null) {
            mapper.insert(receipt.id(), receipt.receiptNo(), receipt.inboundId(), receipt.skuCode(), receipt.expectedQty(), receipt.receivedQty(), receipt.rejectedQty(), receipt.status().code(), receipt.version(), operator);
            return;
        }
        if (mapper.update(receipt.id(), receipt.receivedQty(), receipt.rejectedQty(), receipt.status().code(), receipt.version(), current.version(), operator) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "收货单已被其他人修改");
        }
    }

    /**
     * 转换数据模型 {@code map}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param r 业务处理参数或成员，类型为 {@code ReceiptMapper.Row}
     * @return 转换数据模型的结果，类型为 {@code ReceiptAggregate}
     */
    private ReceiptAggregate map(ReceiptMapper.Row r) {
        return new ReceiptAggregate(r.id(), r.receiptNo(), r.inboundId(), r.skuCode(), r.expectedQty(), r.receivedQty(), r.rejectedQty(), ReceiptStatus.values()[r.status() - 1], r.version());
    }
}
