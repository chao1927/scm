package org.scm.bdp.service._share.enums;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum EmployeeStatus {


    // 1-在职,2-离职,3-停薪留职
    IN_SERVICE(1, "在职"),
    LEAVE_OF_ABSENCE(2, "离职"),
    SUSPENDED_SERVICE(3, "停薪留职");

    private Integer code;
    private String desc;

    EmployeeStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
