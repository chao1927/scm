package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.domain.MasterDataRecordAggregate;
import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmOpenApiMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmPublicationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MdmOpenApiApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class MdmOpenApiApplicationService {

    /**
     * recordMapper（类型：{@code MasterDataRecordMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataRecordMapper recordMapper;

    /**
     * mapper（类型：{@code MdmOpenApiMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final MdmOpenApiMapper mapper;

    /**
     * publicationService（类型：{@code MdmPublicationApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final MdmPublicationApplicationService publicationService;

    /**
     * qualityService（类型：{@code MdmImportQualityApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final MdmImportQualityApplicationService qualityService;

    /**
     * 创建 MdmOpenApiApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param recordMapper 持久化访问依赖，类型为 {@code MasterDataRecordMapper}
     * @param mapper 持久化访问依赖，类型为 {@code MdmOpenApiMapper}
     * @param publicationService 应用或外部协作依赖，类型为 {@code MdmPublicationApplicationService}
     * @param qualityService 应用或外部协作依赖，类型为 {@code MdmImportQualityApplicationService}
     */
    public MdmOpenApiApplicationService(MasterDataRecordMapper recordMapper, MdmOpenApiMapper mapper, MdmPublicationApplicationService publicationService, MdmImportQualityApplicationService qualityService) {
        this.recordMapper = recordMapper;
        this.mapper = mapper;
        this.publicationService = publicationService;
        this.qualityService = qualityService;
    }

    /**
     * 查询并返回 {@code query}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param request 接口请求参数，类型为 {@code QueryRequest}
     * @return 查询并返回的结果，类型为 {@code QueryResponse}
     */
    public QueryResponse query(QueryRequest request) {
        List<Snapshot> snapshots = request.items().stream().map(item -> snapshot(item.typeCode(), item.dataCode(), true)).toList();
        return new QueryResponse(snapshots);
    }

    /**
     * 校验业务约束 {@code validate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param request 接口请求参数，类型为 {@code ValidateRequest}
     * @return 校验业务约束的结果，类型为 {@code ValidateResponse}
     */
    public ValidateResponse validate(ValidateRequest request) {
        List<ValidateItemResult> results = request.items().stream().map(this::validateOne).toList();
        boolean valid = results.stream().allMatch(ValidateItemResult::valid);
        return new ValidateResponse(valid, results);
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param dataCode 可追踪业务编码，类型为 {@code String}
     * @param includeDisabled 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Snapshot}
     */
    public Snapshot snapshot(String typeCode, String dataCode, boolean includeDisabled) {
        MasterDataRecordMapper.RecordRow row = recordMapper.findRecordByCode(typeCode, dataCode);
        if (row == null) {
            throw new IllegalArgumentException("master data snapshot not found");
        }
        if (!includeDisabled && row.status() != MasterDataRecordAggregate.ENABLED) {
            throw new IllegalStateException("master data is not enabled");
        }
        return new Snapshot(row.recordNo(), row.typeCode(), row.dataCode(), row.dataName(), row.dataPayload(), row.status(), row.currentVersionNo(), row.version());
    }

    /**
     * 执行命令 {@code consumeEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code EventEnvelope}
     * @return 执行命令的结果，类型为 {@code ConsumeResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsumeResult consumeEvent(EventEnvelope event) {
        if (PUBLISH_RECEIPT_RETURNED.equals(event.eventType())) {
            publicationService.consumeReceipt(new MdmPublicationApplicationService.ReceiptEvent(event.eventId(), event.eventType(), event.publicationNo(), event.receiptStatus(), event.failureReason(), event.payload()));
            return new ConsumeResult(event.eventId(), "SUCCESS", false, "receipt consumed");
        }
        int claimed = mapper.claimEvent(new MdmPublicationMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessKey(), event.payload(), 1, null));
        if (claimed == 0) {
            return new ConsumeResult(event.eventId(), "DUPLICATE", true, "idempotent hit");
        }
        try {
            if (SUPPLIER_PROFILE_CHANGE_SUBMITTED.equals(event.eventType()) || CARRIER_SERVICE_CONFIRMED.equals(event.eventType())) {
                qualityService.raiseQualityIssue(new MdmImportQualityApplicationService.RaiseQualityIssueCommand(event.typeCode(), event.dataCode(), event.eventType(), event.failureReason() == null ? event.payload() : event.failureReason(), null, event.idempotencyKey()));
            } else if (!supportedIgnored(event.eventType())) {
                mapper.updateEvent(new MdmPublicationMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessKey(), event.payload(), 4, "unsupported event type"));
                return new ConsumeResult(event.eventId(), "IGNORED", false, "unsupported event type");
            }
            mapper.insertOutbox(new MdmMapper.OutboxRow("MdmExternalEventConsumed", event.businessKey(), event.eventType(), 1, LocalDateTime.now()));
            mapper.updateEvent(new MdmPublicationMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessKey(), event.payload(), 2, null));
            return new ConsumeResult(event.eventId(), "SUCCESS", false, "consumed");
        } catch (RuntimeException exception) {
            mapper.updateEvent(new MdmPublicationMapper.EventInboxRow(event.eventId(), event.eventType(), event.businessKey(), event.payload(), 3, exception.getMessage()));
            throw exception;
        }
    }

    /**
     * 查询并返回 {@code listInboxEvents}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmPublicationMapper.EventInboxRow>}
     */
    public List<MdmPublicationMapper.EventInboxRow> listInboxEvents() {
        return mapper.listInboxEvents();
    }

    /**
     * 校验业务约束 {@code validateOne}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param item 业务处理参数或成员，类型为 {@code ValidateItem}
     * @return 校验业务约束的结果，类型为 {@code ValidateItemResult}
     */
    private ValidateItemResult validateOne(ValidateItem item) {
        MasterDataRecordMapper.RecordRow row = recordMapper.findRecordByCode(item.typeCode(), item.dataCode());
        if (row == null) {
            return ValidateItemResult.failed(item, "NOT_FOUND", "master data does not exist", null);
        }
        if (item.expectedVersionNo() != null && row.currentVersionNo() != item.expectedVersionNo()) {
            return ValidateItemResult.failed(item, "VERSION_MISMATCH", "master data version mismatch", toSnapshot(row));
        }
        int requiredStatus = item.requiredStatus() == null ? MasterDataRecordAggregate.ENABLED : item.requiredStatus();
        if (row.status() != requiredStatus) {
            return ValidateItemResult.failed(item, "STATUS_MISMATCH", "master data status mismatch", toSnapshot(row));
        }
        return new ValidateItemResult(item.businessKey(), item.typeCode(), item.dataCode(), true, null, null, toSnapshot(row));
    }

    /**
     * 转换数据模型 {@code toSnapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code MasterDataRecordMapper.RecordRow}
     * @return 转换数据模型的结果，类型为 {@code Snapshot}
     */
    private Snapshot toSnapshot(MasterDataRecordMapper.RecordRow row) {
        return new Snapshot(row.recordNo(), row.typeCode(), row.dataCode(), row.dataName(), row.dataPayload(), row.status(), row.currentVersionNo(), row.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code supportedIgnored}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private static boolean supportedIgnored(String eventType) {
        return "ApprovalApproved".equals(eventType) || "ApprovalRejected".equals(eventType) || "PermissionDataScopeChanged".equals(eventType) || "WarehouseExternalCodeBound".equals(eventType) || "BillingMasterDataReferenced".equals(eventType);
    }

    /**
     * QueryRequest。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record QueryRequest(List<QueryItem> items) {

        public QueryRequest {
            items = items == null ? List.of() : List.copyOf(items);
        }
    }

    /**
     * QueryItem。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record QueryItem(String typeCode, String dataCode) {
    }

    /**
     * QueryResponse。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record QueryResponse(List<Snapshot> items) {
    }

    /**
     * ValidateRequest。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ValidateRequest(String validateScene, List<ValidateItem> items) {

        public ValidateRequest {
            items = items == null ? List.of() : List.copyOf(items);
        }
    }

    /**
     * ValidateItem。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ValidateItem(String businessKey, String typeCode, String dataCode, Integer expectedVersionNo, Integer requiredStatus) {
    }

    /**
     * ValidateResponse。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ValidateResponse(boolean valid, List<ValidateItemResult> items) {
    }

    /**
     * ValidateItemResult。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ValidateItemResult(String businessKey, String typeCode, String dataCode, boolean valid, String failureCode, String failureReason, Snapshot snapshot) {

        /**
         * 处理当前类型职责中的操作 {@code failed}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param item 业务处理参数或成员，类型为 {@code ValidateItem}
         * @param failureCode 可追踪业务编码，类型为 {@code String}
         * @param failureReason 业务处理参数或成员，类型为 {@code String}
         * @param snapshot 业务处理参数或成员，类型为 {@code Snapshot}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code ValidateItemResult}
         */
        static ValidateItemResult failed(ValidateItem item, String failureCode, String failureReason, Snapshot snapshot) {
            return new ValidateItemResult(item.businessKey(), item.typeCode(), item.dataCode(), false, failureCode, failureReason, snapshot);
        }
    }

    /**
     * Snapshot。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Snapshot(String recordNo, String typeCode, String dataCode, String dataName, String dataPayload, int status, int currentVersionNo, long version) {
    }

    /**
     * EventEnvelope。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record EventEnvelope(String eventId, String eventType, String sourceSystem, String businessKey, String idempotencyKey, String payload, String publicationNo, String receiptStatus, String failureReason, String typeCode, String dataCode) {
    }

    /**
     * ConsumeResult。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ConsumeResult(String consumeId, String consumeStatus, boolean idempotentHit, String message) {
    }

    /**
     * 业务常量 {@code CARRIER_SERVICE_CONFIRMED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String CARRIER_SERVICE_CONFIRMED = "CarrierServiceConfirmed";

    /**
     * 业务常量 {@code SUPPLIER_PROFILE_CHANGE_SUBMITTED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String SUPPLIER_PROFILE_CHANGE_SUBMITTED = "SupplierProfileChangeSubmitted";

    /**
     * 业务常量 {@code PUBLISH_RECEIPT_RETURNED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String PUBLISH_RECEIPT_RETURNED = "PublishReceiptReturned";
}
