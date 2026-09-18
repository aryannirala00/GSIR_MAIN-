CREATE TABLE prov_agents (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    agent_type VARCHAR(50) NOT NULL, -- HUMAN, AI_MODEL, SYSTEM
    name VARCHAR(255) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT prov_agent_proj_fk FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX prov_agent_proj_idx ON prov_agents(project_id);

CREATE TABLE prov_activities (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    activity_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    end_time TIMESTAMPTZ,
    parameters JSONB,
    execution_environment JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT prov_act_proj_fk FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX prov_act_proj_idx ON prov_activities(project_id);

CREATE TABLE prov_artifacts (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    artifact_type VARCHAR(100) NOT NULL,
    content_hash VARCHAR(255),
    version_number INTEGER NOT NULL DEFAULT 1,
    location_uri VARCHAR(500),
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT prov_art_proj_fk FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX prov_art_proj_idx ON prov_artifacts(project_id);

CREATE TABLE prov_relationships (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    relationship_type VARCHAR(50) NOT NULL, -- USED, WAS_GENERATED_BY, WAS_ASSOCIATED_WITH, WAS_DERIVED_FROM
    source_id UUID NOT NULL,
    target_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT prov_rel_proj_fk FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX prov_rel_proj_idx ON prov_relationships(project_id);
CREATE INDEX prov_rel_source_idx ON prov_relationships(source_id);
CREATE INDEX prov_rel_target_idx ON prov_relationships(target_id);
