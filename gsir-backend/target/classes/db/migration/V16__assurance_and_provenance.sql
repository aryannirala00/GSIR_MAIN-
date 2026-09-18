CREATE TABLE research_artifacts (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    object_type VARCHAR(100) NOT NULL,
    location_uri VARCHAR(512) NOT NULL,
    expected_hash VARCHAR(255) NOT NULL,
    hash_algorithm VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_artifact_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX idx_artifact_project ON research_artifacts(project_id);

CREATE TABLE assurance_attestations (
    id UUID PRIMARY KEY,
    artifact_id UUID NOT NULL,
    project_id UUID NOT NULL,
    assurance_level VARCHAR(50) NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    attestation_metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_attest_artifact FOREIGN KEY (artifact_id) REFERENCES research_artifacts(id) ON DELETE CASCADE,
    CONSTRAINT fk_attest_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX idx_attest_project ON assurance_attestations(project_id);
