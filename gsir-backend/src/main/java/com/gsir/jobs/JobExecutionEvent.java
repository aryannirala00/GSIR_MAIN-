package com.gsir.jobs;

import org.springframework.context.ApplicationEvent;

public class JobExecutionEvent extends ApplicationEvent {
    
    private final JobEntity job;
    
    public JobExecutionEvent(Object source, JobEntity job) {
        super(source);
        this.job = job;
    }
    
    public JobEntity getJob() {
        return job;
    }
}
