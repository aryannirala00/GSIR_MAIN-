-- V24__part_87_datasets.sql
-- Satisfies Part 87 (GSIR Scientific Data Management and Data Lifecycle Architecture)
-- Extends the existing data_objects table to support deep schema, versioning, quality, and retention.

CREATE TABLE dataset_versions (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL REFERENCES data_objects(id) ON DELETE CASCADE,
    version_name VARCHAR(64) NOT NULL,
    storage_reference VARCHAR(255),
    schema_reference VARCHAR(255),
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE dataset_schemas (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL REFERENCES data_objects(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    definition JSONB NOT NULL,
    version VARCHAR(64) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE dataset_columns (
    id UUID PRIMARY KEY,
    schema_id UUID NOT NULL REFERENCES dataset_schemas(id) ON DELETE CASCADE,
    column_name VARCHAR(255) NOT NULL,
    data_type VARCHAR(64) NOT NULL,
    unit VARCHAR(100),
    constraints JSONB,
    description TEXT
);

CREATE TABLE dataset_quality (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL REFERENCES data_objects(id) ON DELETE CASCADE,
    completeness DOUBLE PRECISION,
    accuracy DOUBLE PRECISION,
    consistency DOUBLE PRECISION,
    validity DOUBLE PRECISION,
    uniqueness DOUBLE PRECISION,
    timeliness DOUBLE PRECISION,
    overall_score DOUBLE PRECISION,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE dataset_retention (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL REFERENCES data_objects(id) ON DELETE CASCADE,
    retention_period_days INTEGER,
    archive_date TIMESTAMP(6) WITH TIME ZONE,
    deletion_date TIMESTAMP(6) WITH TIME ZONE,
    legal_hold BOOLEAN DEFAULT FALSE,
    research_hold BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE dataset_exports (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL REFERENCES data_objects(id) ON DELETE CASCADE,
    requested_by UUID NOT NULL,
    format VARCHAR(64) NOT NULL,
    status VARCHAR(50) NOT NULL,
    export_url VARCHAR(255),
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE dataset_shares (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL REFERENCES data_objects(id) ON DELETE CASCADE,
    shared_by UUID NOT NULL,
    shared_with UUID NOT NULL,
    permission_level VARCHAR(50) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Note: dataset_lineage and dataset_events are deferred to existing prov_relationships and prov_events per Rule 5.
