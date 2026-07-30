package com.chaobo.scm.mdm.application.file;

import com.chaobo.scm.mdm.infrastructure.persistence.MdmImportQualityMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 轮询并分派主数据文件任务。
 */
@Component
public class MdmFileTaskDispatchJob {

    private final MdmImportQualityMapper mapper;
    private final MdmFileTaskProcessor processor;
    private final int batchSize;
    private final int maxRetries;

    public MdmFileTaskDispatchJob(MdmImportQualityMapper mapper,
                                  MdmFileTaskProcessor processor,
                                  @Value("${scm.mdm.file-task.batch-size:10}") int batchSize,
                                  @Value("${scm.mdm.file-task.max-retries:8}") int maxRetries) {
        this.mapper = mapper;
        this.processor = processor;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
    }

    @Scheduled(fixedDelayString = "${scm.mdm.file-task.fixed-delay:2000}")
    public void dispatch() {
        mapper.listPendingImportTasks(batchSize, maxRetries).forEach(processor::processImport);
        mapper.listPendingExportTasks(batchSize, maxRetries).forEach(processor::processExport);
    }
}
