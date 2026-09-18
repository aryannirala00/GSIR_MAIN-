package com.gsir.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Repository for transactional outbox events (Volume II Part 13).
 *
 * WRITE PATH: Called inside @Transactional service methods to atomically persist
 * the outbox event alongside the originating business mutation.
 *
 * READ PATH: The outbox worker queries findUnpublished() to retrieve pending events,
 * delivers each to the external queue, then marks the event published.
 */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Returns all events that have not yet been delivered to the external queue,
     * ordered by creation time (oldest first). The partial index
     * outbox_events_unpublished_idx on (created_at) WHERE status = 'QUEUED'
     * makes this query efficient even at high event volumes.
     */
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'QUEUED' ORDER BY e.createdAt ASC")
    List<OutboxEvent> findUnpublished();

    @Query(value = "SELECT * FROM outbox_events WHERE status = 'QUEUED' ORDER BY created_at ASC LIMIT :limit FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<OutboxEvent> claimUnpublished(@org.springframework.data.repository.query.Param("limit") int limit);
}

