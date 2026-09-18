CREATE TABLE execution_costs (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    workflow_id UUID,
    step_id UUID,
    compute_cost DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    model_cost DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    api_cost DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_cost DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_cost_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX idx_exec_cost_project ON execution_costs(project_id);
CREATE INDEX idx_exec_cost_workflow ON execution_costs(workflow_id);

CREATE TABLE human_review_tasks (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    workflow_id UUID NOT NULL,
    step_id UUID NOT NULL,
    reviewer_id UUID,
    status VARCHAR(50) NOT NULL,
    review_context JSONB,
    decision JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMPTZ,
    CONSTRAINT fk_review_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_workflow FOREIGN KEY (workflow_id) REFERENCES research_workflows(id) ON DELETE CASCADE
);
CREATE INDEX idx_review_project ON human_review_tasks(project_id);
CREATE INDEX idx_review_workflow ON human_review_tasks(workflow_id);

CREATE TABLE project_secrets (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    secret_name VARCHAR(255) NOT NULL,
    encrypted_value TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_secret_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT uq_secret_project_name UNIQUE (project_id, secret_name)
);
