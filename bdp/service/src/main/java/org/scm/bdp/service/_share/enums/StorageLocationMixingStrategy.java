package org.scm.bdp.service._share.enums;

import lombok.Getter;
import lombok.ToString;
import org.scm.common.exception.BizException;

import org.scm.bdp.service._share.enums.errorcode.WarehouseErrorCode;

@Getter
@ToString
public enum StorageLocationMixingStrategy {

    // 混放策略：1（允许批次混放）/2（允许SKU混放）/3（禁止混放）
    ALLOW_BATCH_MIXING(1),
    ALLOW_SKU_MIXING(2),
    FORBID_MIXING(3);

    private int value;

    StorageLocationMixingStrategy(int value) {
        this.value = value;
    }

    public static void checkStrategy(Integer mixingStrategy) {
        if (mixingStrategy == null) {
            throw new BizException(WarehouseErrorCode.MIXING_STRATEGY_NOT_EXIST);
        }

        for (StorageLocationMixingStrategy strategy : StorageLocationMixingStrategy.values()) {
            if (strategy.getValue() == mixingStrategy) {
                return;
            }
        }
        throw new BizException(WarehouseErrorCode.MIXING_STRATEGY_NOT_EXIST);
    }

    public int getValue() {
        return value;
    }



}
