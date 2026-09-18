CREATE TABLE knowledge_entities (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    canonical_name VARCHAR(500) NOT NULL,
    aliases JSONB,
    description TEXT,
    properties JSONB,
    domain VARCHAR(200),
    source_references JSONB,
    confidence DOUBLE PRECISION,
    validation_status VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ke_project_fk FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX ke_project_idx ON knowledge_entities(project_id);

CREATE TABLE knowledge_relationships (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    relationship_type VARCHAR(100) NOT NULL,
    source_entity_id UUID NOT NULL,
    target_entity_id UUID NOT NULL,
    properties JSONB,
    confidence DOUBLE PRECISION,
    provenance_id UUID,
    temporal_context JSONB,
    validation_status VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT kr_project_fk FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT kr_source_fk FOREIGN KEY (source_entity_id) REFERENCES knowledge_entities(id) ON DELETE CASCADE,
    CONSTRAINT kr_target_fk FOREIGN KEY (target_entity_id) REFERENCES knowledge_entities(id) ON DELETE CASCADE
);
CREATE INDEX kr_project_idx ON knowledge_relationships(project_id);
CREATE INDEX kr_source_target_idx ON knowledge_relationships(source_entity_id, target_entity_id);

CREATE TABLE scientific_claims (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    statement TEXT NOT NULL,
    claim_type VARCHAR(50) NOT NULL,
    subject_entity_id UUID NOT NULL,
    predicate VARCHAR(200) NOT NULL,
    object_entity_id UUID NOT NULL,
    evidence JSONB,
    source_ids JSONB,
    provenance_id UUID,
    temporal_context JSONB,
    domain VARCHAR(200),
    confidence DOUBLE PRECISION,
    claim_status VARCHAR(50) NOT NULL,
    validation_status VARCHAR(50) NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT sc_project_fk FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT sc_subject_fk FOREIGN KEY (subject_entity_id) REFERENCES knowledge_entities(id) ON DELETE CASCADE,
    CONSTRAINT sc_object_fk FOREIGN KEY (object_entity_id) REFERENCES knowledge_entities(id) ON DELETE CASCADE
);
CREATE INDEX sc_project_idx ON scientific_claims(project_id);
CREATE INDEX sc_subject_object_idx ON scientific_claims(subject_entity_id, object_entity_id);

CREATE TABLE provenance_records (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    source_id UUID,
    extractor_id VARCHAR(200),
    workflow_step_id UUID,
    execution_fingerprint VARCHAR(500),
    confidence_metric VARCHAR(100),
    extraction_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pr_project_fk FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX pr_project_idx ON provenance_records(project_id);
