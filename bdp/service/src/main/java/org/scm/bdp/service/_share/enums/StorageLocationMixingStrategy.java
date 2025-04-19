package org.scm.bdp.service._share.enums;

import lombok.Getter;
import lombok.ToString;

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

    public int getValue() {
        return value;
    }

}
