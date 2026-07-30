package com.chaobo.scm.supplier.domain.quote;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.domain.shared.*;
import java.time.*;
import java.util.*;

/**
 * SupplierQuoteAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class SupplierQuoteAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * no（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String no;

    /**
     * supplierId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long supplierId;

    /**
     * rfqId（类型：{@code Long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final Long rfqId;

    /**
     * rfqNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String rfqNo;

    /**
     * currency（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String currency;

    /**
     * from、to（类型：{@code LocalDate}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private LocalDate from, to;

    /**
     * status（类型：{@code QuoteStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private QuoteStatus status;

    /**
     * rejectionReason、agreementRef（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String rejectionReason, agreementRef;

    /**
     * lines（类型：{@code List<QuoteLine>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private List<QuoteLine> lines;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * events（类型：{@code List<DomainEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * 创建 SupplierQuoteAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param rfqId 业务或技术标识，类型为 {@code Long}
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param status 生命周期状态，类型为 {@code QuoteStatus}
     * @param rejectionReason 业务处理参数或成员，类型为 {@code String}
     * @param agreementRef 业务处理参数或成员，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<QuoteLine>}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    private SupplierQuoteAggregate(long id, String no, long supplierId, Long rfqId, String rfqNo, String currency, LocalDate from, LocalDate to, QuoteStatus status, String rejectionReason, String agreementRef, List<QuoteLine> lines, int version) {
        this.id = id;
        this.no = no;
        this.supplierId = supplierId;
        this.rfqId = rfqId;
        this.rfqNo = rfqNo;
        this.currency = currency;
        this.from = from;
        this.to = to;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.agreementRef = agreementRef;
        this.lines = new ArrayList<>(lines);
        this.version = version;
        validate();
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param rfqId 业务或技术标识，类型为 {@code Long}
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param lines 业务处理参数或成员，类型为 {@code List<QuoteLine>}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 执行命令的结果，类型为 {@code SupplierQuoteAggregate}
     */
    public static SupplierQuoteAggregate create(long supplierId, Long rfqId, String rfqNo, String currency, LocalDate from, LocalDate to, List<QuoteLine> lines, long operator, IdentifierGenerator ids) {
        long id = ids.nextId();
        var quote = new SupplierQuoteAggregate(id, ids.nextBusinessNo("SQ"), supplierId, rfqId, rfqNo, currency, from, to, QuoteStatus.DRAFT, null, null, lines, 0);
        quote.raise(ids, "SupplierQuoteCreated", "供应商报价已创建", operator, Map.of("supplierId", supplierId, "quoteNo", quote.no, "lineCount", lines.size()));
        return quote;
    }

    /**
     * 处理当前类型职责中的操作 {@code rehydrate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param rfqId 业务或技术标识，类型为 {@code Long}
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param status 生命周期状态，类型为 {@code int}
     * @param rejectionReason 业务处理参数或成员，类型为 {@code String}
     * @param agreementRef 业务处理参数或成员，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<QuoteLine>}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQuoteAggregate}
     */
    public static SupplierQuoteAggregate rehydrate(long id, String no, long supplierId, Long rfqId, String rfqNo, String currency, LocalDate from, LocalDate to, int status, String rejectionReason, String agreementRef, List<QuoteLine> lines, int version) {
        return new SupplierQuoteAggregate(id, no, supplierId, rfqId, rfqNo, currency, from, to, QuoteStatus.fromCode(status), rejectionReason, agreementRef, lines, version);
    }

    /**
     * 处理当前类型职责中的操作 {@code modifyDraft}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param validFrom 业务或技术标识，类型为 {@code LocalDate}
     * @param validTo 业务或技术标识，类型为 {@code LocalDate}
     * @param newLines 业务处理参数或成员，类型为 {@code List<QuoteLine>}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void modifyDraft(LocalDate validFrom, LocalDate validTo, List<QuoteLine> newLines, long operator, IdentifierGenerator ids) {
        require(QuoteStatus.DRAFT);
        if (validFrom == null || validTo == null || validTo.isBefore(validFrom) || newLines == null || newLines.isEmpty()) {
            throw rule("报价有效期或报价行不合法");
        }
        if (newLines.stream().map(QuoteLine::skuCode).distinct().count() != newLines.size()) {
            throw rule("同一报价不能重复SKU");
        }
        from = validFrom;
        to = validTo;
        lines = new ArrayList<>(newLines);
        version++;
        raise(ids, "SupplierQuoteDraftModified", "供应商报价草稿已修改", operator, Map.of("quoteNo", no, "lineCount", newLines.size()));
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void submit(long operator, IdentifierGenerator ids) {
        require(QuoteStatus.DRAFT);
        if (to.isBefore(LocalDate.now())) {
            throw rule("报价有效期已结束");
        }
        status = QuoteStatus.SUBMITTED;
        version++;
        raise(ids, "SupplierQuoteSubmitted", "供应商报价已提交", operator, Map.of("quoteNo", no, "supplierId", supplierId, "rfqId", rfqId == null ? 0 : rfqId, "validTo", to.toString()));
    }

    /**
     * 执行命令 {@code confirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void confirm(long operator, IdentifierGenerator ids) {
        require(QuoteStatus.SUBMITTED);
        status = QuoteStatus.CONFIRMED;
        version++;
        raise(ids, "SupplierQuoteConfirmed", "供应商报价已确认", operator, Map.of("quoteNo", no, "supplierId", supplierId));
    }

    /**
     * 处理当前类型职责中的操作 {@code adopt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ref 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void adopt(String ref, long operator, IdentifierGenerator ids) {
        require(QuoteStatus.CONFIRMED);
        if (ref == null || ref.isBlank()) {
            throw rule("价格协议引用不能为空");
        }
        agreementRef = ref;
        status = QuoteStatus.ADOPTED;
        version++;
        raise(ids, "SupplierQuoteAdopted", "供应商报价已采纳", operator, Map.of("quoteNo", no, "supplierId", supplierId, "priceAgreementRef", ref));
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void reject(String reason, long operator, IdentifierGenerator ids) {
        if (status != QuoteStatus.SUBMITTED && status != QuoteStatus.CONFIRMED) {
            throw state("当前状态不能拒绝报价");
        }
        if (reason == null || reason.isBlank()) {
            throw rule("拒绝原因不能为空");
        }
        rejectionReason = reason.trim();
        status = QuoteStatus.REJECTED;
        version++;
        raise(ids, "SupplierQuoteRejected", "供应商报价已拒绝", operator, Map.of("quoteNo", no, "reason", rejectionReason));
    }

    /**
     * 处理当前类型职责中的操作 {@code voidQuote}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void voidQuote(String reason, long operator, IdentifierGenerator ids) {
        if (status == QuoteStatus.ADOPTED || status == QuoteStatus.VOIDED) {
            throw state("当前状态不能作废报价");
        }
        if (reason == null || reason.isBlank()) {
            throw rule("作废原因不能为空");
        }
        rejectionReason = reason.trim();
        status = QuoteStatus.VOIDED;
        version++;
        raise(ids, "SupplierQuoteVoided", "供应商报价已作废", operator, Map.of("quoteNo", no, "reason", rejectionReason));
    }

    /**
     * 处理当前类型职责中的操作 {@code expire}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void expire(long operator, IdentifierGenerator ids) {
        if (status == QuoteStatus.ADOPTED || status == QuoteStatus.VOIDED || status == QuoteStatus.EXPIRED || to.isAfter(LocalDate.now())) {
            return;
        }
        status = QuoteStatus.EXPIRED;
        version++;
        raise(ids, "SupplierQuoteExpired", "供应商报价已过期", operator, Map.of("quoteNo", no, "validTo", to.toString()));
    }

    /**
     * 校验业务约束 {@code validate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void validate() {
        if (supplierId <= 0 || currency == null || currency.isBlank() || from == null || to == null || to.isBefore(from) || lines == null || lines.isEmpty()) {
            throw rule("报价供应商、币种、有效期或报价行不合法");
        }
        if (lines.stream().map(QuoteLine::skuCode).distinct().count() != lines.size()) {
            throw rule("同一报价不能重复SKU");
        }
    }

    /**
     * 查询并返回 {@code require}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param expected 业务处理参数或成员，类型为 {@code QuoteStatus}
     */
    private void require(QuoteStatus expected) {
        if (status != expected) {
            throw state("报价状态不允许当前操作");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param payload 业务处理参数或成员，类型为 {@code Map<String,Object>}
     */
    private void raise(IdentifierGenerator ids, String type, String name, long operator, Map<String, Object> payload) {
        long event = ids.nextId();
        events.add(new DomainEvent(event, "SUP-" + event, type, name, "SUPPLIER_QUOTE", id, no, version, operator, OffsetDateTime.now(), payload));
    }

    /**
     * 处理当前类型职责中的操作 {@code rule}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param message 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private static BusinessException rule(String message) {
        return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, message);
    }

    /**
     * 处理当前类型职责中的操作 {@code state}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param message 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private static BusinessException state(String message) {
        return new BusinessException(ErrorCode.STATE_CONFLICT, message);
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<DomainEvent>}
     */
    public List<DomainEvent> pullEvents() {
        var result = List.copyOf(events);
        events.clear();
        return result;
    }

    /**
     * 处理当前类型职责中的操作 {@code id}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long id() {
        return id;
    }

    /**
     * 处理当前类型职责中的操作 {@code no}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String no() {
        return no;
    }

    /**
     * 处理当前类型职责中的操作 {@code supplierId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long supplierId() {
        return supplierId;
    }

    /**
     * 处理当前类型职责中的操作 {@code rfqId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    public Long rfqId() {
        return rfqId;
    }

    /**
     * 处理当前类型职责中的操作 {@code rfqNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String rfqNo() {
        return rfqNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code currency}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String currency() {
        return currency;
    }

    /**
     * 处理当前类型职责中的操作 {@code validFrom}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LocalDate}
     */
    public LocalDate validFrom() {
        return from;
    }

    /**
     * 处理当前类型职责中的操作 {@code validTo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LocalDate}
     */
    public LocalDate validTo() {
        return to;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code QuoteStatus}
     */
    public QuoteStatus status() {
        return status;
    }

    /**
     * 执行命令 {@code rejectionReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code String}
     */
    public String rejectionReason() {
        return rejectionReason;
    }

    /**
     * 处理当前类型职责中的操作 {@code agreementRef}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String agreementRef() {
        return agreementRef;
    }

    /**
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<QuoteLine>}
     */
    public List<QuoteLine> lines() {
        return List.copyOf(lines);
    }

    /**
     * 处理当前类型职责中的操作 {@code version}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int version() {
        return version;
    }
}
