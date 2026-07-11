CREATE TABLE IF NOT EXISTS sup_supplier_contract_version_history (
 history_id BIGINT NOT NULL AUTO_INCREMENT, contract_id BIGINT NOT NULL, contract_version INT NOT NULL, effective_to DATE NOT NULL, terms_json JSON NOT NULL, attachment_url VARCHAR(512) NULL, contract_status TINYINT NOT NULL, changed_by BIGINT NOT NULL, changed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 PRIMARY KEY(history_id), UNIQUE KEY uk_sup_contract_version_history(contract_id,contract_version), KEY idx_sup_contract_history(contract_id,changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商合同版本历史快照';
