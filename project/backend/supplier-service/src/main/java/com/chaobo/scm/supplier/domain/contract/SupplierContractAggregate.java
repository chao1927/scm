package com.chaobo.scm.supplier.domain.contract;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.domain.shared.DomainEvent;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

public final class SupplierContractAggregate {
    private final long id; private final String no; private final long supplierId; private final Long quoteId;
    private final String agreement; private final String type; private final LocalDate from; private LocalDate to;
    private ContractStatus status; private String terms; private String attachment; private String reason; private int version;
    private final List<DomainEvent> events = new ArrayList<>();
    private SupplierContractAggregate(long id,String no,long supplierId,Long quoteId,String agreement,String type,LocalDate from,LocalDate to,ContractStatus status,String terms,String attachment,String reason,int version){this.id=id;this.no=no;this.supplierId=supplierId;this.quoteId=quoteId;this.agreement=agreement;this.type=type;this.from=from;this.to=to;this.status=status;this.terms=terms;this.attachment=attachment;this.reason=reason;this.version=version;validate();}
    public static SupplierContractAggregate create(long supplierId,Long quoteId,String agreement,String type,LocalDate from,LocalDate to,String terms,String attachment,long operator,IdentifierGenerator ids){var result=new SupplierContractAggregate(ids.nextId(),ids.nextBusinessNo("SC"),supplierId,quoteId,agreement,type,from,to,ContractStatus.DRAFT,terms,attachment,null,0);result.raise(ids,"SupplierContractCreated","供应商合同已创建",operator);return result;}
    public static SupplierContractAggregate rehydrate(long id,String no,long supplierId,Long quoteId,String agreement,String type,LocalDate from,LocalDate to,int status,String terms,String attachment,String reason,int version){return new SupplierContractAggregate(id,no,supplierId,quoteId,agreement,type,from,to,ContractStatus.from(status),terms,attachment,reason,version);}
    public void modifyDraft(LocalDate until,String updatedTerms,String updatedAttachment,long operator,IdentifierGenerator ids){require(ContractStatus.DRAFT);if(until==null||until.isBefore(from)||updatedTerms==null||updatedTerms.isBlank()||updatedAttachment==null||updatedAttachment.isBlank())throw rule("合同有效期、条款或附件不合法");to=until;terms=updatedTerms.trim();attachment=updatedAttachment.trim();reason=null;version++;raise(ids,"SupplierContractDraftModified","供应商合同草稿已修改",operator);}
    public void submit(long operator,IdentifierGenerator ids){require(ContractStatus.DRAFT);if(attachment==null||attachment.isBlank())throw rule("合同附件不能为空");status=ContractStatus.APPROVING;reason=null;version++;raise(ids,"SupplierContractSubmitted","供应商合同已提交审批",operator);}
    public void approve(long operator,IdentifierGenerator ids){require(ContractStatus.APPROVING);status=ContractStatus.ACTIVE;reason=null;version++;raise(ids,"SupplierContractActivated","供应商合同已生效",operator);}
    public void rejectApproval(String comment,long operator,IdentifierGenerator ids){require(ContractStatus.APPROVING);if(comment==null||comment.isBlank())throw rule("审批驳回意见不能为空");status=ContractStatus.DRAFT;reason=comment.trim();version++;raise(ids,"SupplierContractApprovalRejected","供应商合同审批已驳回",operator);}
    public void renew(LocalDate until,long operator,IdentifierGenerator ids){require(ContractStatus.ACTIVE);if(until==null||!until.isAfter(to))throw rule("续签截止日期必须晚于当前有效期");to=until;version++;raise(ids,"SupplierContractRenewed","供应商合同已续签",operator);}
    public void terminate(String terminationReason,long operator,IdentifierGenerator ids){if(status!=ContractStatus.ACTIVE&&status!=ContractStatus.APPROVING)throw state("当前状态不能终止合同");if(terminationReason==null||terminationReason.isBlank())throw rule("终止原因不能为空");reason=terminationReason.trim();status=ContractStatus.TERMINATED;version++;raise(ids,"SupplierContractTerminated","供应商合同已终止",operator);}
    public void expire(long operator,IdentifierGenerator ids){if(status!=ContractStatus.ACTIVE||to.isAfter(LocalDate.now()))return;status=ContractStatus.EXPIRED;version++;raise(ids,"SupplierContractExpired","供应商合同已到期",operator);}
    private void validate(){if(supplierId<=0||type==null||type.isBlank()||from==null||to==null||to.isBefore(from)||terms==null||terms.isBlank())throw rule("合同核心信息不完整");}
    private void require(ContractStatus expected){if(status!=expected)throw state("合同状态不允许当前操作");}
    private void raise(IdentifierGenerator ids,String eventType,String name,long operator){long eventId=ids.nextId();events.add(new DomainEvent(eventId,"SUP-"+eventId,eventType,name,"SUPPLIER_CONTRACT",id,no,version,operator,OffsetDateTime.now(),Map.of("contractNo",no,"supplierId",supplierId,"quoteId",quoteId==null?0:quoteId,"priceAgreementRef",agreement==null?"":agreement,"validTo",to.toString())));}
    private static BusinessException rule(String message){return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED,message);}private static BusinessException state(String message){return new BusinessException(ErrorCode.STATE_CONFLICT,message);}
    public List<DomainEvent> pullEvents(){var result=List.copyOf(events);events.clear();return result;} public long id(){return id;} public String no(){return no;} public long supplierId(){return supplierId;} public Long quoteId(){return quoteId;} public String agreement(){return agreement;} public String type(){return type;} public LocalDate from(){return from;} public LocalDate to(){return to;} public ContractStatus status(){return status;} public String terms(){return terms;} public String attachment(){return attachment;} public String reason(){return reason;} public int version(){return version;}
}
