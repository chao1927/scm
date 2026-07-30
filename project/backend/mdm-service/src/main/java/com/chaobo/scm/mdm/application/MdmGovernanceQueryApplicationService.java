package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.infrastructure.persistence.MdmGovernanceQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 主数据审核与变更日志查询服务。
 */
@Service
@Transactional(readOnly = true)
public class MdmGovernanceQueryApplicationService {

    private final MdmGovernanceQueryMapper mapper;

    public MdmGovernanceQueryApplicationService(MdmGovernanceQueryMapper mapper) {
        this.mapper = mapper;
    }

    public Page<MdmGovernanceQueryMapper.ApprovalView> approvals(String typeCode, Integer status,
                                                                  Integer pageNo, Integer pageSize) {
        PageRequest page = page(pageNo, pageSize);
        String normalizedType = normalize(typeCode);
        return new Page<>(mapper.listApprovals(normalizedType, status, page.size(), page.offset()),
                mapper.countApprovals(normalizedType, status), page.number(), page.size());
    }

    public Page<MdmGovernanceQueryMapper.ChangeLogView> changeLogs(String typeCode, String dataCode,
                                                                   Integer pageNo, Integer pageSize) {
        PageRequest page = page(pageNo, pageSize);
        String normalizedType = normalize(typeCode);
        String normalizedCode = normalize(dataCode);
        return new Page<>(mapper.listChangeLogs(normalizedType, normalizedCode, page.size(), page.offset()),
                mapper.countChangeLogs(normalizedType, normalizedCode), page.number(), page.size());
    }

    private PageRequest page(Integer pageNo, Integer pageSize) {
        int number = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int size = pageSize == null ? 20 : pageSize;
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("page size must be between 1 and 100");
        }
        return new PageRequest(number, size, (number - 1) * size);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record PageRequest(int number, int size, int offset) {
    }

    public record Page<T>(List<T> items, long total, int pageNo, int pageSize) {
    }
}
