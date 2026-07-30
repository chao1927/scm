package com.chaobo.scm.wms.application.receiving;

import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.domain.receiving.ReceiptAggregate;
import com.chaobo.scm.wms.domain.receiving.ReceiptRepository;
import com.chaobo.scm.wms.domain.receiving.ReceiptStatus;
import com.chaobo.scm.wms.infrastructure.persistence.receiving.ReceiptScanMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ReceivingApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class ReceivingApplicationService {

    /**
     * receipts（类型：{@code ReceiptRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ReceiptRepository receipts;

    /**
     * events（类型：{@code WmsEventPublisher}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final WmsEventPublisher events;

    /**
     * scans（类型：{@code ReceiptScanMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ReceiptScanMapper scans;

    /**
     * ids（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    /**
     * 创建 ReceivingApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param receipts 业务处理参数或成员，类型为 {@code ReceiptRepository}
     * @param events 业务处理参数或成员，类型为 {@code WmsEventPublisher}
     * @param scans 业务处理参数或成员，类型为 {@code ReceiptScanMapper}
     */
    public ReceivingApplicationService(ReceiptRepository receipts, WmsEventPublisher events, ReceiptScanMapper scans) {
        this.receipts = receipts;
        this.events = events;
        this.scans = scans;
    }

    /**
     * 处理当前类型职责中的操作 {@code open}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code Open}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result open(Open command, long operator) {
        var existed = receipts.findByNo(command.receiptNo());
        if (existed.isPresent()) {
            return result(existed.get(), true);
        }
        var receipt = new ReceiptAggregate(ids.incrementAndGet(), command.receiptNo(), command.inboundId(), command.skuCode(), command.expectedQty(), BigDecimal.ZERO, BigDecimal.ZERO, ReceiptStatus.RECEIVING, 0);
        receipts.save(receipt, operator);
        events.publish("WmsArrivalRegistered", "RECEIPT", receipt.receiptNo(), receipt.version(), payload(receipt));
        return result(receipt, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code scan}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code Scan}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result scan(Scan command, long operator) {
        var receipt = load(command.receiptNo());
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "PDA扫码必须提供幂等键");
        }
        if (scans.exists(receipt.id(), command.idempotencyKey()) > 0) {
            return result(receipt, true);
        }
        if (receipt.version() != command.version()) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "收货单版本冲突");
        }
        receipt.scan(command.receivedQty(), command.rejectedQty(), command.rejectReason());
        receipts.save(receipt, operator);
        scans.insert(ids.incrementAndGet(), receipt.id(), command.idempotencyKey(), command.receivedQty(), command.rejectedQty(), command.rejectReason(), operator);
        return result(receipt, false);
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result submit(String no, int version, long operator) {
        var receipt = load(no);
        if (receipt.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "收货单版本冲突");
        }
        receipt.complete();
        receipts.save(receipt, operator);
        events.publish("WmsReceiptCompleted", "RECEIPT", receipt.receiptNo(), receipt.version(), payload(receipt));
        return result(receipt, false);
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ReceiptAggregate}
     */
    private ReceiptAggregate load(String no) {
        return receipts.findByNo(no).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "收货单不存在"));
    }

    /**
     * 处理当前类型职责中的操作 {@code result}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param receipt 业务处理参数或成员，类型为 {@code ReceiptAggregate}
     * @param duplicated 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    private static Result result(ReceiptAggregate receipt, boolean duplicated) {
        return new Result(receipt.id(), receipt.receiptNo(), receipt.status().code(), receipt.status().label(), receipt.version(), duplicated);
    }

    /**
     * 处理当前类型职责中的操作 {@code payload}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param receipt 业务处理参数或成员，类型为 {@code ReceiptAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String payload(ReceiptAggregate receipt) {
        return """
            {"receiptNo":"%s","inboundId":%d,"skuCode":"%s","receivedQty":%s,"rejectedQty":%s}
            """.formatted(receipt.receiptNo(), receipt.inboundId(), receipt.skuCode(), receipt.receivedQty(), receipt.rejectedQty()).trim();
    }

    /**
     * Open。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Open(String receiptNo, long inboundId, String skuCode, BigDecimal expectedQty) {
    }

    /**
     * Scan。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Scan(String receiptNo, int version, BigDecimal receivedQty, BigDecimal rejectedQty, String rejectReason, String idempotencyKey) {
    }

    /**
     * Result。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Result(long id, String receiptNo, int status, String statusName, int version, boolean duplicated) {
    }
}
