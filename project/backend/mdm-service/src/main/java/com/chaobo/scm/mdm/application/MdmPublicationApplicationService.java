package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.domain.MdmEvent;
import com.chaobo.scm.mdm.domain.PublicationAggregate;
import com.chaobo.scm.mdm.domain.PublicationSubscriptionAggregate;
import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmPublicationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MdmPublicationApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class MdmPublicationApplicationService {

    /**
     * mapper（类型：{@code MdmPublicationMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final MdmPublicationMapper mapper;

    /**
     * recordService（类型：{@code MasterDataRecordApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataRecordApplicationService recordService;

    /**
     * subscriptionSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong subscriptionSequence = new AtomicLong(300000);

    /**
     * publicationSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong publicationSequence = new AtomicLong(400000);

    /**
     * 创建 MdmPublicationApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code MdmPublicationMapper}
     * @param recordService 应用或外部协作依赖，类型为 {@code MasterDataRecordApplicationService}
     */
    public MdmPublicationApplicationService(MdmPublicationMapper mapper, MasterDataRecordApplicationService recordService) {
        this.mapper = mapper;
        this.recordService = recordService;
    }

    /**
     * 执行命令 {@code createSubscription}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateSubscriptionCommand}
     * @return 执行命令的结果，类型为 {@code MdmPublicationMapper.SubscriptionRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmPublicationMapper.SubscriptionRow createSubscription(CreateSubscriptionCommand command) {
        MdmPublicationMapper.SubscriptionRow existing = mapper.findActiveSubscription(command.typeCode(), command.targetSystem(), command.eventTopic());
        if (existing != null) {
            return existing;
        }
        PublicationSubscriptionAggregate aggregate = PublicationSubscriptionAggregate.create("SUB" + subscriptionSequence.incrementAndGet(), command.typeCode(), command.targetSystem(), command.eventTopic(), command.filterRule());
        MdmPublicationMapper.SubscriptionRow row = toRow(aggregate);
        mapper.insertSubscription(row);
        saveEvents(aggregate.pullEvents());
        log("CREATE_PUBLICATION_SUBSCRIPTION", row.subscriptionNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 执行命令 {@code disableSubscription}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param subscriptionNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code DisableSubscriptionCommand}
     * @return 执行命令的结果，类型为 {@code MdmPublicationMapper.SubscriptionRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmPublicationMapper.SubscriptionRow disableSubscription(String subscriptionNo, DisableSubscriptionCommand command) {
        PublicationSubscriptionAggregate aggregate = loadSubscription(subscriptionNo);
        aggregate.disable(command.reason(), command.expectedVersion());
        mapper.updateSubscription(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("DISABLE_PUBLICATION_SUBSCRIPTION", subscriptionNo, command.operatorId(), command.idempotencyKey());
        return mapper.findSubscription(subscriptionNo);
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code PublishCommand}
     * @return 执行命令的结果，类型为 {@code List<MdmPublicationMapper.PublicationRow>}
     */
    @Transactional(rollbackFor = Exception.class)
    public List<MdmPublicationMapper.PublicationRow> publish(PublishCommand command) {
        MasterDataRecordMapper.VersionRow version = recordService.getVersion(command.versionNo());
        if (version == null) {
            throw new IllegalArgumentException("master data version not found");
        }
        List<MdmPublicationMapper.SubscriptionRow> subscriptions = mapper.listActiveSubscriptions(version.typeCode());
        List<MdmPublicationMapper.PublicationRow> rows = new ArrayList<>();
        for (MdmPublicationMapper.SubscriptionRow subscription : subscriptions) {
            PublicationAggregate aggregate = PublicationAggregate.create("PUB" + publicationSequence.incrementAndGet(), version.versionNo(), version.typeCode(), version.dataCode(), subscription.targetSystem(), subscription.eventTopic());
            MdmPublicationMapper.PublicationRow row = toRow(aggregate);
            mapper.insertPublication(row);
            saveEvents(aggregate.pullEvents());
            rows.add(row);
        }
        log("PUBLISH_MASTER_DATA_VERSION", command.versionNo(), command.operatorId(), command.idempotencyKey());
        return rows;
    }

    /**
     * 执行命令 {@code retry}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param publicationNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code RetryCommand}
     * @return 执行命令的结果，类型为 {@code MdmPublicationMapper.PublicationRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmPublicationMapper.PublicationRow retry(String publicationNo, RetryCommand command) {
        PublicationAggregate aggregate = loadPublication(publicationNo);
        aggregate.retry(command.reason());
        mapper.updatePublication(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("RETRY_MASTER_DATA_PUBLICATION", publicationNo, command.operatorId(), command.idempotencyKey());
        return mapper.findPublication(publicationNo);
    }

    /**
     * 执行命令 {@code consumeReceipt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code ReceiptEvent}
     */
    @Transactional(rollbackFor = Exception.class)
    public void consumeReceipt(ReceiptEvent event) {
        int claimed = mapper.claimEvent(new MdmPublicationMapper.EventInboxRow(event.eventId(), event.eventType(), event.publicationNo(), event.payload(), 1, null));
        if (claimed == 0) {
            return;
        }
        try {
            PublicationAggregate aggregate = loadPublication(event.publicationNo());
            if (SUCCESS.equals(event.receiptStatus())) {
                aggregate.confirm();
            } else {
                aggregate.fail(event.failureReason());
            }
            mapper.updatePublication(toRow(aggregate));
            saveEvents(aggregate.pullEvents());
            mapper.updateEvent(new MdmPublicationMapper.EventInboxRow(event.eventId(), event.eventType(), event.publicationNo(), event.payload(), 2, null));
        } catch (RuntimeException exception) {
            mapper.updateEvent(new MdmPublicationMapper.EventInboxRow(event.eventId(), event.eventType(), event.publicationNo(), event.payload(), 3, exception.getMessage()));
            throw exception;
        }
    }

    /**
     * 查询并返回 {@code listSubscriptions}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmPublicationMapper.SubscriptionRow>}
     */
    public List<MdmPublicationMapper.SubscriptionRow> listSubscriptions() {
        return mapper.listSubscriptions();
    }

    /**
     * 查询并返回 {@code listPublications}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmPublicationMapper.PublicationRow>}
     */
    public List<MdmPublicationMapper.PublicationRow> listPublications() {
        return mapper.listPublications();
    }

    /**
     * 查询并返回 {@code listOutbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmMapper.OutboxRow>}
     */
    public List<MdmMapper.OutboxRow> listOutbox() {
        return mapper.listOutbox();
    }

    /**
     * 查询并返回 {@code loadSubscription}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param subscriptionNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code PublicationSubscriptionAggregate}
     */
    private PublicationSubscriptionAggregate loadSubscription(String subscriptionNo) {
        MdmPublicationMapper.SubscriptionRow row = mapper.findSubscription(subscriptionNo);
        if (row == null) {
            throw new IllegalArgumentException("publication subscription not found");
        }
        return PublicationSubscriptionAggregate.restore(row.subscriptionNo(), row.typeCode(), row.targetSystem(), row.eventTopic(), row.filterRule(), row.status(), row.version());
    }

    /**
     * 查询并返回 {@code loadPublication}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param publicationNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code PublicationAggregate}
     */
    private PublicationAggregate loadPublication(String publicationNo) {
        MdmPublicationMapper.PublicationRow row = mapper.findPublication(publicationNo);
        if (row == null) {
            throw new IllegalArgumentException("publication not found");
        }
        return PublicationAggregate.restore(row.publicationNo(), row.versionNo(), row.typeCode(), row.dataCode(), row.targetSystem(), row.eventTopic(), row.status(), row.retryCount(), row.failureReason(), row.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code PublicationSubscriptionAggregate}
     * @return 转换数据模型的结果，类型为 {@code MdmPublicationMapper.SubscriptionRow}
     */
    private MdmPublicationMapper.SubscriptionRow toRow(PublicationSubscriptionAggregate aggregate) {
        return new MdmPublicationMapper.SubscriptionRow(null, aggregate.subscriptionNo(), aggregate.typeCode(), aggregate.targetSystem(), aggregate.eventTopic(), aggregate.filterRule(), aggregate.status(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code PublicationAggregate}
     * @return 转换数据模型的结果，类型为 {@code MdmPublicationMapper.PublicationRow}
     */
    private MdmPublicationMapper.PublicationRow toRow(PublicationAggregate aggregate) {
        return new MdmPublicationMapper.PublicationRow(null, aggregate.publicationNo(), aggregate.versionNo(), aggregate.typeCode(), aggregate.dataCode(), aggregate.targetSystem(), aggregate.eventTopic(), aggregate.status(), aggregate.retryCount(), aggregate.failureReason(), aggregate.version());
    }

    /**
     * 执行命令 {@code saveEvents}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param events 业务处理参数或成员，类型为 {@code List<MdmEvent>}
     */
    private void saveEvents(List<MdmEvent> events) {
        for (MdmEvent event : events) {
            mapper.insertOutbox(new MdmMapper.OutboxRow(event.eventType(), event.businessNo(), event.payload(), 1, event.occurredAt()));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code log}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param operationType 业务处理参数或成员，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code Long}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     */
    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new MdmMapper.OperationLogRow(operationType, businessNo, operatorId, idempotencyKey, LocalDateTime.now()));
    }

    /**
     * CreateSubscriptionCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateSubscriptionCommand(String typeCode, String targetSystem, String eventTopic, String filterRule, Long operatorId, String idempotencyKey) {
    }

    /**
     * DisableSubscriptionCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record DisableSubscriptionCommand(String reason, long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * PublishCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record PublishCommand(String versionNo, Long operatorId, String idempotencyKey) {
    }

    /**
     * RetryCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RetryCommand(String reason, Long operatorId, String idempotencyKey) {
    }

    /**
     * ReceiptEvent。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。表达已经发生的业务事实，载荷用于跨事务或跨上下文可靠传播。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ReceiptEvent(String eventId, String eventType, String publicationNo, String receiptStatus, String failureReason, String payload) {
    }

    /**
     * 业务常量 {@code SUCCESS}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String SUCCESS = "SUCCESS";
}
