package com.gsir.common.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AuditedEntityTest {

    static class SampleEntity extends AuditedEntity {
        void triggerCreate() {
            super.onCreate();
        }

        void triggerUpdate() {
            super.onUpdate();
        }
    }

    @Test
    @DisplayName("onCreate should set createdAt and updatedAt to current timestamp")
    void testOnCreate() {
        SampleEntity entity = new SampleEntity();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();

        Instant before = Instant.now().minusMillis(10);
        entity.triggerCreate();
        Instant after = Instant.now().plusMillis(10);

        assertThat(entity.getCreatedAt()).isNotNull().isBetween(before, after);
        assertThat(entity.getUpdatedAt()).isNotNull().isEqualTo(entity.getCreatedAt());
    }

    @Test
    @DisplayName("onUpdate should update updatedAt without modifying createdAt")
    void testOnUpdate() throws InterruptedException {
        SampleEntity entity = new SampleEntity();
        entity.triggerCreate();
        Instant originalCreatedAt = entity.getCreatedAt();

        Thread.sleep(10);
        entity.triggerUpdate();

        assertThat(entity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(entity.getUpdatedAt()).isAfter(originalCreatedAt);
    }
}

