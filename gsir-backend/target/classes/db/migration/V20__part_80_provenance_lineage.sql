-- Part 80 — GSIR Data and Knowledge Provenance, Lineage and Research Traceability Architecture
-- Migration V20: Extends the existing provenance schema (V12) with integrity hashing,
-- actor identity, tool provenance fields, and validity/confidence tracking.

-- 80.17 Execution Provenance: add actor tracking and tool details to activities
ALTER TABLE prov_activities
    ADD COLUMN IF NOT EXISTS actor_type VARCHAR(50),          -- HUMAN, AI_AGENT, SYSTEM, EXTERNAL_SERVICE, SCHEDULED_PROCESS
    ADD COLUMN IF NOT EXISTS actor_id UUID,                   -- UUID of the acting user/agent/service
    ADD COLUMN IF NOT EXISTS tool_id VARCHAR(255),            -- 80.19 Tool provenance: tool identifier
    ADD COLUMN IF NOT EXISTS tool_version VARCHAR(64),        -- 80.19 Tool version
    ADD COLUMN IF NOT EXISTS integrity_hash VARCHAR(255),     -- 80.44/80.46 content hash of outputs
    ADD COLUMN IF NOT EXISTS workflow_id UUID,                -- link back to research_workflows
    ADD COLUMN IF NOT EXISTS step_id UUID,                    -- link back to research_workflow_steps
    ADD COLUMN IF NOT EXISTS confidence VARCHAR(50);          -- 80.84 AUTOMATIC, VERIFIED, RECONSTRUCTED, INFERRED, UNKNOWN

CREATE INDEX IF NOT EXISTS prov_act_workflow_idx ON prov_activities(workflow_id);
CREATE INDEX IF NOT EXISTS prov_act_step_idx ON prov_activities(step_id);
CREATE INDEX IF NOT EXISTS prov_act_actor_idx ON prov_activities(actor_id);

-- 80.43/80.44/80.46 Artifact integrity and validity
ALTER TABLE prov_artifacts
    ADD COLUMN IF NOT EXISTS validity_status VARCHAR(50) DEFAULT 'VALID',   -- VALID, STALE, REQUIRES_REVALIDATION, INVALID, UNKNOWN
    ADD COLUMN IF NOT EXISTS hash_algorithm VARCHAR(50) DEFAULT 'SHA-256',  -- algorithm used for content_hash
    ADD COLUMN IF NOT EXISTS node_type VARCHAR(50);                          -- 80.6 DATA, CODE, MODEL, WORKFLOW, EXECUTION, TOOL, RESULT, DECISION, KNOWLEDGE

-- 80.70 Provenance edges: add metadata column for edge provenance detail
ALTER TABLE prov_relationships
    ADD COLUMN IF NOT EXISTS metadata JSONB;

-- 80.85 Provenance observability: provenance events log
CREATE TABLE IF NOT EXISTS prov_events (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,   -- 80.86: NodeCreated, EdgeCreated, HashCalculated, IntegrityVerified, etc.
    entity_id UUID,                      -- entity affected
    detail JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT prov_event_proj_fk FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS prov_event_proj_idx ON prov_events(project_id);
CREATE INDEX IF NOT EXISTS prov_event_type_idx ON prov_events(event_type);
CREATE INDEX IF NOT EXISTS prov_event_entity_idx ON prov_events(entity_id);

