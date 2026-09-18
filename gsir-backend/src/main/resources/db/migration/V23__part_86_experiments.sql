-- PART 86: EXPERIMENT MANAGEMENT

CREATE TABLE experiments (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    objective TEXT,
    research_question TEXT,
    hypothesis_id UUID,
    status VARCHAR(50) NOT NULL,
    owner_id UUID NOT NULL REFERENCES users(id),
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
CREATE INDEX idx_experiments_project ON experiments(project_id);
CREATE INDEX idx_experiments_owner ON experiments(owner_id);

CREATE TABLE experiment_protocols (
    id UUID PRIMARY KEY,
    experiment_id UUID NOT NULL REFERENCES experiments(id) ON DELETE CASCADE,
    version INT NOT NULL DEFAULT 1,
    preparation TEXT,
    steps TEXT,
    safety_requirements TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE experiment_variables (
    id UUID PRIMARY KEY,
    experiment_id UUID NOT NULL REFERENCES experiments(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL, -- INDEPENDENT, DEPENDENT, CONTROL, CONFOUNDING
    data_type VARCHAR(50) NOT NULL,
    unit VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE experiment_parameters (
    id UUID PRIMARY KEY,
    experiment_id UUID NOT NULL REFERENCES experiments(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    value VARCHAR(255),
    unit VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE experiment_runs (
    id UUID PRIMARY KEY,
    experiment_id UUID NOT NULL REFERENCES experiments(id) ON DELETE CASCADE,
    protocol_id UUID REFERENCES experiment_protocols(id),
    operator_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    resource_snapshot TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE experiment_observations (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES experiment_runs(id) ON DELETE CASCADE,
    variable_id UUID REFERENCES experiment_variables(id),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    value TEXT NOT NULL,
    unit VARCHAR(100),
    source VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE experiment_results (
    id UUID PRIMARY KEY,
    experiment_id UUID NOT NULL REFERENCES experiments(id) ON DELETE CASCADE,
    run_id UUID REFERENCES experiment_runs(id),
    result_type VARCHAR(100) NOT NULL,
    value TEXT NOT NULL,
    unit VARCHAR(100),
    confidence DOUBLE PRECISION,
    validation_status VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE experiment_events (
    id UUID PRIMARY KEY,
    experiment_id UUID NOT NULL REFERENCES experiments(id) ON DELETE CASCADE,
    run_id UUID REFERENCES experiment_runs(id),
    event_type VARCHAR(100) NOT NULL,
    actor_id UUID NOT NULL REFERENCES users(id),
    payload JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

