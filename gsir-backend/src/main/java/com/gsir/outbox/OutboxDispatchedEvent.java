package com.gsir.outbox;

import org.springframework.context.ApplicationEvent;

public class OutboxDispatchedEvent extends ApplicationEvent {
    
    private final OutboxEvent outboxEvent;

    public OutboxDispatchedEvent(Object source, OutboxEvent outboxEvent) {
        super(source);
        this.outboxEvent = outboxEvent;
    }

    public OutboxEvent getOutboxEvent() {
        return outboxEvent;
    }
}
