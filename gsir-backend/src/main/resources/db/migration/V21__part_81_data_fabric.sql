-- V21__part_81_data_fabric.sql
-- Satisfies Part 81 (GSIR Scientific Data Fabric)

CREATE TABLE data_objects (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    data_type VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(64) NOT NULL,
    source_reference VARCHAR(255),
    schema_reference VARCHAR(255),
    classification VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL,
    storage_reference VARCHAR(255),
    metadata JSONB,
    quality_score DOUBLE PRECISION,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_data_objects_project ON data_objects(project_id);
CREATE INDEX idx_data_objects_type ON data_objects(data_type);
CREATE INDEX idx_data_objects_classification ON data_objects(classification);

