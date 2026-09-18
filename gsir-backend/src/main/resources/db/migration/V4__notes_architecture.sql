-- V3__notes_architecture.sql

ALTER TABLE notes ADD COLUMN workspace_id UUID;
ALTER TABLE notes ADD COLUMN organization_id UUID;
ALTER TABLE notes ADD COLUMN version_number INTEGER NOT NULL DEFAULT 1;
ALTER TABLE notes ADD COLUMN is_archived BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE notes ADD COLUMN summary TEXT;
ALTER TABLE notes ADD COLUMN ai_generated BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE notes ADD COLUMN ai_generated_summary BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE notes ADD CONSTRAINT notes_workspace_fk FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE;
ALTER TABLE notes ADD CONSTRAINT notes_org_fk FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE;
CREATE INDEX notes_workspace_idx ON notes(workspace_id);
CREATE INDEX notes_org_idx ON notes(organization_id);

CREATE TABLE note_versions (
    id UUID PRIMARY KEY,
    note_id UUID NOT NULL,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    version_number INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID NOT NULL,
    CONSTRAINT note_versions_note_fk FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    CONSTRAINT note_versions_user_fk FOREIGN KEY (created_by) REFERENCES users(id)
);
CREATE INDEX note_versions_note_idx ON note_versions(note_id);

CREATE TABLE note_tags (
    note_id UUID NOT NULL,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (note_id, tag),
    CONSTRAINT note_tags_note_fk FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE
);

CREATE TABLE note_relationships (
    id UUID PRIMARY KEY,
    note_id UUID NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT note_rel_note_fk FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE
);
CREATE INDEX note_rel_target_idx ON note_relationships(target_type, target_id);

CREATE TABLE note_attachments (
    id UUID PRIMARY KEY,
    note_id UUID NOT NULL,
    file_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT note_att_note_fk FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    CONSTRAINT note_att_file_fk FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE
);

CREATE TABLE knowledge_candidates (
    id UUID PRIMARY KEY,
    source_note_id UUID NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    provenance TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID NOT NULL,
    CONSTRAINT kc_note_fk FOREIGN KEY (source_note_id) REFERENCES notes(id) ON DELETE CASCADE,
    CONSTRAINT kc_user_fk FOREIGN KEY (created_by) REFERENCES users(id)
);

