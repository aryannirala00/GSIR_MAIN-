package com.gsir.common.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class ObservabilityAspect {

    private final MeterRegistry meterRegistry;

    public ObservabilityAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("execution(* com.gsir.tools.ToolExecutionService.executeTool(..))")
    public Object observeToolExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        String capability = (String) joinPoint.getArgs()[0];
        String status = "success";
        
        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            status = "failure";
            throw t;
        } finally {
            long duration = System.nanoTime() - start;
            meterRegistry.timer("gsir.tools.execution", "capability", capability, "status", status)
                    .record(duration, TimeUnit.NANOSECONDS);
        }
    }

    @Around("execution(* com.gsir.workflow.WorkflowOrchestrator.startWorkflow(..))")
    public Object observeWorkflowStart(ProceedingJoinPoint joinPoint) throws Throwable {
        meterRegistry.counter("gsir.workflows.started").increment();
        return joinPoint.proceed();
    }
}
