CREATE TABLE sup_reconciliation (
  reconciliation_id BIGINT PRIMARY KEY, statement_no VARCHAR(64) NOT NULL, supplier_id BIGINT NOT NULL,
  currency VARCHAR(3) NOT NULL, statement_amount DECIMAL(18,2) NOT NULL, confirmed_amount DECIMAL(18,2) NULL,
  status TINYINT NOT NULL COMMENT '1待确认 2已确认 3差异中 4已撤回 5已关闭', difference_reason VARCHAR(500) NULL,
  source_version INT NOT NULL DEFAULT 0, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3), version INT NOT NULL DEFAULT 0, deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_sup_reconciliation_no(statement_no), KEY idx_sup_reconciliation_supplier(supplier_id,status)
);
CREATE TABLE sup_invoice_collaboration (
  invoice_id BIGINT PRIMARY KEY, invoice_no VARCHAR(64) NOT NULL, supplier_id BIGINT NOT NULL, reconciliation_id BIGINT NULL,
  invoice_type TINYINT NOT NULL COMMENT '1专票 2普票 3红字票', amount_excluding_tax DECIMAL(18,2) NOT NULL, tax_amount DECIMAL(18,2) NOT NULL, tax_rate DECIMAL(8,4) NOT NULL,
  attachment_url VARCHAR(500) NOT NULL, status TINYINT NOT NULL COMMENT '1待校验 2校验通过 3待补充 4已关闭', validation_message VARCHAR(500) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3), version INT NOT NULL DEFAULT 0, deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_sup_invoice_no(invoice_no), KEY idx_sup_invoice_supplier(supplier_id,status)
);
CREATE TABLE sup_performance_fact (
  fact_id BIGINT PRIMARY KEY, event_code VARCHAR(64) NOT NULL, supplier_id BIGINT NOT NULL, dimension_code VARCHAR(32) NOT NULL,
  metric_code VARCHAR(64) NOT NULL, metric_value DECIMAL(18,6) NOT NULL, occurred_at DATETIME(3) NOT NULL, source_system VARCHAR(32) NOT NULL, source_no VARCHAR(64) NULL,
  payload_json JSON NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), UNIQUE KEY uk_sup_performance_event(event_code), KEY idx_sup_performance_period(supplier_id,occurred_at,dimension_code)
);
CREATE TABLE sup_score_rule (
  rule_id BIGINT PRIMARY KEY, rule_name VARCHAR(128) NOT NULL, dimension_code VARCHAR(32) NOT NULL, metric_code VARCHAR(64) NOT NULL,
  weight DECIMAL(8,4) NOT NULL, target_value DECIMAL(18,6) NOT NULL, score_direction TINYINT NOT NULL COMMENT '1越大越好 2越小越好', status TINYINT NOT NULL COMMENT '1草稿 2已发布 3已停用',
  effective_from DATE NOT NULL, effective_to DATE NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3), version INT NOT NULL DEFAULT 0
);
CREATE TABLE sup_score_result (
  score_result_id BIGINT PRIMARY KEY, supplier_id BIGINT NOT NULL, period_code VARCHAR(16) NOT NULL, total_score DECIMAL(8,2) NOT NULL,
  dimension_scores_json JSON NOT NULL, fact_summary_json JSON NOT NULL, manual_adjustment DECIMAL(8,2) NOT NULL DEFAULT 0, adjustment_reason VARCHAR(500) NULL,
  status TINYINT NOT NULL COMMENT '1待发布 2已发布', published_at DATETIME(3) NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3), version INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_sup_score_period(supplier_id,period_code)
);
CREATE TABLE sup_risk_recommendation (
  recommendation_id BIGINT PRIMARY KEY, supplier_id BIGINT NOT NULL, score_result_id BIGINT NOT NULL, risk_level TINYINT NOT NULL COMMENT '1提醒 2预警 3高风险',
  recommendation_type TINYINT NOT NULL COMMENT '1整改 2限制供货 3冻结建议', reason VARCHAR(500) NOT NULL, status TINYINT NOT NULL COMMENT '1待处理 2已接受 3已驳回 4已关闭',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3), version INT NOT NULL DEFAULT 0, KEY idx_sup_risk_supplier(supplier_id,status)
);
