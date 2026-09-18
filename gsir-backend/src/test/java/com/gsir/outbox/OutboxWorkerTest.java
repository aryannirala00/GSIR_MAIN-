package com.gsir.outbox;

import com.gsir.common.id.UuidV7Generator;
import com.gsir.projects.ProjectEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class OutboxWorkerTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    @Transactional
    public void testOutboxWorkerClaiming() {
        // Just test that the repository can save and claim
        OutboxEvent event = OutboxEvent.projectCreated(UUID.randomUUID(), 
            new ProjectEntity(UUID.randomUUID(), UUID.randomUUID(), "Test", "Desc")
        );
        outboxEventRepository.save(event);
        
        List<OutboxEvent> claimed = outboxEventRepository.claimUnpublished(10);
        assertThat(claimed).hasSizeGreaterThanOrEqualTo(1);
        
        OutboxEvent found = claimed.stream().filter(e -> e.getId().equals(event.getId())).findFirst().orElse(null);
        assertThat(found).isNotNull();
        
        found.markPublished();
        outboxEventRepository.save(found);
        
        List<OutboxEvent> remaining = outboxEventRepository.claimUnpublished(10);
        boolean stillThere = remaining.stream().anyMatch(e -> e.getId().equals(event.getId()));
        assertThat(stillThere).isFalse();
    }
}
