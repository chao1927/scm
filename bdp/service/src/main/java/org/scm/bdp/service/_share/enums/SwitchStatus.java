package org.scm.bdp.service._share.enums;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum SwitchStatus {

    ENABLED(1),
    DISABLED(0);

    private final int value;

    SwitchStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
