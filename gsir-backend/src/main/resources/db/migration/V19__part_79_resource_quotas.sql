CREATE TABLE resource_quotas (
    project_id UUID PRIMARY KEY,
    max_compute_hours DOUBLE PRECISION,
    max_ai_tokens BIGINT,
    max_parallel_jobs INTEGER,
    max_storage_bytes BIGINT
);
