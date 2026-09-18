CREATE TABLE research_objects (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    object_type VARCHAR(100) NOT NULL,
    persistent_identifier VARCHAR(255),
    version_id VARCHAR(50) NOT NULL DEFAULT '1.0.0',
    metadata JSONB,
    schema_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT ro_proj_fk FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX ro_proj_idx ON research_objects(project_id);
CREATE INDEX ro_type_idx ON research_objects(object_type);

CREATE TABLE metadata_schemas (
    id UUID PRIMARY KEY,
    schema_name VARCHAR(255) NOT NULL,
    schema_version VARCHAR(50) NOT NULL,
    schema_definition JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX metadata_schema_name_ver_idx ON metadata_schemas(schema_name, schema_version);
