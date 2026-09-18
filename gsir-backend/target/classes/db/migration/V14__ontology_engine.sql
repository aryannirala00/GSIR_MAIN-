CREATE TABLE ontologies (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    namespace VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL,
    domain VARCHAR(255),
    source_uri VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ontology_proj_fk FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX ontology_proj_idx ON ontologies(project_id);
CREATE UNIQUE INDEX ontology_ns_ver_idx ON ontologies(project_id, namespace, version);

ALTER TABLE knowledge_relationships ADD COLUMN is_inferred BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE knowledge_relationships ADD COLUMN inference_provenance JSONB;
