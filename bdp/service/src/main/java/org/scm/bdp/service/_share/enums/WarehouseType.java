package org.scm.bdp.service._share.enums;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum WarehouseType {

    // 仓库类型：1（国内仓）/2（海外仓）/3（冷藏仓）/4（保税仓）
    INTERNATIONAL_WAREHOUSE(1),
    OVERSEAS_WAREHOUSE(2),
    COLD_STORAGE_WAREHOUSE(3),
    BONDED_WAREHOUSE(4);

    private int value;

    WarehouseType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
