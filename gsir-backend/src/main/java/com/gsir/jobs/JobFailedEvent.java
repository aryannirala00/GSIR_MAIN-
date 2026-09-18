package com.gsir.jobs;

import org.springframework.context.ApplicationEvent;

public class JobFailedEvent extends ApplicationEvent {
    private final JobEntity job;

    public JobFailedEvent(Object source, JobEntity job) {
        super(source);
        this.job = job;
    }

    public JobEntity getJob() {
        return job;
    }
}
