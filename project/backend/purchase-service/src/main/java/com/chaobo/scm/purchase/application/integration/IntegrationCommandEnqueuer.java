package com.chaobo.scm.purchase.application.integration;

import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import com.chaobo.scm.purchase.infrastructure.persistence.integration.IntegrationCommandMapper;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class IntegrationCommandEnqueuer {
    private final IntegrationCommandMapper mapper;
    private final IdentifierGenerator ids;
    private final ObjectMapper json;

    public IntegrationCommandEnqueuer(IntegrationCommandMapper mapper, IdentifierGenerator ids, ObjectMapper json) {
        this.mapper = mapper;
        this.ids = ids;
        this.json = json;
    }

    public void enqueue(String commandType, String targetSystem, String businessType, String businessId,
                        String businessNo, Object payload) {
        mapper.insert(ids.nextId(), commandType, targetSystem, businessType, businessId, businessNo, write(payload));
    }

    private String write(Object payload) {
        try {
            return json.writeValueAsString(payload);
        } catch (JacksonException exception) {
            throw new IllegalStateException("采购集成命令序列化失败", exception);
        }
    }
}
