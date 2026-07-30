package com.chaobo.scm.supplier.application.masterdata;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 失败记录必须与快照更新事务隔离，否则原事务回滚会同时抹掉失败状态。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class MasterDataEventFailureApplicationService {

    /**
     * consumeLog（类型：{@code MasterDataEventConsumeLogPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataEventConsumeLogPort consumeLog;

    /**
     * 创建 MasterDataEventFailureApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param consumeLog 业务处理参数或成员，类型为 {@code MasterDataEventConsumeLogPort}
     */
    public MasterDataEventFailureApplicationService(MasterDataEventConsumeLogPort consumeLog) {
        this.consumeLog = consumeLog;
    }

    /**
     * 处理当前类型职责中的操作 {@code recordFailure}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code MasterDataEvent}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void recordFailure(MasterDataEvent event, String reason) {
        String value = reason == null ? "未知错误" : reason.substring(0, Math.min(reason.length(), 1000));
        consumeLog.recordFailure(event.sourceSystem(), event.eventCode(), event.eventType(), MasterDataEventConsumerApplicationService.CONSUMER_NAME, event.sourceSystem() + ":" + event.eventCode(), value);
    }
}
