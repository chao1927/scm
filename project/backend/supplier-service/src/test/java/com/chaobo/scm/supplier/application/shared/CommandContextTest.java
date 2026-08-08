package com.chaobo.scm.supplier.application.shared;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandContextTest {

    @Test
    void readContextDoesNotRequireIdempotencyKey() {
        assertDoesNotThrow(() -> new CommandContext(1L, "查询用户", 1L, null,
                "REQ-1", "TRACE-1", "", Set.of("supplier:*")));
    }

    @Test
    void writeCommandStillRequiresIdempotencyKey() {
        var context = new CommandContext(1L, "写入用户", 1L, null,
                "REQ-1", "TRACE-1", "", Set.of("supplier:*"));
        assertThrows(BusinessException.class, context::requiredIdempotencyKey);
    }
}
