CREATE TABLE federation_sites (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    organization VARCHAR(255),
    api_url VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    capabilities JSONB,
    policies JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE remote_executions (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    target_site_id UUID NOT NULL REFERENCES federation_sites(id),
    status VARCHAR(50) NOT NULL,
    payload JSONB,
    result JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

