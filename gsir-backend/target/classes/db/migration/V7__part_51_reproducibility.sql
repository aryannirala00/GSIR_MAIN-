ALTER TABLE research_workflows
ADD COLUMN reproducibility_fingerprint VARCHAR(255),
ADD COLUMN environment_metadata JSONB,
ADD COLUMN reproducibility_status VARCHAR(64);

ALTER TABLE research_workflow_steps
ADD COLUMN execution_fingerprint VARCHAR(255),
ADD COLUMN tool_version VARCHAR(64);

CREATE INDEX idx_rws_execution_fingerprint ON research_workflow_steps(execution_fingerprint);
CREATE INDEX idx_rw_reproducibility_fingerprint ON research_workflows(reproducibility_fingerprint);
