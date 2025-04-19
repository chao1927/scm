package org.scm.bdp.service._share.enums;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum LogisticsChannelServiceType {

    // 服务类型：1（快递）/2（快运）/3（整车）
    EXPRESS(1),
    FAST_DELIVERY(2),
    VEHICLE(3);

    private int value;

    LogisticsChannelServiceType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
