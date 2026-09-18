ALTER TABLE outbox_events ADD COLUMN attempts INT NOT NULL DEFAULT 0;
ALTER TABLE outbox_events ADD COLUMN last_error TEXT;
ALTER TABLE outbox_events ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'QUEUED';

-- Update existing records if any
UPDATE outbox_events SET status = 'PUBLISHED' WHERE published_at IS NOT NULL;

DROP INDEX IF EXISTS outbox_events_unpublished_idx;

CREATE INDEX outbox_events_status_idx
    ON outbox_events(status, created_at)
    WHERE status = 'QUEUED';
