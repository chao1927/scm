CREATE TABLE IF NOT EXISTS sup_quality_issue_evidence (
 evidence_id BIGINT NOT NULL, quality_issue_id BIGINT NOT NULL, evidence_type VARCHAR(32) NOT NULL COMMENT '1检验报告 2图片视频 3整改附件 4验证附件 5责任认定',
 attachment_url VARCHAR(512) NULL, content VARCHAR(1000) NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 PRIMARY KEY(evidence_id), KEY idx_sup_quality_evidence_issue(quality_issue_id,evidence_type,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商质量问题证据与处理附件';
