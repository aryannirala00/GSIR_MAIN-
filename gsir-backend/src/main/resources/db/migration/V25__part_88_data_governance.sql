-- V25__part_88_data_governance.sql
-- Satisfies Part 88 (GSIR Scientific Data Governance)
-- Extends the dataset tracking with explicit policies and licenses per 88.44 requirements.

CREATE TABLE dataset_licenses (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL REFERENCES data_objects(id) ON DELETE CASCADE,
    license_type VARCHAR(64) NOT NULL,
    usage_restrictions TEXT,
    attribution_requirements TEXT,
    redistribution_rules TEXT,
    commercial_restrictions TEXT,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE dataset_policies (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL REFERENCES data_objects(id) ON DELETE CASCADE,
    policy_name VARCHAR(100) NOT NULL,
    policy_type VARCHAR(64) NOT NULL,
    rules JSONB NOT NULL,
    enforcement_level VARCHAR(32) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_dataset_licenses_dataset_id ON dataset_licenses(dataset_id);
CREATE INDEX idx_dataset_policies_dataset_id ON dataset_policies(dataset_id);

