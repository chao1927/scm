package com.chaobo.scm.supplier.application.returning;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.common.integration.*;
import com.chaobo.scm.supplier.application.integration.IntegrationCommandEnqueuer;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.returning.*;
import com.chaobo.scm.supplier.domain.shared.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

@Service
public class SupplierReturnApplicationService {
    private final SupplierReturnRepository repo;
    private final SupplierReturnReadModelPort read;
    private final OutboxRepository outbox;
    private final AuditLogRepository audit;
    private final IdentifierGenerator ids;
    private final TransactionalCommandExecutor executor;
    private final IntegrationCommandEnqueuer integrations;

    public SupplierReturnApplicationService(SupplierReturnRepository repo, SupplierReturnReadModelPort read,
            OutboxRepository outbox, AuditLogRepository audit, IdentifierGenerator ids,
            TransactionalCommandExecutor executor, IntegrationCommandEnqueuer integrations) {
        this.repo=repo;this.read=read;this.outbox=outbox;this.audit=audit;this.ids=ids;
        this.executor=executor;this.integrations=integrations;
    }

    @Transactional(readOnly=true)
    public PageResult<SupplierReturnView> page(Long supplierId,Long scope,Integer status,int page,int size){
        if(page<1||size<1||size>100)throw new BusinessException(ErrorCode.VALIDATION_FAILED,"分页参数不合法");
        return read.page(scope==null?supplierId:scope,status,page,size);
    }
    @Transactional(readOnly=true)
    public SupplierReturnView detail(long id,Long scope){
        var value=read.detail(id).orElseThrow(()->new BusinessException(ErrorCode.NOT_FOUND,"退供单不存在"));
        if(scope!=null&&scope!=value.supplierId())throw new BusinessException(ErrorCode.NOT_FOUND,"退供单不存在");
        return value;
    }
    @Transactional public CommandResult create(long supplierId,long warehouseId,Long qualityIssueId,String reason,List<SupplierReturnAggregate.NewLine> lines,CommandContext c){c.requirePermission("supplier:return:create");c.requireSupplierScope(supplierId);return executor.execute("supplier:return",c,new Create(supplierId,warehouseId,qualityIssueId,reason,lines),()->persist(SupplierReturnAggregate.create(supplierId,warehouseId,qualityIssueId,reason,lines,c.operatorId(),ids),c,"CREATE_SUPPLIER_RETURN",null));}
    @Transactional public CommandResult submit(long id,int version,CommandContext c){c.requirePermission("supplier:return:submit");return change(id,version,c,"SUBMIT_SUPPLIER_RETURN",a->a.submit(c.operatorId(),ids));}
    @Transactional public CommandResult review(long id,int version,boolean pass,String comment,CommandContext c){c.requirePermission("supplier:return:review");return change(id,version,c,"REVIEW_SUPPLIER_RETURN",a->a.review(pass,comment,c.operatorId(),ids));}
    @Transactional public CommandResult requestInventoryLock(long id,int version,CommandContext c){c.requirePermission("supplier:return:inventory_lock");return change(id,version,c,"REQUEST_RETURN_INVENTORY_LOCK",a->a.requestInventoryLock(c.operatorId(),ids));}
    @Transactional public CommandResult supplierConfirm(long id,int version,boolean diff,String reason,CommandContext c){c.requirePermission("supplier:return:confirm");return change(id,version,c,"CONFIRM_SUPPLIER_RETURN",a->a.supplierConfirm(diff,reason,c.operatorId(),ids));}
    @Transactional public CommandResult resolveDifference(long id,int version,CommandContext c){c.requirePermission("supplier:return:resolve_difference");return change(id,version,c,"RESOLVE_RETURN_DIFFERENCE",a->a.resolveDifference(c.operatorId(),ids));}
    @Transactional public CommandResult close(long id,int version,CommandContext c){c.requirePermission("supplier:return:close");return change(id,version,c,"CLOSE_SUPPLIER_RETURN",a->a.close(c.operatorId(),ids));}
    @Transactional public CommandResult recordInventoryLock(long id,boolean success,String lockNo,Map<Long,BigDecimal> quantities,String reason,CommandContext c){return externalChange(id,c,"RECORD_RETURN_INVENTORY_LOCK",a->a.recordInventoryLock(success,lockNo,quantities,reason,c.operatorId(),ids));}
    @Transactional public CommandResult recordOutbound(long id,String no,Map<Long,BigDecimal> quantities,CommandContext c){return externalChange(id,c,"RECORD_RETURN_OUTBOUND",a->a.recordOutbound(no,quantities,c.operatorId(),ids));}
    @Transactional public CommandResult recordWaybill(long id,String shipment,String waybill,String carrier,CommandContext c){return externalChange(id,c,"RECORD_RETURN_WAYBILL",a->a.recordWaybill(shipment,waybill,carrier,c.operatorId(),ids));}
    @Transactional public CommandResult recordSigned(long id,Map<Long,BigDecimal> quantities,String differenceReason,CommandContext c){return externalChange(id,c,"RECORD_RETURN_SIGNED",a->a.recordSigned(quantities,differenceReason,c.operatorId(),ids));}
    @Transactional public CommandResult recordTransportException(long id,String reason,CommandContext c){return externalChange(id,c,"RECORD_RETURN_TRANSPORT_EXCEPTION",a->a.recordTransportException(reason,c.operatorId(),ids));}
    @Transactional public CommandResult recordSettlement(long id,String ref,BigDecimal offset,BigDecimal claim,CommandContext c){return externalChange(id,c,"RECORD_RETURN_SETTLEMENT",a->a.recordSettlement(ref,offset,claim,c.operatorId(),ids));}

    private CommandResult change(long id,int version,CommandContext c,String operation,Consumer<SupplierReturnAggregate> action){return executor.execute("supplier:return",c,new Change(id,version,operation),()->{var aggregate=get(id);c.requireSupplierScope(aggregate.supplierId());if(aggregate.version()!=version)throw new BusinessException(ErrorCode.VERSION_CONFLICT,"退供单已被更新");String before=snapshot(aggregate);action.accept(aggregate);return persist(aggregate,c,operation,before);});}
    private CommandResult externalChange(long id,CommandContext c,String operation,Consumer<SupplierReturnAggregate> action){return executor.execute("supplier:return:event",c,new External(id,operation),()->{var aggregate=get(id);String before=snapshot(aggregate);action.accept(aggregate);return persist(aggregate,c,operation,before);});}
    private SupplierReturnAggregate get(long id){return repo.findById(id).orElseThrow(()->new BusinessException(ErrorCode.NOT_FOUND,"退供单不存在"));}
    private CommandResult persist(SupplierReturnAggregate aggregate,CommandContext context,String operation,String before){repo.save(aggregate,context.operatorId());var events=aggregate.pullEvents();outbox.saveAll(events);enqueueCollaboration(aggregate,operation);audit.save(context,operation,"SUPPLIER_RETURN",aggregate.id(),aggregate.no(),before,snapshot(aggregate));return new CommandResult(aggregate.id(),aggregate.no(),aggregate.status().code(),aggregate.status().label(),aggregate.version(),events.isEmpty()?null:events.getLast().eventCode(),false);}

    private void enqueueCollaboration(SupplierReturnAggregate aggregate,String operation){
        String suffix=aggregate.id()+"-"+aggregate.version();
        if("REQUEST_RETURN_INVENTORY_LOCK".equals(operation)){
            var lines=aggregate.lines().stream().map(line->new InventoryCollaborationApi.Line(line.id(),line.skuCode(),line.batchNo(),line.inventoryStatus(),line.requestedQty())).toList();
            integrations.enqueue("INVENTORY_LOCK_RETURN","SUPPLIER_RETURN",aggregate.id(),aggregate.version(),"INVENTORY",new InventoryCollaborationApi.ReturnLockCommand("RETURN-LOCK-"+suffix,aggregate.id(),aggregate.no(),aggregate.supplierId(),aggregate.warehouseId(),lines));
        }else if("CONFIRM_SUPPLIER_RETURN".equals(operation)&&aggregate.status()==SupplierReturnStatus.PENDING_OUTBOUND){
            var lines=aggregate.lines().stream().map(line->new WmsCollaborationApi.Line(line.id(),line.skuCode(),line.batchNo(),line.lockedQty())).toList();
            integrations.enqueue("WMS_CREATE_RETURN_OUTBOUND","SUPPLIER_RETURN",aggregate.id(),aggregate.version(),"WMS",new WmsCollaborationApi.ReturnOutboundCommand("RETURN-OUTBOUND-"+suffix,aggregate.id(),aggregate.no(),aggregate.supplierId(),aggregate.warehouseId(),aggregate.inventoryLockNo(),lines));
        }else if("CONFIRM_SUPPLIER_RETURN".equals(operation)&&aggregate.status()==SupplierReturnStatus.SUPPLIER_DIFFERENCE&&aggregate.inventoryLockNo()!=null){
            integrations.enqueue("INVENTORY_RELEASE_RETURN","SUPPLIER_RETURN",aggregate.id(),aggregate.version(),"INVENTORY",new InventoryCollaborationApi.ReturnReleaseCommand("RETURN-RELEASE-"+suffix,aggregate.id(),aggregate.inventoryLockNo(),"供应商拒绝或提出退供差异"));
        }else if("RECORD_RETURN_OUTBOUND".equals(operation)){
            integrations.enqueue("TMS_CREATE_RETURN_TRANSPORT","SUPPLIER_RETURN",aggregate.id(),aggregate.version(),"TMS",new TmsCollaborationApi.ReturnTransportCommand("RETURN-TRANSPORT-"+suffix,aggregate.id(),aggregate.no(),aggregate.supplierId(),aggregate.warehouseId(),aggregate.outboundNo()));
        }else if("RECORD_RETURN_SIGNED".equals(operation)&&aggregate.status()==SupplierReturnStatus.SIGNED){
            integrations.enqueue("BMS_CREATE_RETURN_SETTLEMENT","SUPPLIER_RETURN",aggregate.id(),aggregate.version(),"BMS",new BmsCollaborationApi.ReturnSettlementCommand("RETURN-SETTLEMENT-"+suffix,aggregate.id(),aggregate.no(),aggregate.supplierId(),BigDecimal.ZERO,BigDecimal.ZERO,"按退供单和合同价格计算冲减"));
        }
    }
    private String snapshot(SupplierReturnAggregate aggregate){return "{\"returnNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(aggregate.no(),aggregate.status().code(),aggregate.version());}
    private record Create(long supplierId,long warehouseId,Long qualityIssueId,String reason,List<SupplierReturnAggregate.NewLine> lines){}
    private record Change(long id,int version,String operation){}
    private record External(long id,String operation){}
}
