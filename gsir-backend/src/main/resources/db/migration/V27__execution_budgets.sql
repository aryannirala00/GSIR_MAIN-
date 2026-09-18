CREATE TABLE execution_budgets (
    execution_id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    max_steps INTEGER NOT NULL,
    max_tool_calls INTEGER NOT NULL,
    max_depth INTEGER NOT NULL,
    max_runtime_millis BIGINT NOT NULL,
    max_tokens BIGINT NOT NULL,
    max_cost DOUBLE PRECISION NOT NULL,
    consumed_steps INTEGER NOT NULL DEFAULT 0,
    consumed_tool_calls INTEGER NOT NULL DEFAULT 0,
    consumed_depth INTEGER NOT NULL DEFAULT 0,
    consumed_tokens BIGINT NOT NULL DEFAULT 0,
    consumed_cost DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_exec_budget_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX idx_exec_budget_project ON execution_budgets(project_id);

