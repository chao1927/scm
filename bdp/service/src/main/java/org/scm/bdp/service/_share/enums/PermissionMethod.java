package org.scm.bdp.service._share.enums;

import lombok.Getter;
import lombok.ToString;
import org.scm.bdp.service._share.enums.errorcode.PermissionErrorCode;
import org.scm.common.exception.BizException;

@Getter
@ToString
public enum PermissionMethod {

    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    private final String method;

    PermissionMethod(String method) {
        this.method = method;
    }

    public static void checkMethod(String method) {
        for (PermissionMethod permissionMethod : PermissionMethod.values()) {
            if (permissionMethod.getMethod().equals(method)) {
                return;
            }
        }
        throw new BizException(PermissionErrorCode.PERMISSION_METHOD_NOT_ALLOWED);
    }

}
