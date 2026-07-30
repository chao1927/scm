package com.chaobo.scm.common.api;

import java.util.List;

/**
 * PageResult。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record PageResult<T>(int pageNo, int pageSize, long total, List<T> records) {

    public PageResult {
        records = List.copyOf(records);
    }
}
