package com.chaobo.scm.wms.infrastructure.persistence.receiving;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.domain.receiving.ReceiptAggregate;
import com.chaobo.scm.wms.domain.receiving.ReceiptRepository;
import com.chaobo.scm.wms.domain.receiving.ReceiptStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisReceiptRepository implements ReceiptRepository {
    private final ReceiptMapper mapper;
    public MyBatisReceiptRepository(ReceiptMapper mapper) { this.mapper = mapper; }
    public Optional<ReceiptAggregate> findByNo(String no) { return Optional.ofNullable(mapper.findByNo(no)).map(this::map); }
    public void save(ReceiptAggregate receipt, long operator) {
        var current=mapper.findByNo(receipt.receiptNo());
        if(current==null){mapper.insert(receipt.id(),receipt.receiptNo(),receipt.inboundId(),receipt.skuCode(),receipt.expectedQty(),receipt.receivedQty(),receipt.rejectedQty(),receipt.status().code(),receipt.version(),operator);return;}
        if(mapper.update(receipt.id(),receipt.receivedQty(),receipt.rejectedQty(),receipt.status().code(),receipt.version(),current.version(),operator)!=1) throw new BusinessException(ErrorCode.VERSION_CONFLICT,"收货单已被其他人修改");
    }
    private ReceiptAggregate map(ReceiptMapper.Row r){return new ReceiptAggregate(r.id(),r.receiptNo(),r.inboundId(),r.skuCode(),r.expectedQty(),r.receivedQty(),r.rejectedQty(),ReceiptStatus.values()[r.status()-1],r.version());}
}
