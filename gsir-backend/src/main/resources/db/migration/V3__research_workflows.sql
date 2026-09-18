CREATE TABLE research_workflows (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    objective TEXT NOT NULL,
    research_state JSONB,
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT rw_project_fk
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,
    CONSTRAINT rw_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE research_workflow_steps (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    job_id UUID,
    step_name VARCHAR(100) NOT NULL,
    capability VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    inputs JSONB,
    outputs JSONB,
    dependencies JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT rws_workflow_fk
        FOREIGN KEY (workflow_id)
        REFERENCES research_workflows(id)
        ON DELETE CASCADE,
    CONSTRAINT rws_job_fk
        FOREIGN KEY (job_id)
        REFERENCES jobs(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_rw_project ON research_workflows(project_id);
CREATE INDEX idx_rws_workflow ON research_workflow_steps(workflow_id);
CREATE INDEX idx_rws_job ON research_workflow_steps(job_id);
