CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    project_id UUID,
    actor_id UUID NOT NULL,
    action VARCHAR(255) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(255),
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_project ON audit_logs(project_id);
CREATE INDEX idx_audit_actor ON audit_logs(actor_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);

CREATE TABLE data_classification_policies (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    classification_level VARCHAR(50) NOT NULL,
    rules JSONB NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_policy_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX idx_policy_project ON data_classification_policies(project_id);
