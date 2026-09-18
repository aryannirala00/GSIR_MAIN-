CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT users_email_unique UNIQUE (email),
    CONSTRAINT users_status_check
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED'))
);
CREATE TABLE profiles (
    user_id UUID PRIMARY KEY,
    display_name VARCHAR(200),
    avatar_url TEXT,
    timezone VARCHAR(100),
    locale VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT profiles_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
CREATE TABLE sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT sessions_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
CREATE INDEX sessions_user_idx
    ON sessions(user_id);
CREATE INDEX sessions_expiry_idx
    ON sessions(expires_at);
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT refresh_tokens_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT refresh_tokens_hash_unique
        UNIQUE (token_hash)
);
CREATE INDEX refresh_tokens_user_idx
    ON refresh_tokens(user_id);
CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT organizations_creator_fk
        FOREIGN KEY (created_by)
        REFERENCES users(id)
);
CREATE TABLE organization_memberships (
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (organization_id, user_id),
    CONSTRAINT organization_membership_org_fk
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id)
        ON DELETE CASCADE,
    CONSTRAINT organization_membership_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT organization_membership_role_check
        CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER'))
);
CREATE TABLE workspaces (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT workspace_org_fk
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id)
        ON DELETE CASCADE,
    CONSTRAINT workspace_org_id_unique
        UNIQUE (organization_id, id)
);
CREATE INDEX workspaces_org_idx
    ON workspaces(organization_id);
CREATE TABLE workspace_memberships (
    workspace_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (workspace_id, user_id),
    CONSTRAINT workspace_membership_workspace_fk
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id)
        ON DELETE CASCADE,
    CONSTRAINT workspace_membership_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT workspace_membership_role_check
        CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER'))
);
CREATE TABLE projects (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT project_workspace_fk
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id)
        ON DELETE CASCADE,
    CONSTRAINT project_workspace_id_unique
        UNIQUE (workspace_id, id),
    CONSTRAINT project_status_check
        CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);
CREATE INDEX projects_workspace_idx
    ON projects(workspace_id);
CREATE TABLE notes (
    id UUID PRIMARY KEY,
    project_id UUID,
    user_id UUID NOT NULL,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT notes_project_fk
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,
    CONSTRAINT notes_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);
CREATE INDEX notes_project_idx
    ON notes(project_id);
CREATE TABLE web_links (
    id UUID PRIMARY KEY,
    project_id UUID,
    user_id UUID NOT NULL,
    url TEXT NOT NULL,
    title VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT web_links_project_fk
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,
    CONSTRAINT web_links_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);
CREATE INDEX web_links_project_idx
    ON web_links(project_id);
CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    workspace_id UUID,
    project_id UUID,
    title VARCHAR(500),
    mode VARCHAR(32) NOT NULL DEFAULT 'AUTOMATIC',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT conversations_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id),
    CONSTRAINT conversations_workspace_fk
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id)
        ON DELETE SET NULL,
    CONSTRAINT conversations_project_fk
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE SET NULL
);
CREATE INDEX conversations_user_idx
    ON conversations(user_id);
CREATE INDEX conversations_project_idx
    ON conversations(project_id);
CREATE TABLE messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    parent_message_id UUID,
    role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT messages_conversation_fk
        FOREIGN KEY (conversation_id)
        REFERENCES conversations(id)
        ON DELETE CASCADE,
    CONSTRAINT messages_parent_fk
        FOREIGN KEY (parent_message_id)
        REFERENCES messages(id)
        ON DELETE SET NULL
);
CREATE INDEX messages_conversation_idx
    ON messages(conversation_id, created_at);
CREATE TABLE message_parts (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL,
    part_type VARCHAR(32) NOT NULL,
    content TEXT,
    metadata JSONB,
    sequence_number INTEGER NOT NULL,
    CONSTRAINT message_parts_message_fk
        FOREIGN KEY (message_id)
        REFERENCES messages(id)
        ON DELETE CASCADE,
    CONSTRAINT message_parts_sequence_unique
        UNIQUE (message_id, sequence_number)
);
CREATE TABLE files (
    id UUID PRIMARY KEY,
    project_id UUID,
    user_id UUID NOT NULL,
    name VARCHAR(500) NOT NULL,
    mime_type VARCHAR(255),
    size_bytes BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT files_project_fk
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,
    CONSTRAINT files_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);
CREATE INDEX files_project_idx
    ON files(project_id);
CREATE TABLE file_versions (
    id UUID PRIMARY KEY,
    file_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    checksum_sha256 VARCHAR(64) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT file_versions_file_fk
        FOREIGN KEY (file_id)
        REFERENCES files(id)
        ON DELETE CASCADE,
    CONSTRAINT file_versions_unique
        UNIQUE (file_id, version_number)
);
CREATE TABLE storage_objects (
    id UUID PRIMARY KEY,
    file_version_id UUID NOT NULL,
    provider VARCHAR(64) NOT NULL,
    object_key TEXT NOT NULL,
    checksum_sha256 VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT storage_objects_version_fk
        FOREIGN KEY (file_version_id)
        REFERENCES file_versions(id)
        ON DELETE CASCADE
);
CREATE TABLE document_extractions (
    id UUID PRIMARY KEY,
    file_version_id UUID NOT NULL,
    extraction_type VARCHAR(64) NOT NULL,
    content TEXT,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT document_extractions_version_fk
        FOREIGN KEY (file_version_id)
        REFERENCES file_versions(id)
        ON DELETE CASCADE
);
CREATE TABLE document_chunks (
    id UUID PRIMARY KEY,
    file_version_id UUID NOT NULL,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT document_chunks_version_fk
        FOREIGN KEY (file_version_id)
        REFERENCES file_versions(id)
        ON DELETE CASCADE,
    CONSTRAINT document_chunks_unique
        UNIQUE (file_version_id, chunk_index)
);
CREATE TABLE ai_providers (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    provider_type VARCHAR(64) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ai_providers_name_unique
        UNIQUE (name)
);
CREATE TABLE ai_models (
    id UUID PRIMARY KEY,
    provider_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    capability_metadata JSONB,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ai_models_provider_fk
        FOREIGN KEY (provider_id)
        REFERENCES ai_providers(id)
        ON DELETE CASCADE,
    CONSTRAINT ai_models_provider_name_unique
        UNIQUE (provider_id, name)
);
CREATE TABLE ai_model_versions (
    id UUID PRIMARY KEY,
    model_id UUID NOT NULL,
    version VARCHAR(200) NOT NULL,
    context_window INTEGER,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ai_model_versions_model_fk
        FOREIGN KEY (model_id)
        REFERENCES ai_models(id)
        ON DELETE CASCADE,
    CONSTRAINT ai_model_versions_unique
        UNIQUE (model_id, version)
);
CREATE TABLE prompt_templates (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    purpose VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT prompt_templates_name_unique
        UNIQUE (name)
);
CREATE TABLE prompt_versions (
    id UUID PRIMARY KEY,
    prompt_template_id UUID NOT NULL,
    version INTEGER NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT prompt_versions_template_fk
        FOREIGN KEY (prompt_template_id)
        REFERENCES prompt_templates(id)
        ON DELETE CASCADE,
    CONSTRAINT prompt_versions_unique
        UNIQUE (prompt_template_id, version)
);
CREATE TABLE ai_runs (
    id UUID PRIMARY KEY,
    conversation_id UUID,
    model_version_id UUID,
    prompt_version_id UUID,
    status VARCHAR(32) NOT NULL,
    request_metadata JSONB,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    error_code VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ai_runs_conversation_fk
        FOREIGN KEY (conversation_id)
        REFERENCES conversations(id)
        ON DELETE SET NULL,
    CONSTRAINT ai_runs_model_version_fk
        FOREIGN KEY (model_version_id)
        REFERENCES ai_model_versions(id)
        ON DELETE SET NULL,
    CONSTRAINT ai_runs_prompt_version_fk
        FOREIGN KEY (prompt_version_id)
        REFERENCES prompt_versions(id)
        ON DELETE SET NULL
);
CREATE INDEX ai_runs_conversation_idx
    ON ai_runs(conversation_id);
CREATE TABLE ai_outputs (
    id UUID PRIMARY KEY,
    ai_run_id UUID NOT NULL,
    output_type VARCHAR(64) NOT NULL,
    content TEXT,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ai_outputs_run_fk
        FOREIGN KEY (ai_run_id)
        REFERENCES ai_runs(id)
        ON DELETE CASCADE
);
CREATE TABLE jobs (
    id UUID PRIMARY KEY,
    job_type VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL,
    payload JSONB,
    attempts INTEGER NOT NULL DEFAULT 0,
    available_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX jobs_status_available_idx
    ON jobs(status, available_at);
CREATE TABLE job_attempts (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    attempt_number INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    error_message TEXT,
    CONSTRAINT job_attempts_job_fk
        FOREIGN KEY (job_id)
        REFERENCES jobs(id)
        ON DELETE CASCADE,
    CONSTRAINT job_attempts_unique
        UNIQUE (job_id, attempt_number)
);
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(200) NOT NULL,
    payload JSONB NOT NULL,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX outbox_events_unpublished_idx
    ON outbox_events(created_at)
    WHERE published_at IS NULL;
CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    user_id UUID,
    organization_id UUID,
    action VARCHAR(200) NOT NULL,
    resource_type VARCHAR(100),
    resource_id UUID,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT audit_events_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL,
    CONSTRAINT audit_events_organization_fk
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id)
        ON DELETE SET NULL
);
CREATE INDEX audit_events_resource_idx
    ON audit_events(resource_type, resource_id);
CREATE INDEX audit_events_created_idx
    ON audit_events(created_at);
