package com.chaobo.scm.common.api;

import java.util.List;

public record PageResult<T>(int pageNo, int pageSize, long total, List<T> records) {
    public PageResult {
        records = List.copyOf(records);
    }
}
