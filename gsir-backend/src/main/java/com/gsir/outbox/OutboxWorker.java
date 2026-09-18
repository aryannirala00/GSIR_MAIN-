package com.gsir.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.List;

@Component
public class OutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxWorker.class);

    private final OutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;

    public OutboxWorker(OutboxEventRepository outboxEventRepository, 
                        ApplicationEventPublisher eventPublisher,
                        TransactionTemplate transactionTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.eventPublisher = eventPublisher;
        this.transactionTemplate = transactionTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void processOutbox() {
        // We fetch and process one by one in isolated transactions to prevent blocking 
        // while also safely committing each one independently.
        
        // Wait, SKIP LOCKED must be called inside the transaction that also updates the row.
        // So we extract a method that processes one batch.
        processBatch(10);
    }
    
        private void processBatch(int limit) {
        try {
            transactionTemplate.execute(status -> {
                List<OutboxEvent> events = outboxEventRepository.claimUnpublished(limit);
                for (OutboxEvent event : events) {
                    try {
                        // Emit internal spring event. The listener could trigger an async Job or handle synchronously
                        eventPublisher.publishEvent(new OutboxDispatchedEvent(this, event));
                        
                        event.markPublished();
                        outboxEventRepository.save(event);
                        log.info("OutboxEvent {} successfully published", event.getId());
                    } catch (Exception e) {
                        log.error("Failed to process OutboxEvent {}: {}", event.getId(), e.getMessage(), e);
                        event.recordFailure(e.getMessage());
                        outboxEventRepository.save(event);
                        // We do not rollback the batch because we want to save the updated attempts count/status
                    }
                }
                return null;
            });
        } catch (Exception e) {
            log.error("Outbox batch processing failed: {}", e.getMessage(), e);
        }
    }
}

