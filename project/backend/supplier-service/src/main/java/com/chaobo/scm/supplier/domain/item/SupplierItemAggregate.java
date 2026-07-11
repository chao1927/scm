package com.chaobo.scm.supplier.domain.item;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.domain.shared.*;
import java.time.OffsetDateTime;
import java.util.*;

public final class SupplierItemAggregate {
    private final long itemId; private final long supplierId; private final String skuCode;
    private String supplierSkuCode; private SupplyCondition condition; private SupplyStatus status;
    private String pauseReason; private int version; private final List<DomainEvent> events=new ArrayList<>();
    private SupplierItemAggregate(long id,long supplierId,String sku,String supplierSku,SupplyCondition condition,SupplyStatus status,String pauseReason,int version){
        this.itemId=id;this.supplierId=supplierId;this.skuCode=sku;this.supplierSkuCode=supplierSku;this.condition=condition;this.status=status;this.pauseReason=pauseReason;this.version=version;
    }
    public static SupplierItemAggregate enable(long supplierId,String sku,String supplierSku,SupplyCondition condition,long operator,IdentifierGenerator ids){
        if(supplierId<=0||sku==null||sku.isBlank())throw rule("供应商和SKU不能为空");
        long id=ids.nextId();var a=new SupplierItemAggregate(id,supplierId,sku,supplierSku,condition,SupplyStatus.AVAILABLE,null,0);
        a.raise(ids,"SupplierItemEnabled","供应商商品已启用",operator,Map.of("supplierId",supplierId,"skuCode",sku));return a;
    }
    public static SupplierItemAggregate rehydrate(long id,long supplierId,String sku,String supplierSku,SupplyCondition condition,SupplyStatus status,String pauseReason,int version){return new SupplierItemAggregate(id,supplierId,sku,supplierSku,condition,status,pauseReason,version);}
    public void changeCondition(String supplierSku,SupplyCondition condition,long operator,IdentifierGenerator ids){
        if(status==SupplyStatus.DISCONTINUED)throw state("停供商品不能修改供货条件");
        this.supplierSkuCode=supplierSku;this.condition=condition;version++;
        raise(ids,"SupplierItemSupplyConditionChanged","供应商商品供货条件已变更",operator,Map.of("supplierId",supplierId,"skuCode",skuCode));
    }
    public void pause(String reason,long operator,IdentifierGenerator ids){
        if(status!=SupplyStatus.AVAILABLE)throw state("只有可供商品可以暂停");if(reason==null||reason.isBlank())throw rule("暂停原因不能为空");
        status=SupplyStatus.PAUSED;pauseReason=reason.trim();version++;raise(ids,"SupplierItemPaused","供应商商品已暂停",operator,Map.of("reason",pauseReason));
    }
    public void resume(long operator,IdentifierGenerator ids){
        if(status!=SupplyStatus.PAUSED)throw state("只有暂停商品可以恢复");status=SupplyStatus.AVAILABLE;pauseReason=null;version++;raise(ids,"SupplierItemResumed","供应商商品已恢复",operator,Map.of("supplierId",supplierId,"skuCode",skuCode));
    }
    public void discontinue(String reason,long operator,IdentifierGenerator ids){
        if(status==SupplyStatus.DISCONTINUED)throw state("当前商品已停供");if(reason==null||reason.isBlank())throw rule("停供原因不能为空");status=SupplyStatus.DISCONTINUED;pauseReason=reason.trim();version++;raise(ids,"SupplierItemDiscontinued","供应商商品已停供",operator,Map.of("reason",pauseReason,"supplierId",supplierId,"skuCode",skuCode));}
    private void raise(IdentifierGenerator ids,String type,String name,long operator,Map<String,Object> payload){long eventId=ids.nextId();events.add(new DomainEvent(eventId,"SUP-"+eventId,type,name,"SUPPLIER_ITEM",itemId,Long.toString(itemId),version,operator,OffsetDateTime.now(),payload));}
    private static BusinessException rule(String m){return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED,m);} private static BusinessException state(String m){return new BusinessException(ErrorCode.STATE_CONFLICT,m);}
    public List<DomainEvent> pullEvents(){var c=List.copyOf(events);events.clear();return c;}
    public long itemId(){return itemId;}public long supplierId(){return supplierId;}public String skuCode(){return skuCode;}public String supplierSkuCode(){return supplierSkuCode;}public SupplyCondition condition(){return condition;}public SupplyStatus status(){return status;}public String pauseReason(){return pauseReason;}public int version(){return version;}
}
